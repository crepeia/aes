/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAvailable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Leonorico
 */
public class AgendaAvailableDAO extends GenericDAO<AgendaAvailable> {
    
    public AgendaAvailableDAO() throws NamingException {
        super(AgendaAvailable.class);
    }
    
    public AgendaAvailable search(long id, EntityManager em) {
        //String jpql = "SELECT app FROM AgendaAvailable av JOIN FETCH av.user WHERE av.id = :id";
        //return em.createQuery(jpql, AgendaAvailable.class).setParameter("id", id).getSingleResult();
        return super.find(id, em);
    }
    
    public List<AgendaAvailable> findAll(EntityManager em) throws SQLException {
        //String jpql = "SELECT app FROM AgendaAvailable av JOIN FETCH av.user";
        //return em.createQuery(jpql, AgendaAvailable.class).getResultList();
        return super.list(em);
    }

}
