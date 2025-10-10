package aes.persistence;

import aes.model.AnonymousKey;
import aes.utility.AnonymousAuthenticationUtils;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author luansb
 */
public class AnonymousAuthenticationDAO extends GenericDAO<AnonymousKey> {
    
    // Para nonce
    private static final Map<String, Instant> nonceStore = new ConcurrentHashMap<>();
    private static final long ttlSeconds = 60; // tempo de vida de 1 min
    
    // Para chave
    
    public AnonymousAuthenticationDAO() throws NamingException {
        super(AnonymousKey.class);
    }
    
    // ===== INICIO DOS SERVIÇOS DE NONCE =====
    
    /** Gera e armazena um nonce temporário
    * @return 
    * @throws java.sql.SQLException */
    public String createNonce() throws SQLException {
        try {
            String nonce = AnonymousAuthenticationUtils.generateNonce(16);
            nonceStore.put(nonce, Instant.now().plusSeconds(ttlSeconds));
            return nonce;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
    
    /** Valida e consome um nonce (single-use / uso unico)
    * @param nonce
    * @return 
    * @throws java.sql.SQLException */
    public boolean validateNonce(String nonce) throws SQLException {
        try {
            Instant expiry = nonceStore.get(nonce);
            if (expiry == null) return false;
            if (Instant.now().isAfter(expiry)) {
                nonceStore.remove(nonce);
                return false;
            }
            nonceStore.remove(nonce);
            return true;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
    
    // ===== INICIO DOS SERVIÇOS DE CHAVE =====
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
