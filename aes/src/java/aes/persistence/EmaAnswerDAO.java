package aes.persistence;

import aes.model.EmaAnswer;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class EmaAnswerDAO extends GenericDAO<EmaAnswer> {

    public EmaAnswerDAO() throws NamingException {
        super(EmaAnswer.class);
    }


    public static class AnswerConflict extends SQLException {
        public AnswerConflict(String reason) { super(reason); }
    }

    private static java.time.ZonedDateTime localTime(EmaAnswer answer) {
        return answer.getTimestamp().toInstant().atZone(java.time.ZoneId.of("America/Sao_Paulo"));
    }

    public static String periodKey(EmaAnswer answer) {
        java.time.ZonedDateTime time = localTime(answer);
        return time.toLocalDate() + "_" + (time.getHour() < 8 ? "madrugada" : answer.getPeriod());
    }

    public static boolean isValidOfflineAnswer(EmaAnswer answer) {
        if (answer == null || answer.getId() != 0 || answer.getTimestamp() == null ||
                answer.getConsumedAlcohol() == null || answer.getSomeoneDrinking() == null) return false;
        for (Integer value : new Integer[]{answer.getCraving(), answer.getMoodValence(), answer.getMoodArousal(),
                answer.getSelfEfficacy(), answer.getMotivation(), answer.getAvailability()}) {
            if (value == null || value < 0 || value > 5) return false;
        }
        for (String value : new String[]{answer.getCompany(), answer.getCurrentLocation()}) {
            if (value == null || value.trim().isEmpty() || value.length() > 100) return false;
        }
        int hour = localTime(answer).getHour();
        String period = hour >= 8 && hour < 14 ? "manha" : hour >= 14 && hour < 20 ? "tarde" : "noite";
        return period.equals(answer.getPeriod()) && (answer.getConsumedAlcohol()
                ? java.util.Arrays.asList("1", "2", "3", "4", "5+").contains(answer.getNumberOfDoses())
                : "0".equals(answer.getNumberOfDoses()));
    }

    /** Only keyed clients use this path. Answer and receipt share one transaction. */
    public String saveAnswerOnce(EmaAnswer answer, long ownerId, String key, EntityManager em,
            javax.transaction.UserTransaction transaction) throws SQLException {
        try {
            transaction.begin();
            em.joinTransaction();
            aes.model.User user = em.find(aes.model.User.class, ownerId, javax.persistence.LockModeType.PESSIMISTIC_WRITE);
            em.refresh(user, javax.persistence.LockModeType.PESSIMISTIC_WRITE);
            String receiptKey = ownerId + ":" + key;
            String hash = answerHash(answer);
            aes.model.EmaOperation previous = em.find(aes.model.EmaOperation.class, receiptKey);
            if (previous != null) {
                if (!hash.equals(previous.getRequestHash())) throw new AnswerConflict("IDEMPOTENCY_CONFLICT");
                String result = previous.getResponseJson();
                transaction.commit();
                return result;
            }
            // Use the answer's original wall-clock slot, never the retry time.
            java.time.ZonedDateTime time = localTime(answer);
            int hour = time.getHour();
            int startHour = hour < 8 ? 0 : hour < 14 ? 8 : hour < 20 ? 14 : 20;
            int endHour = startHour == 0 ? 8 : startHour == 8 ? 14 : startHour == 14 ? 20 : 24;
            java.time.ZonedDateTime day = time.toLocalDate().atStartOfDay(time.getZone());
            List<EmaAnswer> matches = em.createQuery(
                    "SELECT e FROM EmaAnswer e WHERE e.user.id=:userId AND e.period=:period AND e.timestamp>=:start AND e.timestamp<:end",
                    EmaAnswer.class).setParameter("userId", ownerId).setParameter("period", answer.getPeriod())
                    .setParameter("start", Date.from(day.plusHours(startHour).toInstant()))
                    .setParameter("end", Date.from(day.plusHours(endHour).toInstant())).setMaxResults(1).getResultList();
            if (!matches.isEmpty()) throw new AnswerConflict("EMA_PERIOD_ALREADY_ANSWERED");
            answer.setUser(user);
            em.persist(answer);
            em.flush();
            String result = new org.primefaces.json.JSONObject().put("id", answer.getId())
                    .put("idempotencyKey", key).put("periodKey", periodKey(answer)).toString();
            aes.model.EmaOperation receipt = new aes.model.EmaOperation();
            receipt.setKey(receiptKey);
            receipt.setRequestHash(hash);
            receipt.setResponseJson(result);
            em.persist(receipt);
            em.flush();
            transaction.commit();
            return result;
        } catch (Exception error) {
            try {
                if (transaction.getStatus() != javax.transaction.Status.STATUS_NO_TRANSACTION) transaction.rollback();
            } catch (Exception rollbackError) { error.addSuppressed(rollbackError); }
            if (error instanceof AnswerConflict) throw (AnswerConflict) error;
            throw new SQLException("EMA_TRANSACTION_FAILED", error);
        }
    }

    static String answerHash(EmaAnswer answer) throws Exception {
        String canonical = new org.primefaces.json.JSONArray()
                .put(answer.getCraving()).put(answer.getMoodValence()).put(answer.getMoodArousal())
                .put(answer.getSelfEfficacy()).put(answer.getMotivation()).put(answer.getCompany())
                .put(answer.getCurrentLocation()).put(answer.getSomeoneDrinking()).put(answer.getAvailability())
                .put(answer.getConsumedAlcohol()).put(answer.getNumberOfDoses())
                .put(answer.getTimestamp().getTime()).put(answer.getPeriod()).toString();
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte value : digest) hash.append(String.format("%02x", value & 0xff));
        return hash.toString();
    }

    public void saveAnswer(EmaAnswer answer, EntityManager entityManager) throws SQLException {
        super.insertOrUpdate(answer, entityManager);
    }

    /**
     * Verifica se o utilizador já respondeu num determinado período.
     *
     * @param periodDate  chave no formato "yyyy-M-d_period", ex: "2025-5-4_manha"
     */
    public boolean checkSeJaRespondeu(Long userId, String periodDate, EntityManager em) {
        try {
            String[] parts = periodDate.split("_");
            if (parts.length != 2) return false;

            String datePart = parts[0];
            String period   = parts[1];

            String[] dateParts = datePart.split("-");
            int year  = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day   = Integer.parseInt(dateParts[2]);

            Long count = em.createQuery(
                "SELECT COUNT(e) FROM EmaAnswer e " +
                "WHERE e.user.id = :userId " +
                "  AND e.period = :period " +
                "  AND FUNCTION('YEAR',  e.timestamp) = :year " +
                "  AND FUNCTION('MONTH', e.timestamp) = :month " +
                "  AND FUNCTION('DAY',   e.timestamp) = :day",
                Long.class)
                .setParameter("userId", userId)
                .setParameter("period", period)
                .setParameter("year",   year)
                .setParameter("month",  month)
                .setParameter("day",    day)
                .getSingleResult();

            return count > 0;

        } catch (Exception e) {
            System.err.println("Erro ao verificar resposta no DAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retorna o timestamp da ÚLTIMA resposta de "noite" para a data indicada,
     * ou null se não houver nenhuma resposta nesse período.
     *
     * Usado em checkPeriodoNoite() para distinguir resposta de madrugada
     * (00h-07h) de resposta de noite real (20h-23h) e decidir se o utilizador
     * ainda pode responder à noite real.
     *
     * @param periodDate  chave no formato "yyyy-M-d_noite", ex: "2025-5-4_noite"
     * @return            timestamp da resposta mais recente, ou null se não existe
     */
    public Date getLastAnswerTimestamp(Long userId, String periodDate, EntityManager em) {
        try {
            String[] parts = periodDate.split("_");
            if (parts.length != 2) return null;

            String datePart = parts[0];
            String period   = parts[1];

            String[] dateParts = datePart.split("-");
            int year  = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day   = Integer.parseInt(dateParts[2]);

            // Mesma lógica de filtro de checkSeJaRespondeu,
            // mas retorna o timestamp mais recente em vez de COUNT.
            List<Date> results = em.createQuery(
                "SELECT e.timestamp FROM EmaAnswer e " +
                "WHERE e.user.id = :userId " +
                "  AND e.period = :period " +
                "  AND FUNCTION('YEAR',  e.timestamp) = :year " +
                "  AND FUNCTION('MONTH', e.timestamp) = :month " +
                "  AND FUNCTION('DAY',   e.timestamp) = :day " +
                "ORDER BY e.timestamp DESC",
                Date.class)
                .setParameter("userId", userId)
                .setParameter("period", period)
                .setParameter("year",   year)
                .setParameter("month",  month)
                .setParameter("day",    day)
                .setMaxResults(1)   // só o mais recente
                .getResultList();

            return results.isEmpty() ? null : results.get(0);

        } catch (Exception e) {
            System.err.println("Erro ao buscar último timestamp no DAO: " + e.getMessage());
            return null;
        }
    }
}