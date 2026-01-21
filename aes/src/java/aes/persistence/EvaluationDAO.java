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
        } catch (SQLException e) {
            return null;
        }
    }


    public Evaluation find(Long userId, EntityManager entityManager) throws SQLException {
  
            List<Evaluation> evList = entityManager.createQuery("SELECT e FROM Evaluation e WHERE e.user.id=:userId")
                    .setParameter("userId", userId)
                    .getResultList();
            
            if(evList.size() > 0){
                return evList.get(evList.size()-1);
            } else {
                return null;
            }
    }
    
    public void createEvaluation(Evaluation newEvaluation, EntityManager entityManager) throws SQLException {       
        try {
            super.insertOrUpdate(newEvaluation, entityManager);
        } catch (SQLException e) {
            throw new SQLException("Error inserting Evaluation", e);
        }
    }
    
    public List<Date> listDatesByUser(Long userId, EntityManager entityManager) throws SQLException {
        return entityManager.createQuery(
            "SELECT e.dateCreated FROM Evaluation e WHERE e.user.id = :userId", Date.class)
            .setParameter("userId", userId)
            .getResultList();
    }
    
    public List<Evaluation> findByUserAndDate(Long userId, Date start, Date end, EntityManager entityManager) throws SQLException {
        return entityManager.createQuery(
            "SELECT e FROM Evaluation e WHERE e.user.id = :userId AND e.dateCreated BETWEEN :start AND :end", Evaluation.class)
            .setParameter("userId", userId)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList();
    }
}
