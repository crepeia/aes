/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;
import aes.utility.SecureRandomString;
import aes.model.AuthenticationToken;
import aes.model.User;
import java.util.Date;

import java.util.List;
import java.util.Random;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
public class AuthenticationTokenFacadeREST extends AbstractFacade<AuthenticationToken> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public AuthenticationTokenFacadeREST() {
        super(AuthenticationToken.class);
    }

    @GET
    @Path("{email}/{password}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response authUser(@PathParam("email") String e, @PathParam("password") String p) {
        try {
            
            User usr = super.login(e, p);
            String token = issueToken(usr);
            
            return Response.ok(token).build();
        } catch(Exception exp) {
            return Response.status(Response.Status.FORBIDDEN).build();
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
