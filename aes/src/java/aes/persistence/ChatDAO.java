/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Chat;
import aes.model.User;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.hibernate.CacheMode;

/**
 *
 * @author patri
 */
public class ChatDAO extends GenericDAO<Chat>{
    
    public ChatDAO() throws NamingException {
        super(Chat.class);
    }
    

    public Chat create(Long userId ,EntityManager entityManager) {
        try {
            Chat newChat = new Chat();
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserByID(userId, entityManager);
            newChat.setUser(user);
            newChat.setStartDate(new Date());
            newChat.setUnauthenticatedId(null);
            user.setChat(newChat);
            userDAO.uptadeUser(user,entityManager);
            newChat = find(userId, user.getEmail(), entityManager);
           return newChat;
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
    
    public List<Chat> listUserChats(Object objeto, EntityManager entityManager) throws SQLException {

        try {
            Query query;
            query = entityManager.createQuery("select chat.messageList from Chat chat where chat.user.relatedConsultant.id = :objeto");
            query.setParameter("objeto", objeto);
            query.setHint("org.hibernate.cacheMode", CacheMode.REFRESH);
            query.setHint("org.hibernate.cacheable", true);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
    
}
