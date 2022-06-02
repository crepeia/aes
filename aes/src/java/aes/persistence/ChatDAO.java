/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Chat;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class ChatDAO extends GenericDAO<Chat>{
    
    public ChatDAO(EntityManager entityManager) throws NamingException {
        super(Chat.class);
        this.setEntityManager(entityManager);
    }
    

    public Chat create(Chat entity) {
        try {
           super.insert(entity);
           return entity;
        } catch (Exception e) {
            return null;
        }
    }

    public Chat find(Long userId, String userEmail) {
        //String userEmail = securityContext.getUserPrincipal().getName();
        
        List<Chat> c = getEntityManager().createQuery("SELECT c FROM Chat c WHERE c.user.id=:userId AND c.user.email=:email")
                .setParameter("email", userEmail)
                .setParameter("userId", userId)
                .getResultList();
        
        if(c.isEmpty()){
            return null;
        } else {
            return (Chat) c.toArray()[0];
        }

    }
    
}
