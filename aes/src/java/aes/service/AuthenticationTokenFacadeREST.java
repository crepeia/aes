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
import aes.utility.Encrypter;
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
    
    @DELETE
    @Path("anonymous/logout/{token}")
    @Secured
    public Response anonymousLogout(@PathParam("token") String token) throws SQLException {
        Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário anônimo está deslogando do sistema.");
        
        authenticationTokenDAO.revokeAnonymousToken(token, em);
        
        Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.INFO, "Usuário anônimo deslogou do sistema.");
        
        return Response.ok().build();
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
