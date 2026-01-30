/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Message;
import aes.model.User;
import aes.persistence.MessageDAO;
import aes.utility.Secured;
import aes.utility.SecurityContextHelper;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

    @Inject
    private SecurityContextHelper securityHelper;

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
        try {
            Response r = securityHelper.requireAuthenticatedUser();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            // Only answer queries from the owner of the messagens or consultant
            if (!(loggedUser.getChat().getId().equals(chatId) || loggedUser.isConsultant())) {
                Logger.getLogger(MessageFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER_OR_NON_CONSULTANT loggedUserId={1}",
                        loggedUser.getId());
                return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
            }

            List<Message> messages = messageDAO.findByChat(chatId, em);
            
            if (messages == null) {
                return Response.noContent().build();
            } else {
                return Response.ok().entity(messages).build();
            }
        } catch (Exception ex) {
            Logger.getLogger(MessageFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }
    
    @GET
    @Path("anonymous/{chatId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response anonymousFind(@PathParam("chatId") Long chatId) {
        try {
            Response r = securityHelper.requireAuthenticatedAnonymous();
            if (r != null) return r;

            User loggedUser = securityHelper.getLoggedUser();

            // Only answer queries from the owner of the messagens
            if (!(loggedUser.getChat().getId().equals(chatId))) {
                Logger.getLogger(MessageFacadeREST.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] ACCESS_DENIED reason=ACCESS_DENIED_DIFFERENT_USER loggedUserId={1}",
                        loggedUser.getId());
                return Response.status(Response.Status.FORBIDDEN).entity("ACCESS_DENIED").build();
            }

            List<Message> messages = messageDAO.findByChat(chatId, em);

            if (messages == null) {
                return Response.noContent().build();
            } else {
                return Response.ok().entity(messages).build();
            }
        } catch (Exception ex) {
            Logger.getLogger(MessageFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR").build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
