package aes.service;

import aes.model.AnonymousKey;
import aes.persistence.AnonymousKeyDAO;
import aes.persistence.NonceDAO;
import aes.utility.NaClUtil;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 *
 * @author luansb
 */

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("anonymous-key")
public class AnonymousKeyServiceFacadeREST {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private AnonymousKeyDAO anonymousKeyDao;
    private NonceDAO nonceDao;
    
    public AnonymousKeyServiceFacadeREST() throws NamingException {
        try {
            anonymousKeyDao = new AnonymousKeyDAO();
            nonceDao = new NonceDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AgendaAvailableFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }

    }
    
    public static class KeyRequest {
        public String publicKey;
        public String instanceId;
        public String clientMeta;
    }
    
    public static class ChallengeRequest {
        public String publicKey;
        public String signature;
        public String nonce;
        public String instanceId;
        public long timestamp;   // timestamp enviado pelo app
        public Map<String, String> clientMeta;
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    
    @Path("insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insert(KeyRequest request) throws SQLException {
        try {
            
            // Verifica se já existe
            AnonymousKey existing = anonymousKeyDao.findByInstanceId(request.instanceId, em);
            if (existing != null) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            
            AnonymousKey object = new AnonymousKey();
            object.setPublicKey(request.publicKey);
            object.setInstanceId(request.instanceId);
            object.setClientMeta(request.clientMeta);
            object.setDateCreated(LocalDateTime.now());
            object.setRevoked(false);
            
            anonymousKeyDao.insert(object, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AgendaAppointmentFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @POST
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateChallenge(ChallengeRequest request) throws SQLException {
        try {
            System.out.println("=== VALIDATE CHALLENGE ===");
            
            // 1. Verifica nonce
            if (!nonceDao.validateNonce(request.nonce)) {
                System.out.println("Nonce inválido ou expirado");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid nonce").build();
            }

            // 2. Monta dados e decodifica
            byte[] pubKeyBytes = Base64.getDecoder().decode(request.publicKey);
            byte[] sigBytes = Base64.getDecoder().decode(request.signature);

            String challengeData = request.nonce + "|" + request.instanceId + "|" + request.timestamp;
            byte[] challengeBytes = challengeData.getBytes(StandardCharsets.UTF_8);
            
            System.out.println("PUBLIC KEY (len=" + pubKeyBytes.length + "): " + request.publicKey);
            System.out.println("SIGNATURE (len=" + sigBytes.length + "): " + request.signature);
            System.out.println("CHALLENGE: " + challengeData);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(challengeBytes);
            System.out.println("CHALLENGE SHA256 (hex): " + bytesToHex(hash));
            
            
            // 3. Verifica assinatura
            boolean verified = NaClUtil.verifySignature(pubKeyBytes, challengeBytes, sigBytes);
            if (!verified) {
                System.out.println("Assinatura inválida");
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid signature").build();
            }
            
            System.out.println("Assinatura válida");

            // 4. grava chave
            AnonymousKey existing = anonymousKeyDao.findByInstanceId(request.instanceId, em);
            if (existing == null) {
                AnonymousKey key = new AnonymousKey();
                key.setInstanceId(request.instanceId);
                key.setPublicKey(request.publicKey);
                System.out.println("KEY: Chegou aqui!");
                // dao.save(key, em);
            }

            // 5. Gerar token anônimo — por enquanto comentado
            // String token = TokenUtil.generateAnonymousToken(request.instanceId);
            
            // Retorna OK
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
