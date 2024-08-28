/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Chat;
import aes.persistence.ChatDAO;
import aes.persistence.UserDAO;
import aes.utility.EmailHelper;
import aes.utility.Secured;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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

    @Context
    SecurityContext securityContext;

    public ChatFacadeREST() {
        super(Chat.class);
        emailHelper = new EmailHelper();
        try {
            chatDAO = new ChatDAO();
            userDao = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
        }
    }

    @POST
    @Path("create/{userId}")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("userId") Long userId) {
        try {
            Chat newChat = chatDAO.create(userId, em);
            return Response.ok().entity(newChat).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Path("{userId}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        String userEmail = securityContext.getUserPrincipal().getName();

        Chat c = chatDAO.find(userId, userEmail, em);
        if (c == null) {
            return Response.status(Response.Status.NO_CONTENT).build();

        } else {
            return Response.ok().entity(c).build();
        }
        /*List<Chat> c = getEntityManager().createQuery("SELECT c FROM Chat c WHERE c.user.id=:userId AND c.user.email=:email")
                .setParameter("email", userEmail)
                .setParameter("userId", userId)
                .getResultList();
        
        if(c.isEmpty()){
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok().entity((Chat) c.toArray()[0]).build();
        }*/

    }
    
    @PUT
    @Path("sendContactRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendContactRequest(JsonParser jp) {
        try {
            JsonNode node = jp.getCodec().readTree(jp);
            String email = node.get("email").asText();
            System.out.println("aes.service.ChatFacadeREST.sendContactRequest()");

            emailHelper.sendContactRequestEmail(email, em);
            
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.INFO, null, "Send Contact Request service");
            return Response.ok().build();
        } catch (Exception e) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.INFO, "Error type: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("findUserChats/{id}")
    @Secured
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findUserChats(@PathParam("id") Long idConsultant) {
        List<Chat> chats;
        try {
            chats = chatDAO.listUserChats(idConsultant, em);
            return Response.ok().entity(chats).build();
        } catch (SQLException | RuntimeException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.INFO, "Error type: ", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
