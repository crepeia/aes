package aes.persistence;

import aes.model.Challenge;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
/**
 * @Deprecated
 * This class is no longer in use, because challenge is not in the databank anymore.
 * Challenge is in .properties file and should be used from there, instead.
**/
@Deprecated
public class ChallengeDAO extends GenericDAO<Challenge> {
        
    public ChallengeDAO() throws NamingException {
        super(Challenge.class);
    }

    public List<Challenge> findAll(EntityManager entityManager) throws SQLException {
        return super.list(entityManager);
    }
    
    public List<Challenge> findAllByType(Challenge.ChallengeType ct, EntityManager entityManager) {
        return entityManager.createQuery("SELECT c FROM Challenge c WHERE c.type=:type")
                .setParameter("type", ct)
                .getResultList();
    }
}
