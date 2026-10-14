package aes.service;

import aes.model.User;
import aes.persistence.AppPropertiesDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author bruno
 * @author luansb
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("app-properties/")
public class AppPropertiesFacadeREST extends AbstractFacade<Object> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private AppPropertiesDAO appPropertiesDao;
    
    @Inject
    private SecurityContextHelper securityHelper;

    public AppPropertiesFacadeREST() {
        super(Object.class);
        try {
            appPropertiesDao = new AppPropertiesDAO();
        } catch (NamingException ex) {
            Logger.getLogger(AppPropertiesFacadeREST.class.getName())
                .log(Level.SEVERE, "Erro ao inicializar AppPropertiesDAO: ", ex);
        }
    }

    @GET
    @Path("get-latest-app-version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLatestAppVersion() {
        try {
            String currentVersion = appPropertiesDao.getLatestAppVersion(em);
            Logger.getLogger(AppPropertiesFacadeREST.class.getName())
                    .log(Level.FINE, "[CONNECTION] AN APPLICATION IS USING THE SERVER");
            return Response.ok(currentVersion).build();
        } catch (SQLException ex) {
            Logger.getLogger(AppPropertiesFacadeREST.class.getName())
                    .log(Level.SEVERE, "Error fetching app version: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    // Compara duas versões compostas por números separados por ponto (ex: "2.8.2" vs "2.8.1").
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i].trim()) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i].trim()) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
    
    // Atualiza a versão do app no banco de dados para uma nova versão superior.
    @Secured
    @PUT
    @Path("update-latest-app-version")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLatestAppVersion(Map<String, String> payload) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireAdmin(loggedUser);
            if (r != null) return r;

            // Extrai a variável newVersion do corpo JSON
            String newVersion = payload != null ? payload.get("newVersion") : null;

            if (newVersion == null || newVersion.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("NEW_VERSION_REQUIRED").build();
            }

            newVersion = newVersion.trim();

            String currentVersion = appPropertiesDao.getLatestAppVersion(em);

            if (compareVersions(newVersion, currentVersion) <= 0) {
                Logger.getLogger(AppPropertiesFacadeREST.class.getName()).log(
                    Level.WARNING,
                    "[SECURITY/VALIDATION] DENIED_APP_VERSION_UPDATE reason=VERSION_NOT_GREATER " +
                    "actorUserId={0} currentVersion={1} proposedVersion={2}",
                    new Object[]{loggedUser.getId(), currentVersion, newVersion}
                );
                return Response.status(Response.Status.BAD_REQUEST).entity("NEW_VERSION_MUST_BE_GREATER_THAN_CURRENT").build();
            }

            int updatedRows = appPropertiesDao.updateLatestAppVersion(newVersion, em);

            if (updatedRows > 0) {
                Logger.getLogger(AppPropertiesFacadeREST.class.getName()).log(
                    Level.INFO,
                    "[APP_VERSION] Version updated successfully from {0} to {1} by user {2}",
                    new Object[]{currentVersion, newVersion, loggedUser.getId()}
                );
                return Response.ok("VERSION_UPDATED_SUCCESSFULLY").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("UPDATE_FAILED").build();
            }

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("INVALID_VERSION_FORMAT").build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(AppPropertiesFacadeREST.class.getName()).log(Level.SEVERE, "Error updating app version: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    protected EntityManager getEntityManager() {
        return em;
    }
}
