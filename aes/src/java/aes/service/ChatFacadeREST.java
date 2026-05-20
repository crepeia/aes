/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Chat;
import aes.model.Message;
import aes.model.User;
import aes.persistence.ChatDAO;
import aes.persistence.MessageDAO;
import aes.persistence.UserDAO;
import aes.utility.EmailHelper;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
@Path("chat")
@TransactionManagement(TransactionManagementType.BEAN)
public class ChatFacadeREST extends AbstractFacade<Chat> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ChatDAO chatDAO;
    private UserDAO userDao;
    private EmailHelper emailHelper;
    private MessageDAO messageDAO;
    
    @EJB
    private ChatMessageService chatMessageService;

    @Inject
    private SecurityContextHelper securityHelper;

    public ChatFacadeREST() {
        super(Chat.class);
        emailHelper = new EmailHelper();
        try {
            chatDAO = new ChatDAO();
            userDao = new UserDAO();
            messageDAO = new MessageDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Secured
    public Response create() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            Chat newChat = chatDAO.create(loggedUser.getId(), em);
            return Response.status(Response.Status.CREATED).entity(newChat).build();
        } catch (RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @GET
    @Path("findByUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response findByUser() {
        try {    
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            Chat chat = chatDAO.find(loggedUser.getId(), loggedUser.getEmail(), em);
            if (chat == null) {
                Logger.getLogger(ChatFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] CHAT_NOT_FOUND reason=TARGET_OBJECT_NOT_FOUND "
                       + "actorUserId={0}",
                         loggedUser.getId());
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("TARGET_OBJECT_NOT_FOUND")
                    .build();
            }

            return Response.ok(chat).build();
        } catch (RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @PUT
    @Path("sendContactRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendContactRequest(JsonParser jp) throws MessagingException {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            if (node == null || node.get("email") == null) {
                Logger.getLogger(ChatFacadeREST.class.getName())
                    .log(Level.WARNING,
                         "[SECURITY] INVALID_OR_EMPTY_NODE reason=INVALID_DATA ");
                
                return Response.status(Response.Status.BAD_REQUEST)
                .entity("INVALID_DATA")
                .build();
            }
            
            String email = node.get("email").asText();
            System.out.println("aes.service.ChatFacadeREST.sendContactRequest()");
            emailHelper.sendContactRequestEmail(email, em);
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.INFO, null, "Send Contact Request service");
            
            return Response.ok().build();
        } catch (IOException | SQLException | MissingResourceException | MessagingException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("findUserChats")
    @Secured
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findUserChats() {
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            r = securityHelper.requireConsultant(loggedUser);
            if (r != null) return r;
            
            return Response.ok(chatDAO.listUserChats(loggedUser.getId(), em)).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("unread/{chatId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUnreadMessages(@PathParam("chatId") Long chatId) {
        Logger logger = Logger.getLogger(ChatFacadeREST.class.getName());
        
        try {
            Response r = securityHelper.requireAnyAuthenticated();
            if (r != null) return r;
            
            User loggedUser = securityHelper.getLoggedUser();
            
            logger.log(Level.INFO, "[UNREAD] User {0} requesting unread messages for chat {1}",
                new Object[]{loggedUser.getId(), chatId});
            
            Chat chat = chatDAO.find(chatId, em);
            
            if (chat == null) {
                logger.log(Level.WARNING, "[UNREAD] Chat not found: chatId={0}", chatId);
                
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("TARGET_OBJECT_NOT_FOUND")
                    .build();
            }
            
            List<Message> unreadMessages = messageDAO.getUnreadMessages(
                chatId,
                String.valueOf(loggedUser.getId()),
                em
            );
            
            logger.log(Level.INFO, "[UNREAD] Found {0} unread messages for chatId={1}",
                new Object[]{unreadMessages.size(), chatId});
            
            List<Long> ids = unreadMessages.stream()
                .map(Message::getId)
                .collect(java.util.stream.Collectors.toList());
            
            int updated = chatMessageService.markAllAsReceived(ids);
            
            logger.log(Level.INFO, "[UNREAD] Marked {0} messages as received for chatId={1}",
                new Object[]{updated, chatId});

            return Response.ok(unreadMessages).build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "[UNREAD] ERROR while fetching unread messages", ex);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("INTERNAL_SERVER_ERROR")
                .build();
        }
    }
    
    /*
    @GET
    @Path("findAnonymousChats")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAnonymousChats() {
        List<Chat> chats;
        try {
            chats = chatDAO.listUserChats(null, em);
            return Response.ok().entity(chats).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @GET
    @Path("findAnonymousChat/{chatId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAnonymousChat(@PathParam("chatId") Long chatId) {
        try {
            List<Message> messages = chatDAO.findAnonymousChatById(chatId, em);
            if (messages == null || messages.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                           .entity("Nenhuma mensagem encontrada para o chat especificado.")
                           .build();
            }
            return Response.ok().entity(messages).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    */

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
