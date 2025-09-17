/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.BufferedReader;

/**
 *
 * @author luansb
 */
public class PemUtils {
    public static PublicKey readPublicKeyFromPem(String pem) throws Exception {
        // Remove cabecalho e rodape
        StringBuilder sb = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new StringReader(pem))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains("BEGIN") && !line.contains("END")) {
                    sb.append(line.trim());
                }
            }
        }
        
        byte[] keyBytes = Base64.getDecoder().decode(sb.toString());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
