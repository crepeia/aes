package aes.persistence;

import aes.model.ChatbotInteraction;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author luansb
 */
public class ChatbotInteractionDAO extends GenericDAO<ChatbotInteraction> {
    public ChatbotInteractionDAO() throws NamingException {
        super(ChatbotInteraction.class);
    }
    
    public List<ChatbotInteraction> listByConsultant(Long consultantId, EntityManager entityManager) throws SQLException {
        try {
            Query query = entityManager.createQuery(
                "SELECT ci FROM ChatbotInteraction ci "
              + "WHERE ci.consultor.id = :consultantId "
              + "ORDER BY ci.date_request DESC"
            );
            query.setParameter("consultantId", consultantId);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
}
