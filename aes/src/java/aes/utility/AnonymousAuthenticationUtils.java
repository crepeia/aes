package aes.utility;

// Nonce
import java.security.SecureRandom;
import java.util.Base64;

// Signature
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

/**
 *
 * @author LuanBarbs
 */
public class AnonymousAuthenticationUtils {
    private static final SecureRandom random = new SecureRandom(); // Para geração do nonce
    
    // Função para gerar o nonce
    public static String generateNonce(int lengthBytes) {
        byte[] nonceBytes = new byte[lengthBytes];
        random.nextBytes(nonceBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
    }
    
    /**
    * Verificação da assinatura usando BouncyCastle
    *
    * @param publicKey chave pública Base64 decodificada do app
    * @param message (challenge)
    * @param signature assinatura (Base64 decodificada)
    * @return true se assinatura válida, false caso contrário
    */
    public static boolean verifySignature(byte[] publicKey, byte[] message, byte[] signature) {
        try {
            Ed25519PublicKeyParameters pubKeyParams = new Ed25519PublicKeyParameters(publicKey, 0);
            Ed25519Signer verifier = new Ed25519Signer();
            
            verifier.init(false, pubKeyParams);
            verifier.update(message, 0, message.length);
            
            // O método verify() retorna true se a assinatura for válida
            return verifier.verifySignature(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
