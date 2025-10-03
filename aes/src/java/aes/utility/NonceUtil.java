package aes.utility;

import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 * @author luansb
 */
public class NonceUtil {
    private static final SecureRandom random = new SecureRandom();
    
    /** Gera o nonce **/
    public static String generateNonce(int lengthBytes) {
        byte[] nonceBytes = new byte[lengthBytes];
        random.nextBytes(nonceBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
    }
    
}
