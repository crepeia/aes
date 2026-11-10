package aes.service;

import aes.model.ChatbotInteraction;
import aes.model.Message;
import aes.model.User;
import aes.persistence.ChatbotInteractionDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import aes.utility.ChatbotClient;
import aes.utility.ChatbotPolicy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.SQLException;
import java.util.Date;
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
import javax.ws.rs.QueryParam;
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
    private static final Cache<Long, Bucket> QUOTAS = Caffeine.newBuilder()
            .maximumSize(10000).expireAfterAccess(Duration.ofMinutes(10)).build();
    private static final Set<Long> IN_FLIGHT = ConcurrentHashMap.newKeySet();
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

    public static class SuggestionRequest {
        public Long messagePacienteId;
    }

    @POST
    @Path("generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(SuggestionRequest request) {
        Response denied = securityHelper.requireAuthenticatedUser();
        if (denied != null) return denied;
        User consultant = securityHelper.getLoggedUser();
        if (!consultant.isConsultant() || !consultant.isUse_chatbot()) {
            return Response.status(403).entity("CHATBOT_ACCESS_DENIED").build();
        }
        if (request == null || request.messagePacienteId == null || request.messagePacienteId <= 0) {
            return Response.status(400).entity("INVALID_CHATBOT_REQUEST").build();
        }
        boolean acquired = false;
        try {
            Message patientMessage = em.find(Message.class, request.messagePacienteId);
            if (patientMessage == null) return Response.status(404).entity("TARGET_MESSAGE_NOT_FOUND").build();
            if (!ChatbotPolicy.canRequest(consultant, patientMessage)) {
                return Response.status(403).entity("INVALID_USER_OBJECT_RELATION").build();
            }
            Bucket quota = QUOTAS.get(consultant.getId(), id -> Bucket.builder().addLimit(
                    Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)))).build());
            if (!quota.tryConsume(1)) return Response.status(429).header("Retry-After", "6").entity("CHATBOT_RATE_LIMIT").build();
            acquired = IN_FLIGHT.add(consultant.getId());
            if (!acquired) return Response.status(429).header("Retry-After", "5").entity("CHATBOT_BUSY").build();

            List<Message> messages = em.createQuery("SELECT m FROM Message m WHERE m.chat.id = :chatId "
                    + "AND m.id <= :messageId ORDER BY m.id DESC", Message.class)
                    .setParameter("chatId", patientMessage.getChat().getId())
                    .setParameter("messageId", patientMessage.getId()).setMaxResults(10).getResultList();
            Collections.reverse(messages);
            String context = ChatbotPolicy.buildContext(consultant, patientMessage, messages);
            Date requestedAt = new Date();
            List<String> suggestions = createChatbotClient().generate(context);

            ChatbotInteraction interaction = new ChatbotInteraction();
            interaction.setConsultor(consultant);
            interaction.setMessagePaciente(patientMessage);
            interaction.setDate_request(requestedAt);
            interaction.setDate_response(new Date());
            interaction.setResponse1(suggestions.get(0));
            if (suggestions.size() > 1) interaction.setResponse2(suggestions.get(1));
            if (suggestions.size() > 2) interaction.setResponse3(suggestions.get(2));
            chatbotDao.insert(interaction, em);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interactionId", interaction.getId());
            result.put("chatId", patientMessage.getChat().getId());
            result.put("respostas", suggestions);
            return Response.ok(result).header("Cache-Control", "no-store").build();
        } catch (ChatbotClient.Failure e) {
            return Response.status(e.status).header("Retry-After", "5").entity(e.code).build();
        } catch (IllegalArgumentException e) {
            return Response.status(413).entity("INVALID_CHATBOT_CONTEXT").build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(ChatbotInteractionFacadeREST.class.getName()).log(Level.SEVERE,
                    "CHATBOT_REQUEST_FAILED exceptionType={0}", e.getClass().getSimpleName());
            return Response.status(500).entity("CHATBOT_REQUEST_FAILED").build();
        } finally {
            if (acquired) IN_FLIGHT.remove(consultant.getId());
        }
    }

    protected ChatbotClient createChatbotClient() throws ChatbotClient.Failure {
        return ChatbotClient.fromEnvironment();
    }
    
    @Path("insert")
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response insert(@QueryParam("messagePacienteId") Long messagePacienteId) {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;

            if (messagePacienteId == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("MISSING_MESSAGE_PACIENTE_ID").build();
            }

            Message patientMessage = em.find(Message.class, messagePacienteId);
            if (patientMessage == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("TARGET_MESSAGE_NOT_FOUND").build();
            }
            if (!ChatbotPolicy.canRequest(loggedUser, patientMessage)) {
                return Response.status(403).entity("INVALID_USER_OBJECT_RELATION").build();
            }

            ChatbotInteraction interaction = new ChatbotInteraction();
            interaction.setConsultor(loggedUser);
            interaction.setMessagePaciente(patientMessage);
            interaction.setDate_request(new Date());

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

            if (interaction.getResponse1() != null) existing.setResponse1(interaction.getResponse1());
            if (interaction.getResponse2() != null) existing.setResponse2(interaction.getResponse2());
            if (interaction.getResponse3() != null) existing.setResponse3(interaction.getResponse3());

            if (interaction.getConsultantClickedResponse1() != null)
                existing.setConsultantClickedResponse1(interaction.getConsultantClickedResponse1());
            if (interaction.getConsultantClickedResponse2() != null)
                existing.setConsultantClickedResponse2(interaction.getConsultantClickedResponse2());
            if (interaction.getConsultantClickedResponse3() != null)
                existing.setConsultantClickedResponse3(interaction.getConsultantClickedResponse3());
            
            Integer lastClicked = interaction.getLastClickedResponseByConsultant();
            if (lastClicked != null) {
                switch (lastClicked) {
                    case 1: existing.setConsultantClickedResponse1(true); break;
                    case 2: existing.setConsultantClickedResponse2(true); break;
                    case 3: existing.setConsultantClickedResponse3(true); break;
                    default:
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity("INVALID_LAST_CLICKED_RESPONSE").build();
                }
                existing.setLastClickedResponseByConsultant(lastClicked);
            }

            if (interaction.getMessageConsultor() != null)
                existing.setMessageConsultor(interaction.getMessageConsultor());

            boolean respostasChegaram = interaction.getResponse1() != null
                    || interaction.getResponse2() != null
                    || interaction.getResponse3() != null;
            if (respostasChegaram && existing.getDate_response() == null) {
                existing.setDate_response(new Date());
            }

            chatbotDao.update(existing, em);
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
