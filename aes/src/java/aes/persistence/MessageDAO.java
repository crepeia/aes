/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Message;
import aes.model.User;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class MessageDAO extends GenericDAO<Message>{
    
    public MessageDAO() throws NamingException {
        super(Message.class);
    }
    
    public List<Message> findByChat(Long chatId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT m FROM Message m WHERE m.chat.id=:chatId ORDER BY m.sentDate DESC")
            .setParameter("chatId", chatId)
            .getResultList();
    }

    public Date findLastSentDateByChatId(Long chatId, EntityManager em) {
        try {
            return em.createQuery(
                "SELECT MAX(m.sentDate) FROM Message m WHERE m.chat.id = :chatId", Date.class)
                 .setParameter("chatId", chatId)
                 .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar última sentDate", e);
        }
    }
    
    public boolean existsMessageInChat(Long chatId, EntityManager em) {
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(m.id) FROM Message m WHERE m.chat.id = :chatId", Long.class)
                    .setParameter("chatId", chatId)
                    .setMaxResults(1)
                    .getSingleResult();
            
            return count > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar se existe mensagem no chat", e);
        }
    }

    public void markAsReceived(Long messageId, EntityManager em) {
        em.createQuery("UPDATE Message m SET m.received = true WHERE m.id = :id")
            .setParameter("id", messageId)
            .executeUpdate();
    }
}
