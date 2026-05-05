package aes.service;

import aes.persistence.MessageDAO;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author luansb
 */
@Stateless
public class ChatMessageService {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    public void markAsReceived(Long messageId) {
        try {
            em.createQuery("UPDATE Message m SET m.received = true WHERE m.id = :id")
                .setParameter("id", messageId)
                .executeUpdate();
        } catch (Exception ex) {
            Logger.getLogger(MessageFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    public int markAllAsReceived(List<Long> messageIds) {
        try {
            if (messageIds == null || messageIds.isEmpty()) return 0;
            
            return em.createQuery("UPDATE Message m SET m.received = true WHERE m.id IN :ids")
                .setParameter("ids", messageIds)
                .executeUpdate();
        } catch (Exception ex) {
            Logger.getLogger(MessageFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return 0;
        }
    }
}
