package aes.persistence;

import aes.model.AnonymousKey;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author luansb
 */
public class AnonymousKeyDAO extends GenericDAO<AnonymousKey> {
    
    public AnonymousKeyDAO() throws NamingException {
        super(AnonymousKey.class);
    }
    
    public AnonymousKey findByInstanceId(String instanceId, EntityManager em) {
        try {
            return em.createQuery("SELECT a FROM AnonymousKey a WHERE a.instanceId = :instanceId", AnonymousKey.class)
                     .setParameter("instanceId", instanceId)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public void revoke(AnonymousKey key, EntityManager em) {
        key.setRevoked(true);
        em.merge(key);
    }
}
