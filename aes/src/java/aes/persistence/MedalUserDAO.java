/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.DailyLog;
import aes.model.MedalUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Matheus Carvalho
 */
public class MedalUserDAO extends GenericDAO<MedalUser> {
    
    public MedalUserDAO() throws NamingException {
        super(MedalUser.class);
    }
    
    public List<MedalUser> findByUserId(Long userId, EntityManager entityManager) {
        return (List<MedalUser>) entityManager.createQuery("SELECT mu FROM MedalUser mu WHERE mu.user.id=:userId")
            .setParameter("userId", userId)
            .getResultList();
    }
    
    public List<MedalUser> findByUserMedalAndDescription(Long userId, Long medalId, String description, EntityManager entityManager) {
        return entityManager.createQuery(
                "SELECT me FROM MedalUser me " +
                "WHERE me.user.id = :userId " +
                "AND me.medal.id = :medalId " +
                "AND me.description = :description",
                MedalUser.class)
            .setParameter("userId", userId)
            .setParameter("medalId", medalId)
            .setParameter("description", description)
            .getResultList();
    }
    
    public List<MedalUser> findByUserAndMedal(Long userId, Long medalId, EntityManager entityManager) {
        return entityManager.createQuery(
                "SELECT me FROM MedalUser me " +
                "WHERE me.user.id = :userId " +
                "AND me.medal.id = :medalId ",
                MedalUser.class)
            .setParameter("userId", userId)
            .setParameter("medalId", medalId)
            .getResultList();
    }
    
    public MedalUser findSingleResultByUserAndMedal(Long userId, Long medalId, EntityManager entityManager) {
        MedalUser mu = (MedalUser) entityManager.createQuery("SELECT mu from MedalUser mu WHERE mu.user.id=:userId and mu.medal.id=:medalId")
            .setParameter("userId", userId)
            .setParameter("medalId", medalId)
            .getSingleResult();
        
        return mu;
    }
            
    public String getMonthlyDrinkMedalByUser(Long userId, LocalDate today, int month, EntityManager entityManager) {
        return entityManager
            .createQuery("SELECT COUNT(*) FROM ChallengeUser c WHERE month(c.dateCreated)=:monthNumber and c.user.id=:userId and c.challenge.id=:challengeId")
            .setParameter("monthNumber", month)
            .setParameter("userId", userId)   
            .setParameter("challengeId", 5L)
            .getSingleResult().toString();
    }
    
    public String getMonthlyNotDrinkMedalByUser(Long userId, LocalDate today, int month, EntityManager entityManager) {
        return entityManager
            .createQuery("SELECT COUNT(*) FROM ChallengeUser c WHERE month(c.dateCreated)=:monthNumber and c.user.id=:userId and c.challenge.id=:challengeId")
            .setParameter("monthNumber", month)
            .setParameter("userId", userId)   
            .setParameter("challengeId", 6L)
            .getSingleResult().toString();  
    }
    
    public List<DailyLog> getDrinkLogByUser(Long userId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT dl FROM DailyLog dl WHERE dl.record.id=:userId ORDER BY dl.logDate DESC")
            .setParameter("userId", userId)   
            .getResultList(); 
    }
    
    public void insertMedalAndTitle(MedalUser medalUser, EntityManager entityManager) throws SQLException {
        this.insertOrUpdate(medalUser, entityManager);    
    }
    
     
}
