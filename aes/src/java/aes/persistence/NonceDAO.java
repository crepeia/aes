package aes.persistence;

import aes.utility.NonceUtil;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;

/**
 *
 * @author luansb
 */
public class NonceDAO {
    private static final Map<String, Instant> nonceStore = new ConcurrentHashMap<>();
    private static final long ttlSeconds = 60; // tempo de vida de 1 min
    
    public NonceDAO() throws NamingException {
        // não precisa de EntityManager, mas construtor consistente
    }
     
    /** Gera e armazena um nonce temporário */
    public String createNonce() throws SQLException {
        try {
            String nonce = NonceUtil.generateNonce(16);
            nonceStore.put(nonce, Instant.now().plusSeconds(ttlSeconds));
            return nonce;
        } catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
    
    /** Valida e consome um nonce (single-use / uso unico) */
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
}
