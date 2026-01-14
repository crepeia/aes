/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Question;
import aes.model.QuestionUser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Matheus Carvalho
 */
public class QuestionDAO extends GenericDAO<Question>{
    
    public QuestionDAO() throws NamingException {
        super(Question.class);
    }
    
    public Question findById(Long questionId, EntityManager entityManager) {
        return (Question) entityManager.createQuery("SELECT q FROM Question q WHERE q.id=:questionId")
            .setParameter("questionId", questionId)
            .getSingleResult();
    }
    
    public List<Question> findAllQuestion(EntityManager entityManager) {
        return entityManager.createQuery("SELECT q FROM Question q").getResultList();
    }
    
    public List<QuestionUser> findUserQuestions(Long userId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT qu FROM QuestionUser qu WHERE qu.user.id=:userId")
            .setParameter("userId", userId)
            .getResultList();
    }
    
    public List<QuestionUser> findLastUserAnswer(String cD, Long userId, EntityManager entityManager) throws ParseException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = sdf.parse(cD);

            return entityManager.createQuery("SELECT qu FROM QuestionUser qu WHERE qu.user.id=:userId AND qu.dateCreated=:currentDate")
                .setParameter("userId", userId)
                .setParameter("currentDate", currentDate.toInstant().atZone( ZoneId.systemDefault() ).toLocalDate())
                .getResultList();  
        } catch (ParseException ex) {
            System.out.println("[ERROR] ParseException error: " + ex);
            return null;
        }
    }
}
