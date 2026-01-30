package aes.utility;

import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class Encrypter {

    private static final byte[] key = AppServletContextListener.getServletContext().getInitParameter("key").getBytes();
    private static final SecretKey aesKey = new SecretKeySpec(key, "AES");
    
    
    private static final int HASH_ITERATIONS = 10000;
    private static final int HASH_LENGTH_BYTES = 128; 

    public Encrypter() {
    }

    public static byte[] encrypt(String text) throws EncrypterException {
        try {
            // Cria uma instância nova por requisição (Thread Safe)
            // Mantendo AES/ECB para compatibilidade com seus dados atuais
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return cipher.doFinal(text.getBytes("UTF-8")); // Forçando UTF-8 para evitar erro de encoding
        } catch (Exception ex) {
            throw new EncrypterException(ex);
        }
    }

    public static String decrypt(String text) throws EncrypterException {
        try {
            // Cria uma instância nova por requisição (Thread Safe)
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            
            byte[] decrypted = cipher.doFinal(Hex.decodeHex(text.toCharArray()));
            return new String(decrypted, "UTF-8");
        } catch (Exception ex) {
            throw new EncrypterException(ex);
        }
    }

    public static byte[] hashPassword(final String password, final byte[] salt) throws EncrypterException {
        try {
            char[] passwordChars = password.toCharArray();
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, HASH_ITERATIONS, HASH_LENGTH_BYTES);
            SecretKey generatedKey = skf.generateSecret(spec);
            return generatedKey.getEncoded();

        } catch (Exception ex) {
            throw new EncrypterException(ex);
        }
    }

    //Comparação segura contra Timing Attacks
    public static boolean compareHash(String providedPassword, byte[] expectedHash, byte[] salt) throws EncrypterException {
        try {
            byte[] generatedHash = Encrypter.hashPassword(providedPassword, salt);
            
            // Verifica primeiro se o tamanho bate (para evitar erro no MessageDigest)
            if (generatedHash.length != expectedHash.length) {
                return false;
            }
            
            // MessageDigest.isEqual compara de forma segura
            return MessageDigest.isEqual(generatedHash, expectedHash);

        } catch (Exception ex) {
            throw new EncrypterException(ex);
        }
    }

    // Mantido para compatibilidade, usando a lógica segura de comparação
    public static boolean compare(String text, byte[] bytes) throws EncrypterException {
        try {
            byte[] password = Encrypter.encrypt(text);
            return MessageDigest.isEqual(password, bytes);
        } catch (Exception ex) {
            throw new EncrypterException(ex);
        }
    }

    public static byte[] generateRandomSecureSalt(int length) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }
}