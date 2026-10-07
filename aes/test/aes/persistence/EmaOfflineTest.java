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
    // Simulate calendar extraction in the database's zone as well as instant bounds.
    static EntityManager readManager(List<EmaAnswer> answers, ZoneId databaseZone) {
        return proxy(EntityManager.class, (method, args) -> {
            if (!method.equals("createQuery")) throw new AssertionError(method);
            String query = (String) args[0];
            Map<String, Object> params = new HashMap<>();
            final TypedQuery<?>[] self = new TypedQuery<?>[1];
            self[0] = proxy(TypedQuery.class, (name, values) -> {
                if (name.equals("setMaxResults")) return self[0];
                if (name.equals("setParameter")) { params.put((String) values[0], values[1]); return self[0]; }
                if (!name.equals("getResultList") && !name.equals("getSingleResult")) throw new AssertionError(name);
                List<Date> matches = new ArrayList<>();
                for (EmaAnswer answer : answers) {
                    if (!Long.valueOf(answer.getUser().getId()).equals(params.get("userId")) ||
                            !answer.getPeriod().equals(params.get("period"))) continue;
                    Date timestamp = answer.getTimestamp();
                    boolean inDay;
                    if (query.contains("FUNCTION('YEAR'")) {
                        ZonedDateTime calendar = timestamp.toInstant().atZone(databaseZone);
                        inDay = Integer.valueOf(calendar.getYear()).equals(params.get("year")) &&
                                Integer.valueOf(calendar.getMonthValue()).equals(params.get("month")) &&
                                Integer.valueOf(calendar.getDayOfMonth()).equals(params.get("day"));
                    } else {
                        inDay = !timestamp.before((Date) params.get("start")) && timestamp.before((Date) params.get("end"));
                    }
                    if (inDay) matches.add(timestamp);
                }
                matches.sort(Collections.reverseOrder());
                return name.equals("getSingleResult") ? Long.valueOf(matches.size()) : matches.subList(0, Math.min(1, matches.size()));
            });
            return self[0];
        });
    }
    static void availabilityTests() throws Exception {
        EmaAnswerDAO dao = new EmaAnswerDAO();
        Database db = new Database();
        save(db, 1, "night", answer(21));
        // 21:00 in Brasilia is 00:00 UTC on the next date.
        for (String zone : new String[]{"UTC", "America/Sao_Paulo", "Asia/Tokyo"}) {
            EntityManager em = readManager(db.data.answers, ZoneId.of(zone));
            check(dao.checkSeJaRespondeu(1L, "2026-09-28_noite", em), "21h answer missed in zone " + zone);
            check(answer(21).getTimestamp().equals(dao.getLastAnswerTimestamp(1L, "2026-09-28_noite", em)), "Missing evening timestamp");
            check(!dao.checkSeJaRespondeu(1L, "2026-09-29_noite", em), "Evening answer leaked into next local date");
            check(!dao.checkSeJaRespondeu(2L, "2026-09-28_noite", em), "Other user's answer leaked");
        }
        try { save(db, 1, "night-again", answer(23)); throw new AssertionError("21h answer allowed another at 23h"); }
        catch (EmaAnswerDAO.AnswerConflict expected) { check(expected.getMessage().equals("EMA_PERIOD_ALREADY_ANSWERED"), "Wrong night conflict"); }
        for (int hour : new int[]{2, 8, 14}) save(db, 1, "slot-" + hour, answer(hour));
        EntityManager em = readManager(db.data.answers, ZoneId.of("UTC"));
        check(dao.checkSeJaRespondeu(1L, "2026-9-28_manha", em), "Legacy unpadded date failed");
        check(dao.checkSeJaRespondeu(1L, "2026-09-28_tarde", em), "Afternoon disappeared");
        check(answer(21).getTimestamp().equals(dao.getLastAnswerTimestamp(1L, "2026-09-28_noite", em)), "Delayed early-morning answer replaced evening");
        check(db.data.answers.size() == 4, "Legacy early-morning exception changed");
        EmaAnswer nextMidnight = answer(0);
        nextMidnight.setTimestamp(Date.from(nextMidnight.getTimestamp().toInstant().plus(1, java.time.temporal.ChronoUnit.DAYS)));
        save(db, 1, "next-midnight", nextMidnight);
        check(nextMidnight.getTimestamp().equals(dao.getLastAnswerTimestamp(1L, "2026-09-29_noite", em)), "Midnight boundary excluded next day");
        check(answer(21).getTimestamp().equals(dao.getLastAnswerTimestamp(1L, "2026-09-28_noite", em)), "Next midnight included in previous day");
        EntityManager broken = proxy(EntityManager.class, (method, args) -> { throw new PersistenceException("simulated read failure"); });
        try { dao.checkSeJaRespondeu(1L, "2026-09-28_manha", broken); throw new AssertionError("Failed count released the period"); }
        catch (IllegalStateException expected) { check(expected.getCause() instanceof PersistenceException, "Read error lost its cause"); }
        try { dao.getLastAnswerTimestamp(1L, "2026-09-28_noite", broken); throw new AssertionError("Failed night lookup released the period"); }
        catch (IllegalStateException expected) { check(expected.getCause() instanceof PersistenceException, "Night error lost its cause"); }
        System.out.println("PASS: EMA availability, local date boundaries across DB zones, 21h blocks 23h, midnight/early-morning exception, read failures (JPA doubles; no real DB)");
    }
    static EmaAnswer withTimeContext(EmaAnswer answer) {
        answer.setCapturedAtUtcMs(answer.getTimestamp().getTime());
        answer.setDeviceTimeZone("America/Manaus");
        answer.setDeviceUtcOffsetMinutes(-240);
        return answer;
    }

    static void timeContextTests() throws Exception {
        String legacyHash = "efc2e69ae0573ac72a3d209162a3bb6b69a39e306ae5271605d255cb4d88f70e";
        check(legacyHash.equals(EmaAnswerDAO.answerHash(answer(10))), "Hash changed for old queued answers");
        Database legacy = new Database();
        EmaOperation oldReceipt = new EmaOperation();
        oldReceipt.setKey("1:old"); oldReceipt.setRequestHash(legacyHash);
        oldReceipt.setResponseJson("{\"id\":17,\"idempotencyKey\":\"old\",\"periodKey\":\"2026-09-28_manha\"}");
        legacy.data.receipts.put(oldReceipt.getKey(), oldReceipt);
        EmaAnswer historical = answer(10); historical.setId(17);
        User user = new User(); user.setId(1); historical.setUser(user); legacy.data.answers.add(historical);
        check(oldReceipt.getResponseJson().equals(save(legacy, 1, "old", answer(10))), "Old receipt no longer replays");
        check(historical.getCapturedAtUtcMs() == null && historical.getReceivedAtUtcMs() == null &&
                historical.getDeviceTimeZone() == null && historical.getDeviceUtcOffsetMinutes() == null,
                "Historical row was backfilled with invented metadata");

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        EmaAnswer event = withTimeContext(answer(23));
        event.setReceivedAtUtcMs(1L); // A client's server time must never be trusted.
        String json = mapper.writeValueAsString(event);
        check(json.contains("2026-09-28T23:00:00.000"), "Legacy JSON timestamp format changed");
        EmaAnswer decoded = mapper.readValue(json, EmaAnswer.class);
        check(decoded.getReceivedAtUtcMs() == null, "Jackson accepted a client-supplied reception time");
        check(decoded.getCapturedAtUtcMs().equals(event.getCapturedAtUtcMs()) &&
                decoded.getTimestamp().equals(event.getTimestamp()), "JSON changed the captured instant");
        check(EmaAnswerDAO.isValidOfflineAnswer(decoded), "Valid new context rejected");
        String newHash = EmaAnswerDAO.answerHash(event);
        event.setReceivedAtUtcMs(2L);
        check(newHash.equals(EmaAnswerDAO.answerHash(event)), "Reception time leaked into the idempotency hash");

        Database db = new Database();
        long before = System.currentTimeMillis();
        String result = save(db, 1, "context", event);
        long after = System.currentTimeMillis();
        EmaAnswer stored = db.data.answers.get(0);
        check(stored.getTimestamp().equals(answer(23).getTimestamp()) &&
                stored.getCapturedAtUtcMs().equals(answer(23).getTimestamp().getTime()), "Delayed arrival changed collection time");
        check(stored.getReceivedAtUtcMs() >= before && stored.getReceivedAtUtcMs() <= after, "Invalid server reception time");
        org.primefaces.json.JSONObject context = new org.primefaces.json.JSONObject(result).getJSONObject("timeContext");
        check(context.getLong("capturedAtUtcMs") == stored.getCapturedAtUtcMs() &&
                context.getLong("receivedAtUtcMs") == stored.getReceivedAtUtcMs() &&
                context.getInt("deviceUtcOffsetMinutes") == -240 && context.getString("deviceTimeZone").equals("America/Manaus"),
                "Receipt did not acknowledge persisted context");
        check(result.equals(save(db, 1, "context", withTimeContext(answer(23)))), "Retry changed time or receipt");
        check(db.data.answers.size() == 1, "Time context caused a duplicate answer");
        EmaAnswer changed = withTimeContext(answer(23)); changed.setDeviceUtcOffsetMinutes(-180);
        try { save(db, 1, "context", changed); throw new AssertionError("Changed context reused a key"); }
        catch (EmaAnswerDAO.AnswerConflict expected) { check(expected.getMessage().equals("IDEMPOTENCY_CONFLICT"), "Wrong context conflict"); }

        EmaAnswer invalid = withTimeContext(answer(10)); invalid.setCapturedAtUtcMs(invalid.getCapturedAtUtcMs() + 1);
        check(!EmaAnswerDAO.isValidOfflineAnswer(invalid), "Contradictory timestamps accepted");
        invalid = withTimeContext(answer(10)); invalid.setDeviceUtcOffsetMinutes(900);
        check(!EmaAnswerDAO.isValidOfflineAnswer(invalid), "Invalid offset accepted");
        invalid.setDeviceUtcOffsetMinutes(null);
        check(!EmaAnswerDAO.isValidTimeContext(invalid), "Incomplete context accepted");
        invalid = withTimeContext(answer(10)); invalid.setDeviceTimeZone("invalid\nzone");
        check(!EmaAnswerDAO.isValidTimeContext(invalid), "Malformed zone accepted");
        EmaAnswer offsetOnly = withTimeContext(answer(10)); offsetOnly.setDeviceTimeZone(null);
        check(EmaAnswerDAO.isValidOfflineAnswer(offsetOnly), "Device without IANA support rejected");
        save(db, 2, "offset-only", offsetOnly);
        EmaAnswer oldClient = mapper.readValue(mapper.writeValueAsString(answer(14)), EmaAnswer.class);
        check(EmaAnswerDAO.isValidOfflineAnswer(oldClient), "Old client without context rejected");
        save(db, 1, "old-client", oldClient);
        check(oldClient.getCapturedAtUtcMs() == null && oldClient.getDeviceTimeZone() == null &&
                oldClient.getDeviceUtcOffsetMinutes() == null && oldClient.getReceivedAtUtcMs() != null,
                "Legacy submission lost compatibility or invented a capture context");
        System.out.println("PASS: EMA optional time context, legacy hash/receipt/data compatibility, real Jackson contract, delayed capture, server receipt time, retry conflicts (JPA/JTA doubles; no real DB)");
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, Naming.class.getName());
        timeContextTests();
        availabilityTests();
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
