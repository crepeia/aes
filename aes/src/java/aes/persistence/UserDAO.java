/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.User;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author thiago
 */
public class UserDAO extends GenericDAO {
    
    public UserDAO() throws NamingException {
        super(User.class);
    }
    
    public List getYearEmailUsers(EntityManager entityManager){
        Query query = entityManager.createQuery("SELECT u FROM User INNER JOIN  Evaluation as e WHERE e.yearEmail != null");
        Date data = Calendar.getInstance().getTime();
        query.setParameter("data",data );
        query.setHint("toplink.refresh", "true");
        return query.getResultList();
    }
    
}
