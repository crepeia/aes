/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author luansb
 */
public class NonceStore {
    // Nonce expiração (em ms desde epoch)
    private static final Map<String, Long> nonces = new ConcurrentHashMap<>();
    
    // TTL padrão (5 minutos)
    private static final long TTL_MILLIS = 5 * 60 * 1000;
    
    public static String generateNonce() {
        String nonce = java.util.UUID.randomUUID().toString();
        long expiration = System.currentTimeMillis() + TTL_MILLIS;
        nonces.put(nonce, expiration);
        return nonce;
    }
    
    public static boolean exists(String nonce) {
        Long expiration = nonces.get(nonce);
        if (expiration == null) {
            return false;
        }
        
        // verifica se esta expirado
        if (System.currentTimeMillis() > expiration) {
            nonces.remove(nonce);
            return false;
        }
        return true;
    }
    
     public static void consume(String nonce) {
        nonces.remove(nonce);
    }
}
