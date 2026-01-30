/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.TitleUser;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Matheus Carvalho
 */
public class TitleUserDAO extends GenericDAO<TitleUser> {
    
    public TitleUserDAO() throws NamingException {
        super(TitleUser.class);
    }
    
    public void insertMedalAndTitle(TitleUser titleUser,  EntityManager entityManager) throws SQLException {
        this.insertOrUpdate(titleUser, entityManager);
    }
    
    public List<TitleUser> findByUserTitleDescription(Long userId, Long titleId, String description, EntityManager entityManager) {
        return entityManager.createQuery("SELECT te FROM TitleUser te WHERE te.user.id =: userId AND te.title.id =: titleId AND te.description =: titleDescription")
            .setParameter("userId", userId)
            .setParameter("titleId", titleId)
            .setParameter("titleDescription", description)
            .getResultList();
    }
    
    public List<TitleUser> findByUser(Long userId, EntityManager entityManager) {
        return entityManager.createQuery("SELECT te FROM TitleUser te WHERE te.user.id =: userId")
            .setParameter("userId", userId)
            .getResultList();
    }
    
    public List<TitleUser> findAll(EntityManager entityManager) {
        return entityManager.createQuery("SELECT te FROM TitleUser te").getResultList();
    }
}