package aes.service;

import aes.model.ChatbotInteraction;
import aes.model.User;
import aes.persistence.ChatbotInteractionDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.sql.SQLException;
import java.util.Objects;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author luansb
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("chatbotinteraction")
public class ChatbotInteractionFacadeREST extends AbstractFacade<ChatbotInteraction> {
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ChatbotInteractionDAO chatbotDao;
    
    @Inject
    private SecurityContextHelper securityHelper;
    
    public ChatbotInteractionFacadeREST() {
        super(ChatbotInteraction.class);
        try {
            chatbotDao = new ChatbotInteractionDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private boolean canAccessInteraction(User loggedUser, ChatbotInteraction interaction) {
        if (loggedUser == null || interaction == null || interaction.getConsultor() == null) {
            return false;
        }
        return Objects.equals(interaction.getConsultor().getId(), loggedUser.getId());
    }
    
    private Response validateInteractionAccess(User loggedUser, ChatbotInteraction interaction) {
        if (!canAccessInteraction(loggedUser, interaction)) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName())
                .log(Level.WARNING,
                     "[SECURITY] DENIED_CHATBOT_ACCESS reason=INVALID_USER_OBJECT_RELATION "
                   + "actorUserId={0} interactionId={1}",
                     new Object[]{loggedUser.getId(), interaction.getId()});
            return Response.status(Response.Status.FORBIDDEN).entity("INVALID_USER_OBJECT_RELATION").build();
        }
        return null;
    }
    
    @Path("insert")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(ChatbotInteraction interaction) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;

            interaction.setConsultor(loggedUser);

            chatbotDao.insert(interaction, em);

            return Response.status(Response.Status.CREATED).entity(interaction.getId()).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @Path("update")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response update(ChatbotInteraction interaction) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;

            if (interaction.getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("MISSING_ID").build();
            }

            ChatbotInteraction existing = chatbotDao.find(interaction.getId(), em);
            if (existing == null) {
                Logger.getLogger(ChatbotInteractionFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_CHATBOT_UPDATE reason=TARGET_OBJECT_NOT_FOUND actorUserId={0}",
                         loggedUser.getId());
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }

            Response access = validateInteractionAccess(loggedUser, existing);
            if (access != null) return access;

            interaction.setConsultor(existing.getConsultor());

            chatbotDao.update(interaction, em);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("find/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;

            ChatbotInteraction interaction = chatbotDao.find(id, em);
            if (interaction == null) {
                Logger.getLogger(ChatbotInteractionFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] DENIED_CHATBOT_FIND reason=TARGET_OBJECT_NOT_FOUND actorUserId={0}",
                         loggedUser.getId());
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_OBJECT_NOT_FOUND").build();
            }

            Response access = validateInteractionAccess(loggedUser, interaction);
            if (access != null) return access;

            return Response.ok(interaction).build();
        } catch (RuntimeException ex) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
