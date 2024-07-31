/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAppointment;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.hibernate.CacheMode;

/**
 *
 * @author Leonorico
 */
public class AgendaAppointmentDAO extends GenericDAO<AgendaAppointment> {
    
    public AgendaAppointmentDAO(Class<AgendaAppointment> classe) throws NamingException {
        super(classe);
    }
    
    public List<AgendaAppointment> listByUser(String id, Object idValue, Object dateTimeValue, EntityManager entityManager) throws SQLException {

        try {
            
            id = id.substring(0, 1).toLowerCase() + id.substring(1);
            Query query;
            
            if(id != null) {
                query = entityManager.createQuery("select app from AgendaAppointment app where app." + id + " = :idValue and app.appointmentDate >= :dateTimeValue order by app.appointmentDate");
                query.setParameter("idValue", idValue);
                query.setParameter("dateTimeValue", dateTimeValue);
            } else {
                query = entityManager.createQuery("select app from AgendaAppointment app where app." + id + " is Null");
            }
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
}
