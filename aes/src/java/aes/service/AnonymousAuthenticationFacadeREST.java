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
 * @author LuanBarbs
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
        public Long userId;
    }
    
    // ===== SERVIÇOS DE NONCE =====
    
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
    
    // ===== SERVIÇOS DE CHAVE =====
    
    @Path("anonymous-key/insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insert(KeyRequest request) throws SQLException {
        try {
            if (request == null ||
                    request.instanceId == null ||
                    request.publicKey == null) {
                
                Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_ANONKEY_INSERT reason=INVALID_DATA");
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            // Verifica se já existe chave para o instanceId
            AnonymousKey existing = anonymousAuthenticationDao.findByInstanceId(request.instanceId, true, em);
            if (existing != null) {
                // Revoga a chave existente
                existing.setRevoked(true);
                anonymousAuthenticationDao.update(existing, em);
            }
            
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
            if (!anonymousAuthenticationDao.validateNonce(request.nonce)) {
                Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_VALIDADE_NONCE reason=INVALID_DATA");
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }

            AnonymousKey existing = anonymousAuthenticationDao.findByInstanceId(request.instanceId, false, em);
            if (existing == null || existing.getPublicKey() == null) {
                Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] PUBLIC_KEY_NOT_FOUND reason=INVALID_DATA"
                        + "requestInstanceId={0}",
                        request.instanceId);
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_DATA").build();
            }
            
            byte[] pubKeyBytes = Base64.getDecoder().decode(existing.getPublicKey());
            byte[] sigBytes = Base64.getDecoder().decode(request.signature);

            String challengeData = request.nonce + "|" + request.instanceId + "|" + request.timestamp;
            byte[] challengeBytes = challengeData.getBytes(StandardCharsets.UTF_8);
            
            boolean verified = AnonymousAuthenticationUtils.verifySignature(pubKeyBytes, challengeBytes, sigBytes);
            if (!verified) {
                Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_SIGNATURE reason=INVALID_DATA");
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_DATA").build();
            }
           
            String token = authenticationTokenDao.issueAnonymousToken(existing, request.userId, em);
            
            JsonObject response = Json.createObjectBuilder()
                .add("token", token)
                .build();
            
            return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
        } catch (SQLException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
