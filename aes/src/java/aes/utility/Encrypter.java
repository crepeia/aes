package aes.utility;


import java.io.UnsupportedEncodingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class Encrypter {

    private static final byte[] key = AppServletContextListener.getServletContext().getInitParameter("key").getBytes();
    private static final SecretKey aesKey = new SecretKeySpec(key, "AES");
    private static final int HASH_ITERATIONS = 10000;
    private static final int HASH_LENGTH_BYTES = 128;
    private static Cipher aesEncryptionCipher;
    private static Cipher aesDecryptionCipher;


    public Encrypter() {
    }

    public static Cipher getAesCipherForEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        if (Encrypter.aesEncryptionCipher == null) {
            Encrypter.aesEncryptionCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Encrypter.aesEncryptionCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        }
        return Encrypter.aesEncryptionCipher;
    }
    
    
    
    public static Cipher getAesCipherForDecryption() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        if (Encrypter.aesDecryptionCipher == null) {
            Encrypter.aesDecryptionCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Encrypter.aesDecryptionCipher.init(Cipher.DECRYPT_MODE, aesKey);
        }
        return Encrypter.aesDecryptionCipher;
    }

    public static byte[] encrypt(String text) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        return Encrypter.getAesCipherForEncryption().doFinal(text.getBytes());

    }
    
    public static String decrypt(String text) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,  IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, DecoderException {
      
       byte[] decrypted = Encrypter.getAesCipherForDecryption().doFinal(Hex.decodeHex(text.toCharArray()));
       return new String(decrypted, "UTF-8");
       
    }
    
    
    
    

    /*  public static byte[] hashString(String password, byte[] salt) throws UnsupportedEncodingException {

        int iterations = 10000;
        int keyLength = 128;
        char[] passwordChars = password.toCharArray();
        //byte[] saltBytes = generateRandomSecureSalt(16);

        byte[] hashedBytes = hashPassword(passwordChars, salt, iterations, keyLength);
        //String hashedString = Hex.encodeHexString(hashedBytes);
        //System.out.println(hashedString);
        //System.out.println(BinaryCodec.toAsciiString(hashedBytes));
 
        return hashedBytes;

    }*/
    public static byte[] generateRandomSecureSalt(int length) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);

        //System.out.println("Salt: " + Hex.encodeHexString(salt));
        return salt;
    }

    public static byte[] hashPassword(final String password, final byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{

       // int iterations = 10000;
        //int keyLength = 128;
        char[] passwordChars = password.toCharArray();

      
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, HASH_ITERATIONS, HASH_LENGTH_BYTES);
        SecretKey generatedKey = skf.generateSecret(spec);
        byte[] generatedKeyBytes = generatedKey.getEncoded();
        return generatedKeyBytes;
      
           
    }

    public static boolean compare(String text, byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        byte[] password = Encrypter.encrypt(text);

        if (password.length == bytes.length) {
            boolean equals = true;
            int i = 0;
            while (i < password.length && equals) {
                equals &= password[i] == bytes[i];
                i++;
            }
            return equals;
        } else {
            return false;
        }

    }

    public static boolean compareHash(String providedPassword,  byte[] expectedHash, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{

        byte[] password = Encrypter.hashPassword(providedPassword, salt);

        if (password.length == expectedHash.length) {
            boolean equals = true;
            int i = 0;
            while (i < password.length && equals) {
                equals &= password[i] == expectedHash[i];
                i++;
            }
            return equals;
        } else {
            return false;
        }

    }

}
