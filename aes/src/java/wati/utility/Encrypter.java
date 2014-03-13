/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.utility;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.FacesContext;

/**
 *
 * @author hedersb
 */
public class Encrypter {

	private static final byte[] key = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("key").getBytes();
	private static final SecretKey aesKey = new SecretKeySpec(key, "AES");
	
	private static Cipher aesCipher;

	public Encrypter() {
	}

	/**
	 * @return the aesCipher
	 */
	public static Cipher getAesCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		if (Encrypter.aesCipher == null) {
			Encrypter.aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Encrypter.aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		}
		return Encrypter.aesCipher;
	}

	public static byte[] encrypt(String text) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

		return Encrypter.getAesCipher().doFinal( text.getBytes() );
		
	}
	
	public static boolean compare(String text, byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

		byte[] password = Encrypter.encrypt( text );
		
		if ( password.length == bytes.length ) {
			boolean equals = true;
			int i = 0;
			while (i < password.length && equals) {
				equals &= password[ i ] == bytes[ i ];
				i++;
			}
			return equals;
		} else {
			return false;
		}
		
	}
	
}
