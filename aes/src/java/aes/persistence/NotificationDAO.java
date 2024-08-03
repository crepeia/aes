/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Notification;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Malder
 */
public class NotificationDAO extends GenericDAO<Notification> {
    
    public NotificationDAO(Class<Notification> classe) throws NamingException {
        super(classe);
    }
    
    public List<Notification> listUnreadByUser(Object idValue, EntityManager entityManager) throws SQLException {

        try {         
            Query query;
            
            if(idValue != null) {
                query = entityManager.createQuery("select notif from Notification notif where notif.user.id = :idValue and notif.notificated = false order by notif.user.id");
                query.setParameter("idValue", idValue);
            } else {
                query = entityManager.createQuery("select notif from Notification notif where notif.user.id is Null");
            }
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
    
}
