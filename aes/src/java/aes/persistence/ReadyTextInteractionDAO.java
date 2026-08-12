package aes.persistence;
 
import aes.model.ReadyTextInteraction;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
 
/**
 *
 * @author luansb
 */
public class ReadyTextInteractionDAO extends GenericDAO<ReadyTextInteraction> {
 
    public ReadyTextInteractionDAO() throws NamingException {
        super(ReadyTextInteraction.class);
    }
 
    public List<ReadyTextInteraction> listByConsultant(Long consultantId, EntityManager entityManager) throws SQLException {
        try {
            Query query = entityManager.createQuery(
                "SELECT rt FROM ReadyTextInteraction rt "
              + "WHERE rt.consultor.id = :consultantId "
              + "ORDER BY rt.dateUsed DESC"
            );
            query.setParameter("consultantId", consultantId);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
}