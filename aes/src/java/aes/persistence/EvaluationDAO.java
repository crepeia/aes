/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Evaluation;
import java.util.ArrayList;
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
public class EvaluationDAO extends GenericDAO {
    
    public EvaluationDAO() throws NamingException{
            super(Evaluation.class);
    }
    
   public List getYearEmailEvaluations(EntityManager entityManager){
        Query query = entityManager.createQuery("SELECT e1 FROM Evaluation e1 \n" +
        "WHERE e1.date = (SELECT max(e2.date) from Evaluation e2 where e2.user = e1.user)" +
        "AND yearEmail != null AND (yearEmailDate == null OR  yearEmailDate <= :oneYearAgo");
        Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.set(Calendar.YEAR, (oneYearAgo.get(Calendar.YEAR) - 1));
        query.setParameter("oneYearAgo", oneYearAgo.getTime());
        return query.getResultList();
    } 
    
}
