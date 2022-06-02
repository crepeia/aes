/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Chat;
import aes.persistence.ChatDAO;
import aes.utility.Secured;
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
@Path("chat")
@TransactionManagement(TransactionManagementType.BEAN)
public class ChatFacadeREST extends AbstractFacade<Chat> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ChatDAO chatDAO;

    @Context
    SecurityContext securityContext;

    public ChatFacadeREST() {
        super(Chat.class);
        try {
            chatDAO = new ChatDAO(em);
        } catch (NamingException ex) {
            Logger.getLogger(ChatFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Chat create(Chat entity) {
        try {
            return super.create(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("userId") Long userId) {
        String userEmail = securityContext.getUserPrincipal().getName();

        Chat c = chatDAO.find(userId, userEmail);
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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
