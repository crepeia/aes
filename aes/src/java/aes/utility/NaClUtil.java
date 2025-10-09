package aes.utility;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

/**
 *
 * @author luansb
 */
public class NaClUtil {
    /**
     * Verifica assinatura usando TweetNaClFast
     *
     * @param publicKeyBytes chave pública 32 bytes (Base64 decodificada do app)
     * @param messageBytes   mensagem (challenge)
     * @param signatureBytes assinatura (Base64 decodificada)
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
