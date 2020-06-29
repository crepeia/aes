/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Message;
import aes.model.Chat;
import aes.utility.Secured;
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
import javax.ws.rs.core.MediaType;

/**
 *
 * @author bruno
 */
@Stateless
@Secured
@Path("secured/message")
public class MessageFacadeREST extends AbstractFacade<Message> {

    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;

    public MessageFacadeREST() {
        super(Message.class);
    }

    @GET
    @Path("{chatId}")
    @Produces( MediaType.APPLICATION_JSON)
    public List<Message> find(@PathParam("chatId") Long chatId) {
        /*return getEntityManager().createQuery("SELECT m FROM Message m ORDER BY m.sentDate DESC")
                .getResultList();
        */
        
        return getEntityManager().createQuery("SELECT m FROM Message m WHERE m.chat.id=:chatId ORDER BY m.sentDate DESC")
                .setParameter("chatId", chatId)
                .getResultList();

    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
