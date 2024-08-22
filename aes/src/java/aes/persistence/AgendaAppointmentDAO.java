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

/**
 *
 * @author Leonorico
 */
public class AgendaAppointmentDAO extends GenericDAO<AgendaAppointment> {
    
    public AgendaAppointmentDAO() throws NamingException {
        super(AgendaAppointment.class);
    }
    
    public List<AgendaAppointment> find(Long id, EntityManager entityManager) throws SQLException {
        Query query;
        try {
            query = entityManager.createQuery("select app from AgendaAppointment app where app.id = :id");
            query.setParameter("id", id);
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
    
    public List<AgendaAppointment> listCurrentByUser(Long idValue, EntityManager entityManager) throws SQLException {

        try {

            Query query;
            LocalDateTime dateTimeValue = LocalDateTime.now();
            
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
