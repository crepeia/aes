package aes.persistence;

import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 *
 * @author luansb
 */
public class AppPropertiesDAO extends GenericDAO<Object> {
    public AppPropertiesDAO() throws NamingException {
        super(Object.class);
    }
    
    public String getLatestAppVersion(EntityManager em) throws SQLException {
        try {
            Object result = em.createNativeQuery(
                "SELECT latest_app_version FROM app_properties WHERE id = 1"
            ).getSingleResult();
            return (String) result;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    public int updateLatestAppVersion(String newVersion, EntityManager em) throws SQLException {
        UserTransaction transaction = null;
        boolean started = false;
        try {
            transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            transaction.begin();
            started = true;

            int updatedRows = em.createNativeQuery("UPDATE app_properties SET latest_app_version = :newVersion WHERE id = 1")
                    .setParameter("newVersion", newVersion)
                    .executeUpdate();

            transaction.commit();
            return updatedRows;
        } catch (Exception erro) {
            if (started) {
                try {
                    int status = transaction.getStatus();
                    if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                        transaction.rollback();
                    }
                } catch (Exception rollbackError) {
                    erro.addSuppressed(rollbackError);
                }
            }
            throw new SQLException(erro);
        }
    }
}
