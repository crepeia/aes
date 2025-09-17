/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;
import aes.model.AuthenticationToken;
import aes.model.User;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.UserDAO;
import aes.utility.CryptoUtils;
import aes.utility.Encrypter;
import aes.utility.NonceStore;
import aes.utility.PemUtils;
import aes.utility.Secured;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.core.Request;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import java.util.Collections;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;


/**
 *
 * @author bruno
 */
@Stateless
@Path("authenticate")
@TransactionManagement(TransactionManagementType.BEAN)
public class AuthenticationTokenFacadeREST extends AbstractFacade<AuthenticationToken> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private UserDAO userDAO;
    private AuthenticationTokenDAO authenticationTokenDAO;

    @Context
    SecurityContext securityContext;

    public AuthenticationTokenFacadeREST() {
        super(AuthenticationToken.class);
        try {
            userDAO = new UserDAO();
            authenticationTokenDAO = new AuthenticationTokenDAO();
        } catch (NamingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("{email}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authUser(@PathParam("email") String e, @PathParam("password") String p) throws InvalidKeyException {

        try {
            String clientEncriptedHexPassword = p;
            String decriptedPassword = Encrypter.decrypt(clientEncriptedHexPassword);
            
            User user = userDAO.checkCredentials(e, decriptedPassword, em);
           
            if(user != null){
               AuthenticationToken authToken = authenticationTokenDAO.issueToken(user, em);
               
               Map<String, Object> responseToken = new HashMap<>();
               responseToken.put("token", authToken.getToken());
               responseToken.put("dateCreated", authToken.getDateCreated());
               
               Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' logou no sistema.");
               return Response.ok(responseToken).build();
            } else {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' não conseguiu logar.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception exp) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, null, exp);
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário '" + e + "' não conseguiu logar.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @DELETE
    @Path("secured/logout/{token}")
    @Secured
    public Response logout(@PathParam("token") String token) {
        try {
            String userEmail = securityContext.getUserPrincipal().getName();

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, new StringBuffer(userEmail).append(" está deslogando do sistema.").toString());

            authenticationTokenDAO.revokeToken(token, userEmail, em);

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, new StringBuffer(userEmail).append(" deslogou do sistema.").toString());

            return Response.ok().build();
        } catch (NoResultException e) {
            return Response.ok().build(); //se o token não existe ou já foi deletado ignoro o erro
        } catch (SQLException e) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, null, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("secured/refreshtoken")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(@HeaderParam("Authorization") String tokenHeader) throws SQLException{
        try {
            // Extrai o token (remove "Bearer " se vier no header)
            String tokenString = tokenHeader.replace("Bearer ", "").trim();
            
            // Busca o token no banco
            AuthenticationToken existingToken = authenticationTokenDAO.findByToken(tokenString, em);
            if (existingToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid token").build();
            }
            
            User user = existingToken.getUser();
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("User not found").build();
            }
            
            // Atualiza o token existente
            AuthenticationToken newToken = authenticationTokenDAO.updateToken(tokenString, user.getEmail(), em);
            Map<String, Object> responseToken = new HashMap<>();
            responseToken.put("token", newToken.getToken());
            responseToken.put("dateCreated", newToken.getDateCreated());
            
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                .log(Level.INFO, "Usuário '" + user.getEmail() + "' renovou token.");
            
            return Response.ok(responseToken).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao renovar token").build();
        }
    }
    
    @GET
    @Path("anonymous/nonce")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateNonce() {
        String nonce = NonceStore.generateNonce();
        return Response.ok("{\"nonce\":\"" + nonce + "\"}").build();
    }
    
    @POST
    @Path("anonymous")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateAnonymous(Map<String, String> payload) throws SignatureException, InvalidKeyException {
        try {
            String nonce = payload.get("nonce");
            String signatureBase64 = payload.get("signature");
            
            // Validar se o nonce existe e não foi usado
            if (!NonceStore.exists(nonce)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid nonce").build();
            }
            
            // Carregar chave pública
            String publicKeyPem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlVE81//D5jqcEvZ6bXDa\n917HOiKPmD3pRHkyL9sFIzQLaciHxoywKhcAi5y3g8PSbxfuhtwzcdFUkJgCf+l+\nYD3H6lBucO+YWza++BiRuPVWhZQ5UrIiViL+R5T6LFM8OIeuGEy1sgBu3mN7aWXp\nflKPAU1R8twAm1WTR2nU9R33coiJgDnFpXgOHN1UMRn04ua1TN/lWeAITRDhFBzE\n5s7Trwf1o+gZvaVJVtlsWOjJzDc4ltFv2WozCWjvnoRGM/i6boVqHRNko+fhfvWg\noqFkQjF0pi8b/qJmAf4m8VsdBjbDAA15T9Px37Nqyxnom1gmwsu3gdTBuu3BdZKT\nGQIDAQAB\n-----END PUBLIC KEY-----";
            System.out.println(publicKeyPem);
            PublicKey publicKey = PemUtils.readPublicKeyFromPem(publicKeyPem);
            
            // Validar assinatura
            byte[] signature = Base64.getDecoder().decode(signatureBase64);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(nonce.getBytes(StandardCharsets.UTF_8));
            
            if (!sig.verify(signature)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid signature").build();
            }
            
            // Marcar nonce como usado
            NonceStore.consume(nonce);
            
            String token = authenticationTokenDAO.issueAnonymousToken("anon", em);
            return Response.ok("{\"token\":\"" + token + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error").build();
        }
    }
    
    @DELETE
    @Path("anonymous/logout/{token}")
    @Secured
    public Response anonymousLogout(@PathParam("token") String token) throws SQLException {
        Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário anônimo está deslogando do sistema.");
        
        authenticationTokenDAO.revokeAnonymousToken(token, em);
        
        Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário anônimo deslogou do sistema.");
        
        return Response.ok().build();
    }
    
    @GET
    @Path("anonymous/{clientKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateClientKey(@PathParam("clientKey") String clientKey) {
        try {
            // Dividir o clientKey em duas partes: rawKey e signature.
            String[] parts = clientKey.split("-");
            if(!(parts.length == 3)) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "ClientKey invalido.");
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid clientKey").build();
            }
            
            // Reconstroi os elementos.
            String rawKey = parts[0] + "-" + parts[1];
            String signature = parts[2];
            
            // Validar a assinatura usando a mesma SECRET_KEY do aplicativo.
            String expectedSignature = CryptoUtils.hmacSHA256(rawKey, "(uf0&p85F^nAY&MlRW:4;Ld(Q");
            if (!signature.equals(expectedSignature)) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Assinatura invÃ¡lida para o clientKey.");
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid signature").build();
            }
            
            // Gerar token anonimo temporario.
            String anonymousToken = authenticationTokenDAO.issueAnonymousToken(rawKey, em);
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "ClientKey validado com sucesso.");
            return Response.ok("{\"token\":\"" + anonymousToken + "\"}").build();
        } catch (Exception e) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, "Erro ao validar clientKey.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal error").build();
        }
    }
    
    /*private String issueToken(User usr){
        String token = SecureRandomString.generate();
        
        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken(token);
        authToken.setUser(usr);
        authToken.setDateCreated(new Date());
        super.create(authToken);
        
        return token;
    }*/
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
