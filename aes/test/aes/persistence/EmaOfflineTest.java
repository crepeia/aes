package aes.persistence;

import aes.model.EmaAnswer;
import aes.model.EmaOperation;
import aes.model.User;
import java.io.*;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.naming.Context;
import javax.persistence.*;
import javax.transaction.*;
import static aes.persistence.DailyLogOfflineTest.*;

/** Contract tests with JPA/JTA doubles; no real database or application server. */
public class EmaOfflineTest {
    static class Data implements Serializable {
        List<EmaAnswer> answers = new ArrayList<>();
        Map<String, EmaOperation> receipts = new HashMap<>();
        long nextId = 100;
    }
    static class Database {
        Data data = new Data();
        ReentrantLock lock = new ReentrantLock();
        boolean failReceipt;
    }
    static class Transaction {
        Database db;
        Data snapshot;
        boolean active;
        Transaction(Database db) { this.db = db; }
        void acquire() throws Exception {
            if (!active) throw new AssertionError("No transaction");
            if (snapshot != null) return;
            db.lock.lock();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream stream = new ObjectOutputStream(bytes)) { stream.writeObject(db.data); }
            snapshot = (Data) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        }
        UserTransaction api = proxy(UserTransaction.class, (method, args) -> {
            switch (method) {
                case "begin": active = true; return null;
                case "getStatus": return active ? Status.STATUS_ACTIVE : Status.STATUS_NO_TRANSACTION;
                case "rollback": if (snapshot != null) db.data = snapshot;
                case "commit": active = false; if (snapshot != null) { snapshot = null; db.lock.unlock(); } return null;
                default: throw new AssertionError(method);
            }
        });
        EntityManager em = proxy(EntityManager.class, (method, args) -> {
            switch (method) {
                case "joinTransaction": case "flush": return null;
                case "refresh":
                    check(args[1] == LockModeType.PESSIMISTIC_WRITE, "Missing refresh lock"); return null;
                case "find":
                    if (args[0] == User.class) {
                        check(args.length == 3 && args[2] == LockModeType.PESSIMISTIC_WRITE, "Missing owner lock");
                        acquire(); User user = new User(); user.setId((Long) args[1]); return user;
                    }
                    check(snapshot != null, "Receipt read before owner lock"); return db.data.receipts.get(args[1]);
                case "createQuery":
                    check(snapshot != null, "Period read before owner lock");
                    Map<String, Object> params = new HashMap<>();
                    final TypedQuery<?>[] self = new TypedQuery<?>[1];
                    self[0] = proxy(TypedQuery.class, (name, values) -> {
                        if (name.equals("setMaxResults")) return self[0];
                        if (name.equals("setParameter")) { params.put((String) values[0], values[1]); return self[0]; }
                        if (!name.equals("getResultList")) throw new AssertionError(name);
                        List<EmaAnswer> result = new ArrayList<>();
                        for (EmaAnswer answer : db.data.answers) {
                            if (Long.valueOf(answer.getUser().getId()).equals(params.get("userId")) &&
                                    answer.getPeriod().equals(params.get("period")) &&
                                    !answer.getTimestamp().before((Date) params.get("start")) &&
                                    answer.getTimestamp().before((Date) params.get("end"))) result.add(answer);
                        }
                        return result;
                    }); return self[0];
                case "persist":
                    if (args[0] instanceof EmaAnswer) {
                        EmaAnswer answer = (EmaAnswer) args[0]; answer.setId(db.data.nextId++); db.data.answers.add(answer);
                    } else {
                        if (db.failReceipt) throw new RuntimeException("simulated receipt write failure");
                        EmaOperation receipt = (EmaOperation) args[0];
                        check(!db.data.receipts.containsKey(receipt.getKey()), "Duplicate receipt");
                        db.data.receipts.put(receipt.getKey(), receipt);
                    } return null;
                default: throw new AssertionError(method);
            }
        });
    }
    static EmaAnswer answer(int hour) {
        EmaAnswer answer = new EmaAnswer();
        answer.setTimestamp(Date.from(LocalDate.of(2026, 9, 28).atTime(hour, 0).atZone(ZoneId.of("America/Sao_Paulo")).toInstant()));
        answer.setPeriod(hour >= 8 && hour < 14 ? "manha" : hour >= 14 && hour < 20 ? "tarde" : "noite");
        answer.setCraving(1); answer.setMoodValence(2); answer.setMoodArousal(3);
        answer.setSelfEfficacy(4); answer.setMotivation(5); answer.setAvailability(0);
        answer.setCompany("Family"); answer.setCurrentLocation("Home"); answer.setSomeoneDrinking(false);
        answer.setConsumedAlcohol(true); answer.setNumberOfDoses("5+"); return answer;
    }
    static String save(Database db, long owner, String key, EmaAnswer answer) throws Exception {
        Transaction tx = new Transaction(db);
        return new EmaAnswerDAO().saveAnswerOnce(answer, owner, key, tx.em, tx.api);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, Naming.class.getName());
        Database db = new Database();
        check(EmaAnswerDAO.isValidOfflineAnswer(answer(10)), "Valid legacy answer rejected");
        EmaAnswer invalid = answer(10); invalid.setCraving(6);
        check(!EmaAnswerDAO.isValidOfflineAnswer(invalid), "Invalid scale accepted");
        invalid = answer(10); invalid.setPeriod("noite");
        check(!EmaAnswerDAO.isValidOfflineAnswer(invalid), "Wrong period accepted");
        invalid = answer(10); invalid.setConsumedAlcohol(false);
        check(!EmaAnswerDAO.isValidOfflineAnswer(invalid), "Inconsistent doses accepted");
        invalid.setNumberOfDoses("0"); check(EmaAnswerDAO.isValidOfflineAnswer(invalid), "Zero doses rejected");

        String first = save(db, 1, "same", answer(10));
        check(first.equals(save(db, 1, "same", answer(10))), "Replay response changed");
        check(db.data.answers.size() == 1 && db.data.receipts.size() == 1, "Replay duplicated answer");
        org.primefaces.json.JSONObject receipt = new org.primefaces.json.JSONObject(first);
        check(receipt.getLong("id") == db.data.answers.get(0).getId(), "Missing canonical id");
        check(receipt.getString("periodKey").equals("2026-09-28_manha"), "Wrong receipt slot");
        EmaAnswer changed = answer(10); changed.setCompany("Friends");
        try { save(db, 1, "same", changed); throw new AssertionError("Missing payload conflict"); }
        catch (EmaAnswerDAO.AnswerConflict expected) { check(expected.getMessage().equals("IDEMPOTENCY_CONFLICT"), "Wrong conflict"); }
        try { save(db, 1, "other", answer(11)); throw new AssertionError("Duplicate slot"); }
        catch (EmaAnswerDAO.AnswerConflict expected) { check(expected.getMessage().equals("EMA_PERIOD_ALREADY_ANSWERED"), "Wrong slot conflict"); }
        save(db, 2, "same", answer(10)); check(db.data.answers.size() == 2, "Owner key not isolated");
        save(db, 1, "early", answer(2)); save(db, 1, "evening", answer(21));
        check(db.data.answers.size() == 4, "Early morning blocked evening");
        check(first.equals(save(db, 1, "same", answer(10))), "Later slots changed original receipt");

        Database rollback = new Database(); rollback.failReceipt = true;
        try { save(rollback, 1, "failure", answer(10)); throw new AssertionError("Missing failure"); }
        catch (SQLException expected) { }
        check(rollback.data.answers.isEmpty() && rollback.data.receipts.isEmpty(), "Partial EMA transaction committed");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            Database concurrent = new Database(); List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 12; i++) calls.add(() -> save(concurrent, 1, "shared-key", answer(10)));
            Set<String> responses = new HashSet<>();
            for (Future<String> result : executor.invokeAll(calls)) responses.add(result.get());
            check(responses.size() == 1 && concurrent.data.answers.size() == 1, "Concurrent retry duplicated EMA");
            Database samePeriod = new Database(); calls.clear();
            for (int i = 0; i < 12; i++) {
                final String key = "different-" + i;
                calls.add(() -> { try { return save(samePeriod, 1, key, answer(10)); }
                    catch (EmaAnswerDAO.AnswerConflict expected) { return "conflict"; } });
            }
            int conflicts = 0;
            for (Future<String> result : executor.invokeAll(calls)) if (result.get().equals("conflict")) conflicts++;
            check(conflicts == 11 && samePeriod.data.answers.size() == 1, "Concurrent period duplicates accepted");
        } finally { executor.shutdownNow(); }
        System.out.println("PASS: EMA validation, replay, content/period conflicts, owner isolation, morning/evening, rollback, concurrent requests (JPA/JTA doubles; no real DB)");
    }
}
