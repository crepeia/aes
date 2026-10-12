package aes.persistence;

import aes.model.ChallengeUser;
import aes.model.DailyLog;
import aes.model.DailyLogOperation;
import aes.model.Record;
import aes.model.User;
import java.io.*;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/** DAO contract tests with an in-memory JPA/JTA double. No database or server. */
public class DailyLogOfflineTest {
    public static class Naming implements InitialContextFactory {
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return proxy(Context.class, (method, args) -> null);
        }
    }
    interface Call { Object invoke(String method, Object[] args) throws Throwable; }
    @SuppressWarnings("unchecked")
    static <T> T proxy(Class<T> type, Call call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (instance, method, args) -> call.invoke(method.getName(), args));
    }
    static class Data implements Serializable {
        Map<Long, User> users = new HashMap<>();
        Map<Long, Record> records = new HashMap<>();
        List<DailyLog> logs = new ArrayList<>();
        List<ChallengeUser> challenges = new ArrayList<>();
        Map<String, DailyLogOperation> receipts = new HashMap<>();
        long nextId = 100;
    }
    static class Database {
        Data data = new Data();
        ReentrantLock lock = new ReentrantLock();
        boolean failReceipt;
        Database() {
            for (long id = 1; id <= 2; id++) {
                User user = new User(); user.setId(id); data.users.put(id, user);
                Record record = new Record(); record.setId(id * 10); record.setUser(user); data.records.put(id * 10, record);
            }
        }
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
                    if (args[1] != LockModeType.PESSIMISTIC_WRITE) throw new AssertionError("Missing lock");
                    return null;
                case "find":
                    if (args[0] == User.class) {
                        if (args.length != 3 || args[2] != LockModeType.PESSIMISTIC_WRITE) throw new AssertionError("Missing owner lock");
                        acquire(); return db.data.users.get(args[1]);
                    }
                    if (snapshot == null) throw new AssertionError("Read without owner lock");
                    return args[0] == Record.class ? db.data.records.get(args[1]) : db.data.receipts.get(args[1]);
                case "createQuery":
                    String query = (String) args[0]; Map<String, Object> params = new HashMap<>();
                    final TypedQuery<?>[] self = new TypedQuery<?>[1];
                    self[0] = proxy(TypedQuery.class, (name, values) -> {
                        if (name.equals("setParameter")) { params.put((String) values[0], values[1]); return self[0]; }
                        if (!name.equals("getResultList")) throw new AssertionError(name);
                        List<Object> results = new ArrayList<>();
                        if (query.contains("FROM DailyLog")) {
                            for (DailyLog log : db.data.logs) if (Long.valueOf(log.getRecord().getId()).equals(params.get("recordId")) && log.getLogDate().equals(params.get("logDate"))) results.add(log);
                        } else {
                            for (ChallengeUser ch : db.data.challenges) if (Long.valueOf(ch.getUser().getId()).equals(params.get("userId"))) results.add(ch);
                        }
                        return results;
                    }); return self[0];
                case "persist":
                    Object value = args[0];
                    if (value instanceof DailyLog) { DailyLog log = (DailyLog) value; log.setId(db.data.nextId++); db.data.logs.add(log); }
                    else if (value instanceof ChallengeUser) { ChallengeUser ch = (ChallengeUser) value; ch.setId(db.data.nextId++); db.data.challenges.add(ch); }
                    else {
                        if (db.failReceipt) throw new RuntimeException("simulated receipt write failure");
                        DailyLogOperation receipt = (DailyLogOperation) value;
                        if (db.data.receipts.containsKey(receipt.getKey())) throw new AssertionError("Duplicate receipt");
                        db.data.receipts.put(receipt.getKey(), receipt);
                    } return null;
                case "remove": db.data.challenges.remove(args[0]); return null;
                default: throw new AssertionError(method);
            }
        });
    }
    static DailyLog log(long owner, int drinks) {
        Record record = new Record(); record.setId(owner * 10);
        DailyLog log = new DailyLog(); log.setRecord(record); log.setDrinks(drinks);
        log.setLogDate(LocalDate.of(2026, 9, 17)); log.setContext(""); log.setConsequences(""); return log;
    }
    static String save(Database db, long owner, String key, int drinks, boolean challenges) throws Exception {
        Transaction tx = new Transaction(db);
        return new DailyLogDAO().editOrCreateOnce(log(owner, drinks), owner, key, challenges,
                LocalDate.of(2026, 9, 17), tx.em, tx.api);
    }
    static void check(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, Naming.class.getName());
        Database db = new Database();
        String first = save(db, 1, "operation-a", 0, true);
        check(new org.primefaces.json.JSONObject(first).getLong("id") == db.data.logs.get(0).getId(), "Missing canonical dose id");
        check(first.equals(save(db, 1, "operation-a", 0, true)), "Replay must return identical receipt");
        check(db.data.logs.size() == 1 && db.data.challenges.size() == 2, "Replay duplicated log/points");
        check(db.data.challenges.stream().mapToLong(ChallengeUser::getScore).sum() == 13, "Legacy 6 + 7 points changed");
        try { save(db, 1, "operation-a", 3, true); throw new AssertionError("Missing conflict"); }
        catch (DailyLogDAO.IdempotencyConflict expected) { }
        save(db, 1, "operation-b", 3, true);
        check(db.data.challenges.size() == 1, "Changing from zero must remove the don't-drink challenge");
        save(db, 1, "operation-a", 0, true);
        check(db.data.logs.get(0).getDrinks() == 3, "Old retry overwrote newer total");
        save(db, 2, "operation-a", 2, false);
        check(db.data.logs.size() == 2 && db.data.challenges.size() == 1, "Owner isolation or context-only effect failed");

        Database rollback = new Database(); rollback.failReceipt = true;
        try { save(rollback, 1, "failure", 0, true); throw new AssertionError("Missing rollback"); }
        catch (SQLException expected) { }
        check(rollback.data.logs.isEmpty() && rollback.data.challenges.isEmpty() && rollback.data.receipts.isEmpty(), "Partial transaction committed");

        Database concurrent = new Database(); ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 12; i++) calls.add(() -> save(concurrent, 1, "same-key", 0, true));
            Set<String> responses = new HashSet<>();
            for (Future<String> result : executor.invokeAll(calls)) responses.add(result.get());
            check(responses.size() == 1 && concurrent.data.logs.size() == 1 && concurrent.data.challenges.size() == 2, "Concurrent replay duplicated effects");
        } finally { executor.shutdownNow(); }
        System.out.println("PASS: replay, conflict, canonical id, legacy points/removal, owner isolation, rollback, concurrent replay (JPA/JTA doubles; no real DB)");
    }
}
