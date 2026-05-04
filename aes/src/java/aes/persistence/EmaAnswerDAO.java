package aes.persistence;

import aes.model.EmaAnswer;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.sql.SQLException;

public class EmaAnswerDAO extends GenericDAO<EmaAnswer> {

    public EmaAnswerDAO() throws NamingException {
        super(EmaAnswer.class);
    }

    public void saveAnswer(EmaAnswer answer, EntityManager entityManager) throws SQLException {
        // Aproveitamos o método insertOrUpdate que já existe no seu GenericDAO
        super.insertOrUpdate(answer, entityManager);
    }
    
    public boolean checkSeJaRespondeu(Long userId, String periodDate, EntityManager em) {
        try {
            // periodDate format: "yyyy-M-d_period" e.g. "2025-5-4_manha"
            // We split to get the date part and the period (turn) separately
            String[] parts = periodDate.split("_");
            if (parts.length != 2) return false;
            String datePart = parts[0]; // e.g. "2025-5-4"
            String period   = parts[1]; // e.g. "manha"

            String[] dateParts = datePart.split("-");
            int year  = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]); // 1-based
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
                .setParameter("year",  year)
                .setParameter("month", month)
                .setParameter("day",   day)
                .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            System.err.println("Erro ao verificar resposta no DAO: " + e.getMessage());
            return false;
        }
    }
}