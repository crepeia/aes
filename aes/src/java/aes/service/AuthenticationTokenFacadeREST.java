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
import aes.utility.EncrypterException;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @Inject
    private SecurityContextHelper securityHelper;

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
    public Response authUser(@PathParam("email") String email, @PathParam("password") String encryptedPassword) {
        try {
            if (email == null || email.isEmpty() || encryptedPassword == null || encryptedPassword.isEmpty()) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_AUTHENTICATION reason=INVALID_DATA");

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            String decryptedPassword;
            
            try {
                decryptedPassword = Encrypter.decrypt(encryptedPassword);
            } catch (EncrypterException ex) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_AUTHENTICATION reason=INVALID_DATA email={0}", email);

                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_DATA").build();
            }
            
            User user = userDAO.checkCredentials(email, decryptedPassword, em);
            
            if (user == null) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] DENIED_AUTHENTICATION reason=INVALID_DATA email={0}", email);

                return Response.status(Response.Status.FORBIDDEN).entity("INVALID_DATA").build();
            }
            
            AuthenticationToken authToken = authenticationTokenDAO.issueToken(user, em);
               
            Map<String, Object> responseToken = new HashMap<>();
            responseToken.put("token", authToken.getToken());
            responseToken.put("dateCreated", authToken.getDateCreated());
            
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                .log(Level.INFO,"[SECURITY] AUTHENTICATION_SUCCESS userId={0}", user.getId());
               
            return Response.ok(responseToken).build();
        } catch (EncrypterException | SQLException ex) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @DELETE
    @Path("secured/logout")
    @Secured
    public Response logout() {
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            String token = securityHelper.getToken();
            
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                .log(Level.INFO, "[INFO] USER_IS_TRYING_TO_LOGOUT " + "actorUserId={0}", loggedUser.getId());
            
            authenticationTokenDAO.revokeToken(token, loggedUser.getId(), em);

            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                .log(Level.INFO, "[INFO] USER_WAS_ABLE_TO_LOGOUT " + "actorUserId={0}", loggedUser.getId());

            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @POST
    @Path("secured/refreshtoken")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken() throws SQLException{
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();
            
            String token = securityHelper.getToken();
            
            AuthenticationToken existingToken = authenticationTokenDAO.findByToken(token, em);
            
            if (existingToken == null) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURED] TOKEN_NOT_FOUND_IN_DB reason=INVALID_TOKEN " + "actorUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_TOKEN").build();
            }
            
            User user = existingToken.getUser();
            
            if (user == null) {
                Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                    .log(Level.WARNING, "[SECURED] USER_NOT_FOUND reason=TARGET_USER_NOT_FOUND " + "actorUserId={0}", loggedUser.getId());
                
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_USER_NOT_FOUND").build();
            }
            
            r = securityHelper.requireSameUser(user, loggedUser.getId());
            if (r != null) return r;
            
            AuthenticationToken newToken = authenticationTokenDAO.updateToken(token, loggedUser.getId(), em);
            Map<String, Object> responseToken = new HashMap<>();
            responseToken.put("token", newToken.getToken());
            responseToken.put("dateCreated", newToken.getDateCreated());
            
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName())
                .log(Level.INFO, "[INFO] USER_WAS_ABLE_TO_REFRESH_TOKEN " + "actorUserId={0}", loggedUser.getId());
            
            return Response.ok(responseToken).build();
        } catch (SQLException ex) {
            Logger.getLogger(AuthenticationTokenFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
