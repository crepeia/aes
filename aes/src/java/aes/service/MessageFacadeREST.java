/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Message;
import aes.persistence.MessageDAO;
import aes.utility.Secured;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
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

    public MessageFacadeREST() {
        super(Message.class);

        try {
            messageDAO = new MessageDAO();
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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
