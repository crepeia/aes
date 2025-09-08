/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author luansb
 */
public class CryptoUtils {
    public static String hmacSHA256(String data, String key) throws Exception {
        // Inicializa o HMAC com o algoritmo SHA-256.
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        // Gera o hash do HMAC.
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());
        
        // Converte o hash para hexadecimal.
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // Adiciona um zero à esquerda se necessário
            }
            hexString.append(hex);
        }
        
        return hexString.toString(); // Retorna o hash em formato hexadecimal.
    }
}
