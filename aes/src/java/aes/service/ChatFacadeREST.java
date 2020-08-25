/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Chat;
import aes.model.User;
import aes.utility.Secured;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
@Secured
@Path("chat")
public class ChatFacadeREST extends AbstractFacade<Chat> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    
    @Context
    SecurityContext securityContext;
    

    public ChatFacadeREST() {
        super(Chat.class);
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
    public Chat find(@PathParam("userId") Long userId) {
        String userEmail = securityContext.getUserPrincipal().getName();
        
        List<Chat> c = getEntityManager().createQuery("SELECT c FROM Chat c WHERE c.user.id=:userId AND c.user.email=:email")
                .setParameter("email", userEmail)
                .setParameter("userId", userId)
                .getResultList();
        
        if(c.isEmpty()){
            return null;
            /*
            Chat newChat = new Chat();
            newChat.setUnauthenticatedId("");
            newChat.setUser(em.find(User.class, userId));
            newChat.setStartDate(new Date());
            super.create(newChat);
            return newChat;
            */
        } else {
            return (Chat) c.toArray()[0];
        }

        /*
        return getEntityManager().createQuery("SELECT m FROM Message m WHERE m.chat.user.id=:userId")
                .setParameter("userId", userId)
                .getResultList();
*/
    }
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
