/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Evaluation;
import aes.model.User;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class EvaluationDAO extends GenericDAO<Evaluation>{
    
    public EvaluationDAO(EntityManager entityManager) throws NamingException {
        super(Evaluation.class);
        this.setEntityManager(entityManager);
    }
    

    public Evaluation create(Evaluation entity) {
        try {
            super.insertOrUpdate(entity);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }


    public Evaluation find(Long userId, String userEmail) throws SQLException {
  
            List<Evaluation> evList = getEntityManager().createQuery("SELECT e FROM Evaluation e WHERE e.user.id=:userId AND e.user.email=:userEmail")
                    .setParameter("userId", userId)
                    .setParameter("userEmail", userEmail)
                    .getResultList();
            
            if(evList.size() > 0){
                return evList.get(evList.size()-1);
            } else {
                //System.out.println("service.EvaluationFacadeREST.find() create");
                Evaluation ev = new Evaluation();
                ev.setDateCreated(new Date());
                ev.setUser(getEntityManager().find(User.class, userId));
                super.insertOrUpdate(ev);
                return ev;
            }

        
    }
    
    
}
