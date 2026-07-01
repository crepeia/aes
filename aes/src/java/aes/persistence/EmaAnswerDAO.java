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