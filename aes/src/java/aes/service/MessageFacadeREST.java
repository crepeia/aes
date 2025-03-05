/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Chat;
import aes.model.User;
import aes.model.Message;
import aes.persistence.ChatDAO;
import aes.persistence.MessageDAO;
import aes.persistence.UserDAO;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
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
@Secured
@Path("secured/message")
@TransactionManagement(TransactionManagementType.BEAN)
public class MessageFacadeREST extends AbstractFacade<Message> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    @Context
    SecurityContext securityContext;

    private MessageDAO messageDAO;
    private ChatDAO chatDAO;
    private UserDAO userDAO;

    public MessageFacadeREST() {
        super(Message.class);

        try {
            messageDAO = new MessageDAO();
            chatDAO = new ChatDAO();
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(MessageFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("{chatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("chatId") Long chatId) {

        String userEmail = securityContext.getUserPrincipal().getName();
        /* User u = (User) getEntityManager().createQuery("SELECT u FROM User u WHERE u.email=:userEmail")
                .setParameter("userEmail", userEmail)
                .getSingleResult();
        //only answer queries from the owner of the messagens or consultant
        if(u.getChat().getId().equals(chatId) || u.isConsultant()){
            List<Message> m = (List<Message>) getEntityManager().createQuery("SELECT m FROM Message m WHERE m.chat.id=:chatId ORDER BY m.sentDate DESC")
                .setParameter("chatId", chatId)
                .getResultList();
       
            return Response.ok().entity(m).build();
        } else {
            return Response.noContent().build();
        }*/

        List<Message> m = messageDAO.find(chatId, userEmail, em);
        if (m == null) {
            return Response.noContent().build();

        } else {
            return Response.ok().entity(m).build();

        }
    }
    
    @POST
    @Path("sendMessage")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response sendMessage(Message message) {
        try {
            if (message == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Message object cannot be null").build();
            }

            if (message.getChat() == null || message.getChat().getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Chat ID must be provided").build();
            }

            if (message.getIdFrom() == null || message.getIdFrom().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Sender ID must be provided").build();
            }

            // Verificar se o usuário remetente existe.
            Long userId;
            try {
                userId = Long.parseLong(message.getIdFrom());
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("idFrom deve ser um número válido").build();
            }
            
            User sender = userDAO.find(userId, em);
            if (sender == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Sender ID does not exist").build();
            }

            messageDAO.createMessage(message, em);
            return Response.status(Response.Status.CREATED).build(); 
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(PageRatingFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
