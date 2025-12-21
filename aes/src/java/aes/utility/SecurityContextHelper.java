package aes.utility;

import aes.model.User;
import aes.persistence.UserDAO;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author LuanBarbs
 */

@ApplicationScoped
public class SecurityContextHelper {
    @Context
    private SecurityContext securityContext;

    @Inject
    private UserDAO userDao;

    @PersistenceContext
    private EntityManager em;
    
    public User getLoggedUser() {
        if (securityContext.getUserPrincipal() == null) {
            return null;
        }
        Long userId = Long.valueOf(securityContext.getUserPrincipal().getName());
        return userDao.find(userId, em);
    }
    
    public boolean isAnonymous(User user) {
        return user != null || user.getEmail() == null;
    }
    
    // Usuário autenticado não anônimo (padrão)
    public Response requireAuthenticatedUser() {
        User user = getLoggedUser();

        if (user == null) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=NOT_AUTHENTICATED");
            return Response.status(Response.Status.UNAUTHORIZED).entity("NOT_AUTHENTICATED").build();
        }

        if (isAnonymous(user)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=ANONYMOUS_USER userId={0}",
                     user.getId());
            return Response.status(Response.Status.FORBIDDEN).entity("ANONYMOUS_USER").build();
        }

        return null;
    }
    
    // Usuário autenticado (padrão, consultor ou anônimo)
    public Response requireAnyAuthenticated() {
        User user = getLoggedUser();
        
        if (user == null) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=NOT_AUTHENTICATED");
            return Response.status(Response.Status.UNAUTHORIZED).entity("NOT_AUTHENTICATED").build();
        }
        
        return null;
    }
    
    // Usuário anônimo autenticado
    public Response requireAuthenticatedAnonymous() {
        User user = getLoggedUser();

        if (user == null) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=NOT_AUTHENTICATED");
            return Response.status(Response.Status.UNAUTHORIZED).entity("NOT_AUTHENTICATED").build();
        }

        if (!isAnonymous(user)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE");
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
        }

        return null;
    }
    
    public Response requireConsultant(User user) {
        if (!user.isConsultant()) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE loggedUserId={0}",
                     user.getId());
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
        }
        return null;
    }
    
    public Response requireRegularUser(User user) {
        if (user.isConsultant()) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE loggedUserId={0}",
                     user.getId());
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
        }
        return null;
    }
    
    public Response requireSameUser(User user, Long userId) {
        if (!Objects.equals(user.getId(), userId)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER paramUserId={0} loggedUserId={1}",
                     new Object[]{ userId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
        }
        return null;
    }
    
    public Response requireSameConsultant(User user, Long consultantId) {
        if (!Objects.equals(user.getId(), consultantId)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER paramUserId={0} loggedUserId={1}",
                     new Object[]{ consultantId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
        }
        return null;
    }
    
    public boolean isValidUserConsultantRelation(User user, User consultant) {
        boolean valid = user.getRelatedConsultant() != null &&
               Objects.equals(user.getRelatedConsultant().getId(), consultant.getId());
        
        if (!valid) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_CONSULTANT_RELATION userId={0} consultantId={1}",
                     new Object[]{ user.getId(), consultant.getId() });
        }
        
        return valid;
    }
    
    public Response requireRegularSameUser(Long userId) {
        User user = getLoggedUser();

        Response r = requireAuthenticatedUser();
        if (r != null) return r;

        if (user.isConsultant()) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE paramUserId={0} loggedUserId={1}",
                     new Object[]{ userId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
        }

        if (!Objects.equals(user.getId(), userId)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER paramUserId={0} loggedUserId={1}",
                     new Object[]{ userId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
        }

        return null;
    }
    
    public Response requireConsultantSameUser(Long userId) {
        User user = getLoggedUser();

        Response r = requireAuthenticatedUser();
        if (r != null) return r;

        if (!user.isConsultant()) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=INVALID_USER_ROLE paramUserId={0} loggedUserId={1}",
                     new Object[]{ userId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_ROLE").build();
        }

        if (!Objects.equals(user.getId(), userId)) {
            Logger.getLogger(SecurityContextHelper.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER paramUserId={0} loggedUserId={1}",
                     new Object[]{ userId, user.getId() });
            return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
        }

        return null;
    }
}
