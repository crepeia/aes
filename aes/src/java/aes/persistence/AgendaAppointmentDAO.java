/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAppointment;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
    
    public List<AgendaAppointment> listCurrentByUser(Long idValue, LocalDateTime dateTimeValue, EntityManager entityManager) throws SQLException {

        try {

            Query query;
            
            if(idValue != null) {
                query = entityManager.createQuery("select app from AgendaAppointment app where app.user.id = :idValue and app.appointmentDate >= :dateTimeValue order by app.appointmentDate");
                query.setParameter("idValue", idValue);
                query.setParameter("dateTimeValue", dateTimeValue);
            } else {
                query = entityManager.createQuery("select app from AgendaAppointment app where app.user.id is Null");
            }
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
}
