package aes.persistence;

import aes.model.AnonymousKey;
import aes.utility.AnonymousAuthenticationUtils;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 *
 * @author LuanBarbs
 */
public class AnonymousAuthenticationDAO extends GenericDAO<AnonymousKey> {
    
    // Para nonce
    private static final Map<String, Instant> nonceStore = new ConcurrentHashMap<>();
    private static final long ttlSeconds = 60; // tempo de vida de 1 min
    private final UserTransaction keyTransaction;
    
    // Para chave
    
    public AnonymousAuthenticationDAO() throws NamingException {
        super(AnonymousKey.class);
        keyTransaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
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
    public AnonymousKey findByInstanceId(String instanceId, boolean includeRevoked, EntityManager em) {
        try {
            String jpql = "SELECT a FROM AnonymousKey a WHERE a.instanceId = :instanceId";
            
            if (!includeRevoked) {
                jpql += " AND a.revoked = false";
            }
            
            jpql += " ORDER BY a.dateCreated DESC, a.id DESC";
            
            return em.createQuery(jpql, AnonymousKey.class)
                     .setParameter("instanceId", instanceId)
                     .setMaxResults(1)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public void revoke(AnonymousKey key, EntityManager em) {
        key.setRevoked(true);
        em.merge(key);
    }
    
    public void registerKey(String instanceId, String publicKey, String clientMeta, EntityManager em) throws SQLException {
        boolean transactionStarted = false;

        try {
            keyTransaction.begin();
            transactionStarted = true;

            List<AnonymousKey> activeKeys = em.createQuery("SELECT a FROM AnonymousKey a WHERE a.instanceId = :instanceId AND a.revoked = false", AnonymousKey.class)
                    .setParameter("instanceId", instanceId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();

            for (AnonymousKey key : activeKeys) {
                key.setRevoked(true);
            }

            em.flush();

            AnonymousKey key = new AnonymousKey();
            key.setInstanceId(instanceId);
            key.setPublicKey(publicKey);
            key.setClientMeta(clientMeta);
            key.setDateCreated(LocalDateTime.now());
            key.setRevoked(false);

            em.persist(key);
            keyTransaction.commit();
        } catch (Exception ex) {
            if (transactionStarted) {
                try {
                    int status = keyTransaction.getStatus();
                    if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                        keyTransaction.rollback();
                    }
                } catch (Exception rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }

            throw new SQLException("Erro ao registrar chave anônima.", ex);
        }
    }
}
