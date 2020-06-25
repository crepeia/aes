/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;
import aes.utility.SecureRandomString;
import aes.model.AuthenticationToken;
import aes.model.User;
import aes.utility.Secured;
import java.util.Date;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author bruno
 */
@Stateless
@Path("authenticate")
public class AuthenticationTokenFacadeREST extends AbstractFacade<AuthenticationToken> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    @Context
    SecurityContext securityContext;

    public AuthenticationTokenFacadeREST() {
        super(AuthenticationToken.class);
    }

    @GET
    @Path("{email}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authUser(@PathParam("email") String e, @PathParam("password") String p) {
        try {
            
            User usr = super.login(e, p);
            String token = issueToken(usr);
            
            //String jsonString = new JSONObject().put("token", token).toString();

            return Response.ok(token).build();
        } catch(Exception exp) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
    
    @DELETE
    @Path("secured/logout/{token}")
    @Secured
    public Response logout(@PathParam("token") String token) {
        try {
        String userEmail = securityContext.getUserPrincipal().getName();//httpRequest.getAttribute("userEmail").toString();
        
            
        AuthenticationToken at = (AuthenticationToken) getEntityManager().createQuery("SELECT at FROM AuthenticationToken at WHERE at.token=:token AND at.user.email=:uEmail")
                .setParameter("token", token)
                .setParameter("uEmail", userEmail)
                .getSingleResult();
        super.remove(at);
        
        return Response.ok().build();
        } catch( NoResultException e ) {
            return Response.ok().build(); //se o token não existe ou já foi deletado ignoro o erro
        }catch(Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String issueToken(User usr){
        String token = SecureRandomString.generate();
        
        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken(token);
        authToken.setUser(usr);
        authToken.setDateCreated(new Date());
        super.create(authToken);
        
        return token;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
