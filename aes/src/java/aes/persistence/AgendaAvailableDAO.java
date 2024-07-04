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
    
    UserDAO userDao;
    
    public AgendaAvailableDAO() throws NamingException {
        super(AgendaAvailable.class);
        userDao = new UserDAO();
    }
    
    //TOVERIFY    
    @Override
    public void insert(AgendaAvailable available, EntityManager em) throws SQLException {
        userDao.find(available.getUser(), em).setAvailableUser(available);
        super.insert(available, em);
    }
    
    //TOVERIFY
    public void remove(long id, EntityManager em) {
        em.getTransaction().begin();
        search(id, em).removeAvailable();
        em.getTransaction().commit();
        em.remove(search(id, em));
        em.getTransaction().commit();
    }
    
    //TOVERIFY
    public void update(long id, AgendaAvailable available, EntityManager em) {
        em.getTransaction().begin();
        AgendaAvailable av = search(id, em);
        av.setUser(available.getUser());
        av.setAvailableDate(available.getAvailableDate());
        em.getTransaction().commit();
    }
    
    //TOVERIFY
    public AgendaAvailable search(long id, EntityManager em) {
        String jpql = "SELECT app FROM AgendaAvailable av JOIN FETCH av.user WHERE av.id = :id";
        return em.createQuery(jpql, AgendaAvailable.class).setParameter("id", id).getSingleResult();
        //return super.find(id, em);
    }
    
    //TOVERIFY
    public List<AgendaAvailable> findAll(EntityManager em) throws SQLException {
        String jpql = "SELECT app FROM AgendaAvailable av JOIN FETCH av.user";
        return em.createQuery(jpql, AgendaAvailable.class).getResultList();
        //return super.list(em);
    }

}
