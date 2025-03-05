/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Message;
import aes.model.User;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class MessageDAO extends GenericDAO<Message>{
    
    public MessageDAO() throws NamingException {
        super(Message.class);
    }
    
    

    public List<Message> find(Long chatId, String userEmail, EntityManager entityManager) {

        //String userEmail = securityContext.getUserPrincipal().getName();
        User u = (User) entityManager.createQuery("SELECT u FROM User u WHERE u.email=:userEmail")
                .setParameter("userEmail", userEmail)
                .getSingleResult();
        //only answer queries from the owner of the messagens or consultant
        if(u.getChat().getId().equals(chatId) || u.isConsultant()){
            List<Message> m = (List<Message>) entityManager.createQuery("SELECT m FROM Message m WHERE m.chat.id=:chatId ORDER BY m.sentDate DESC")
                .setParameter("chatId", chatId)
                .getResultList();
            return m;
        } else {
            return null;
        }
    }
    
    public void createMessage(Message message, EntityManager entityManager) throws SQLException {
        try {
            super.insertOrUpdate(message, entityManager);
        } catch (SQLException e) {
            throw new SQLException("Error inserting message", e);
        }
    }
}
