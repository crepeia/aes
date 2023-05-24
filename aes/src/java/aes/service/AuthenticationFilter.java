/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.utility.Secured;
import aes.model.AuthenticationToken;
import aes.model.User;

import java.security.Principal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author bruno
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    
    private static final String REALM = "aes";
    private static final String AUTHENTICATION_SCHEME = "Bearer";
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    
    
    public AuthenticationFilter() {
    }    
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        
        try {
            
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.INFO, new StringBuilder("Creating an authentication for: ").append(authHeader).toString());
            if(!isTokenBasedAuthentication(authHeader)){
                abortWithUnauthorized(requestContext);
                Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, new StringBuilder("This is not a token based authentication: ").append(authHeader).toString());
                return;
            }

            String token = authHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

            try {
                validateToken(token);
            } catch (SQLException e) {
                Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, new StringBuilder("Error when validating the following token: ").append(token).toString());
                Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, e);
                abortWithUnauthorized(requestContext);
            } catch(Exception e){
                Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, new StringBuilder("Error when validating the following token: ").append(token).toString());
                Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, e);
                abortWithUnauthorized(requestContext);
            }

            //OVERRIDING SECURITY CONTEXT
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    try{
                        User usr = (User) em.createQuery("SELECT a.user FROM AuthenticationToken a WHERE a.token=:t").setParameter("t", token).getSingleResult();
                        return () -> usr.getEmail();
                    }catch(Exception e) {
                        Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, e);
                        return null;
                    }

                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return AUTHENTICATION_SCHEME;
                }
            });

            Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.INFO, "AuthenticationFilter.filter executed with success.");
        } catch (Exception e) {
            Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, "Error when executing AuthenticationFilter.filter.");
            Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, e);
        }
    }
   
    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, 
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }
    
    
    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        // Check if the Authorization header is valid
        return authorizationHeader != null && 
                authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private AuthenticationToken validateToken(String token) throws Exception {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
        return (AuthenticationToken) em.createQuery("SELECT a FROM AuthenticationToken a WHERE a.token=:t").setParameter("t", token).getSingleResult();
    }
    
   
}
