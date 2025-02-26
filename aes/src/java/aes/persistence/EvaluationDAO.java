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
    
    public EvaluationDAO() throws NamingException {
        super(Evaluation.class);
    }
    

    public Evaluation create(Evaluation entity, EntityManager entityManager) {
        try {
            super.insertOrUpdate(entity, entityManager);
            return entity;
        } catch (Exception e) {
            return null;
        }
    }


    public Evaluation find(Long userId, String userEmail, EntityManager entityManager) throws SQLException {
  
            List<Evaluation> evList = entityManager.createQuery("SELECT e FROM Evaluation e WHERE e.user.id=:userId AND e.user.email=:userEmail")
                    .setParameter("userId", userId)
                    .setParameter("userEmail", userEmail)
                    .getResultList();
            
            if(evList.size() > 0){
                return evList.get(evList.size()-1);
            } else {
                //System.out.println("service.EvaluationFacadeREST.find() create");
                Evaluation ev = new Evaluation();
                ev.setDateCreated(new Date());
                ev.setUser(entityManager.find(User.class, userId));
                super.insertOrUpdate(ev, entityManager);
                return ev;
            }
    }
    
   public void createEvaluation(Evaluation newEvaluation, EntityManager entityManager) throws SQLException {       
       try {
           super.insertOrUpdate(newEvaluation, entityManager);
       } catch (SQLException e) {
           throw new SQLException("Error inserting Evaluation", e);
       }
   }
    
    
}
