package aes.service;

import aes.model.AnonymousKey;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.AnonymousAuthenticationDAO;
import aes.utility.AnonymousAuthenticationUtils;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author luansb
 */
@Stateless
@Path("anonymous-authentication")
@TransactionManagement(TransactionManagementType.BEAN)
public class AnonymousAuthenticationFacadeREST extends AbstractFacade<AnonymousKey> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private AnonymousAuthenticationDAO anonymousAuthenticationDao;
    private AuthenticationTokenDAO authenticationTokenDao;
    
    public AnonymousAuthenticationFacadeREST() {
        super(AnonymousKey.class);
        try {
            anonymousAuthenticationDao = new AnonymousAuthenticationDAO();
            authenticationTokenDao = new AuthenticationTokenDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    public static class KeyRequest {
        public String publicKey;
        public String instanceId;
        public String clientMeta;
    }
    
    public static class ChallengeRequest {
        public String signature;
        public String nonce;
        public String instanceId;
        public long timestamp;
    }
    
    // ===== INICIO DOS SERVIÇOS DE NONCE =====
    
    @GET
    @Path("nonce/generate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateNonce() {
        try {
            String nonce = anonymousAuthenticationDao.createNonce();
            return Response.ok(nonce).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @POST
    @Path("nonce/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateNonce(String nonce) {
        try {
            if (nonce == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            boolean valid = anonymousAuthenticationDao.validateNonce(nonce);
            return Response.ok("{\"valid\": " + valid + "}").build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    // ===== INICIO DOS SERVIÇOS DE CHAVE =====
    
    @Path("anonymous-key/insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insert(KeyRequest request) throws SQLException {
        try {
            // Verifica se já existe chave para o instanceId
            AnonymousKey existing = anonymousAuthenticationDao.findByInstanceId(request.instanceId, true, em);
            if (existing != null) {
                // Revoga a chave existente
                existing.setRevoked(true);
                anonymousAuthenticationDao.update(existing, em);
            }
            
            // Cria chave nova
            AnonymousKey object = new AnonymousKey();
            object.setPublicKey(request.publicKey);
            object.setInstanceId(request.instanceId);
            object.setClientMeta(request.clientMeta);
            object.setDateCreated(LocalDateTime.now());
            object.setRevoked(false);
            
            anonymousAuthenticationDao.insert(object, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @POST
    @Path("anonymous-key/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateChallenge(ChallengeRequest request) throws SQLException {
        try {
            // 1. Verifica nonce
            if (!anonymousAuthenticationDao.validateNonce(request.nonce)) {
                System.out.println("Nonce inválido ou expirado");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid nonce").build();
            }

            // 2. Monta dados e decodifica
            
            // Buscar publicKey
            AnonymousKey existing = anonymousAuthenticationDao.findByInstanceId(request.instanceId, false, em);
            if (existing == null || existing.getPublicKey() == null) {
                System.out.println("Chave pública não encontrada para o instanceId: " + request.instanceId);
                return Response.status(Response.Status.UNAUTHORIZED).entity("Public key not registered").build();
            }
            
            byte[] pubKeyBytes = Base64.getDecoder().decode(existing.getPublicKey());
            byte[] sigBytes = Base64.getDecoder().decode(request.signature);

            String challengeData = request.nonce + "|" + request.instanceId + "|" + request.timestamp;
            byte[] challengeBytes = challengeData.getBytes(StandardCharsets.UTF_8);
        
            // 3. Verifica assinatura
            boolean verified = AnonymousAuthenticationUtils.verifySignature(pubKeyBytes, challengeBytes, sigBytes);
            if (!verified) {
                System.out.println("Assinatura inválida");
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid signature").build();
            }
           
            // 4. Gerar token anônimo — por enquanto comentado
            String token = authenticationTokenDao.issueAnonymousToken(existing, em);
            
            // Retorna em JSON
            JsonObject response = Json.createObjectBuilder()
                .add("token", token)
                .build();
            
            return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
