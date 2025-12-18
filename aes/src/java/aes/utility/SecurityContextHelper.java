package aes.utility;

import aes.model.User;
import aes.persistence.UserDAO;
import java.util.Objects;
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
        return user == null || user.getEmail() == null;
    }
    
    // Usuário autenticado não anônimo (padrão)
    public Response requireAuthenticatedUser() {
        User user = getLoggedUser();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (isAnonymous(user)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return null;
    }
    
    // Usuário autenticado (padrão, consultor ou anônimo)
    public Response requireAnyAuthenticated() {
        User user = getLoggedUser();
        
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        return null;
    }
    
    // Usuário anônimo autenticado
    public Response requireAuthenticatedAnonymous() {
        User user = getLoggedUser();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!isAnonymous(user)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return null;
    }
    
    public Response requireConsultant(User user) {
        if (!user.isConsultant()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }
    
    public Response requireRegularUser(User user) {
        if (user.isConsultant()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }
    
    public Response requireSameUser(User user, Long userId) {
        if (!Objects.equals(user.getId(), userId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }
    
    public Response requireSameConsultant(User user, Long consultantId) {
        if (!Objects.equals(user.getId(), consultantId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }
    
    public boolean isValidUserConsultantRelation(User user, User consultant) {
        return user.getRelatedConsultant() != null &&
               Objects.equals(user.getRelatedConsultant().getId(), consultant.getId());
    }
    
    public Response requireRegularSameUser(Long userId) {
        User user = getLoggedUser();

        Response r = requireAuthenticatedUser();
        if (r != null) return r;

        if (user.isConsultant()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (!Objects.equals(user.getId(), userId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return null;
    }
    
    public Response requireConsultantSameUser(Long userId) {
        User user = getLoggedUser();

        Response r = requireAuthenticatedUser();
        if (r != null) return r;

        if (!user.isConsultant()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (!Objects.equals(user.getId(), userId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return null;
    }
}
