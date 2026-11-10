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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
            if(!isTokenBasedAuthentication(authHeader)){
                abortWithUnauthorized(requestContext);
                return;
            }

            String token = authHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
            if (token.isEmpty()) {
                abortWithUnauthorized(requestContext);
                return;
            }

            // Reuse the validated record: never create a principal after a failed check.
            AuthenticationToken authentication = validateToken(token);
            if (authentication == null) {
                abortWithUnauthorized(requestContext);
                return;
            }
            final User user = authentication.getUser();
            requestContext.setProperty("AUTH_TOKEN", token);

            //OVERRIDING SECURITY CONTEXT
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    // Unbound anonymous tokens intentionally have no user principal.
                    return user == null ? null : () -> String.valueOf(user.getId());
                }

                @Override
                public boolean isUserInRole(String role) {
                    if (user == null || role == null) return false;
                    if ("admin".equalsIgnoreCase(role)) return user.isAdmin();
                    if ("consultant".equalsIgnoreCase(role)) return user.isConsultant();
                    if ("anonymous".equalsIgnoreCase(role)) return user.getEmail() == null;
                    if ("user".equalsIgnoreCase(role)) return user.getEmail() != null;
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext != null && currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return AUTHENTICATION_SCHEME;
                }
            });

        } catch (NoResultException e) {
            abortWithUnauthorized(requestContext);
        } catch (Exception e) {
            // Exception messages may contain SQL parameters, including the token.
            Logger.getLogger(AuthenticationFilter.class.getName()).log(
                    Level.SEVERE, "AUTHENTICATION_UNAVAILABLE exceptionType={0}", e.getClass().getSimpleName());
            requestContext.abortWith(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("AUTHENTICATION_UNAVAILABLE").build());
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
                authorizationHeader.regionMatches(true, 0, AUTHENTICATION_SCHEME + " ", 0,
                        AUTHENTICATION_SCHEME.length() + 1);
    }

    private AuthenticationToken validateToken(String token) {
        // Preserve the existing one-month policy; unknown/expired tokens throw NoResultException.
        LocalDateTime validDateTime = LocalDateTime.now().minusMonths(1);
        Date validDate = Date.from(validDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return (AuthenticationToken) em.createQuery(
                "SELECT a FROM AuthenticationToken a WHERE a.token=:t AND a.dateCreated >= :validDate"
            )
            .setParameter("t", token)
            .setParameter("validDate", validDate)        
            .getSingleResult();
    }   
}
