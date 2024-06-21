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
        find(id, em).removeAvailable();
        em.remove(find(id, em));
        em.getTransaction().commit();
    }
    
    //TOVERIFY
    public void update(long id, AgendaAvailable available, EntityManager em) {
        em.getTransaction().begin();
        AgendaAvailable av = find(id, em);
        av.setUser(available.getUser());
        av.setAvailableDate(available.getAvailableDate());
        em.getTransaction().commit();
    }
    
    //TOVERIFY
    public AgendaAvailable find(long id, EntityManager em) {
        return super.find(id, em);
    }
    
    //TOVERIFY
    public List<AgendaAvailable> findAll(EntityManager em) throws SQLException {
        return super.list(em);
    }

}
