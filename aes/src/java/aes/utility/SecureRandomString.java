package aes.utility;

import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

public class SecureRandomString {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generate() {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        
        return Base64.encodeBase64URLSafeString(buffer);
    }
}
