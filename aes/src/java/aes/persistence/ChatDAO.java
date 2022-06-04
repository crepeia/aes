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
    
    public ChatDAO() throws NamingException {
        super(Chat.class);
    }
    

    public Chat create(Chat entity, EntityManager entityManager) {
        try {
           super.insert(entity, entityManager);
           return entity;
        } catch (Exception e) {
            return null;
        }
    }

    public Chat find(Long userId, String userEmail, EntityManager entityManager) {
        //String userEmail = securityContext.getUserPrincipal().getName();
        
        List<Chat> c = entityManager.createQuery("SELECT c FROM Chat c WHERE c.user.id=:userId AND c.user.email=:email")
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
