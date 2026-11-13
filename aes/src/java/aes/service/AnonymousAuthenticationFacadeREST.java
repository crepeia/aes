package aes.service;

import aes.model.AnonymousKey;
import aes.model.User;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.AnonymousAuthenticationDAO;
import aes.persistence.UserDAO;
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
    private UserDAO userDao;
    
    public AnonymousAuthenticationFacadeREST() {
        super(AnonymousKey.class);
        try {
            anonymousAuthenticationDao = new AnonymousAuthenticationDAO();
            authenticationTokenDao = new AuthenticationTokenDAO();
            userDao = new UserDAO();
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
    public Response insert(KeyRequest request) {
        if (request == null || request.instanceId == null || request.instanceId.trim().isEmpty()
                || request.instanceId.length() > 64 || request.publicKey == null
                || (request.clientMeta != null && request.clientMeta.length() > 64)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
        }

        try {
            if (Base64.getDecoder().decode(request.publicKey).length != 32) {
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
        }

        try {
            anonymousAuthenticationDao.registerKey(request.instanceId, request.publicKey, request.clientMeta, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao registrar chave anônima.", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ANONYMOUS_KEY_REGISTRATION_FAILED").build();
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
                return Response.status(Response.Status.UNAUTHORIZED).entity("PUBLIC_KEY_NOT_FOUND").build();
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
                return Response.status(Response.Status.UNAUTHORIZED).entity("INVALID_SIGNATURE").build();
            }
            
            if (request.userId != null) {
                User user = userDao.find(request.userId, em);
                
                if (user == null || user.getEmail() != null) {
                    Logger.getLogger(AnonymousAuthenticationFacadeREST.class.getName())
                        .log(Level.WARNING,
                             "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE");
                    
                    return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
                }
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
