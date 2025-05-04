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
    
    public List<AgendaAppointment> findByDate(String date, EntityManager entityManager) throws SQLException {
        try {
            Query query = entityManager.createQuery(
                "SELECT app FROM AgendaAppointment app WHERE DATE(app.appointmentDate) = :dateValue ORDER BY app.appointmentDate"
            );
            query.setParameter("dateValue", java.sql.Date.valueOf(date));
            return query.getResultList();
        } catch (Exception erro) {
            throw new SQLException(erro);
        }
    }
    
    public List<AgendaAppointment> findAppointmentsByUserAndDate(Long userId, String date, EntityManager em) throws SQLException {
        String jpql = "SELECT a FROM AgendaAppointment a " +
                      "WHERE a.user.id = :userId " +
                      "AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay";

        // Monta o início e o final do dia
        String startOfDayStr = date + " 00:00:00.000000";
        String endOfDayStr = date + " 23:59:59.999999";

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.parse(startOfDayStr, formatter);
        java.time.LocalDateTime endOfDay = java.time.LocalDateTime.parse(endOfDayStr, formatter);

        return em.createQuery(jpql, AgendaAppointment.class)
                 .setParameter("userId", userId)
                 .setParameter("startOfDay", startOfDay)
                 .setParameter("endOfDay", endOfDay)
                 .getResultList();
    }
}
