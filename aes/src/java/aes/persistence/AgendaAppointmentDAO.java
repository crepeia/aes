/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAppointment;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Leonorico
 */
public class AgendaAppointmentDAO extends GenericDAO<AgendaAppointment> {
    
    public AgendaAppointmentDAO() throws NamingException {
        super(AgendaAppointment.class);
    }
    
    public List<AgendaAppointment> findAll(EntityManager em) throws SQLException {
        //TODO: Possível melhoria de performance no futuro
//        String jpql = "SELECT app FROM AgendaAppointment app JOIN FETCH app.user JOIN FETCH app.consultant";
//        return em.createQuery(jpql, AgendaAppointment.class).getResultList();
        return super.list(em);
    }

    public AgendaAppointment search(long id, EntityManager em) {
        //TODO: Possível melhoria de performance no futuro
//        String jpql = "SELECT app FROM AgendaAppointment app JOIN FETCH app.user JOIN FETCH app.consultant WHERE app.id = :id";
//        return em.createQuery(jpql, AgendaAppointment.class).setParameter("id", id).getSingleResult();
        return super.find(id, em);
    }
}
