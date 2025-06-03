/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AuthenticationToken;
import aes.model.User;
import aes.utility.SecureRandomString;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author patri
 */
public class AuthenticationTokenDAO extends GenericDAO<AuthenticationToken>{
    
    public AuthenticationTokenDAO() throws NamingException {
        super(AuthenticationToken.class);
    }
    
    
    public String issueToken(User usr, EntityManager entityManager) throws SQLException{
        String token = SecureRandomString.generate();
        
        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken(token);
        authToken.setUser(usr);
        authToken.setDateCreated(new Date());
        super.update(authToken, entityManager);
        
        return token;
    }
    
    public void revokeToken(String token, String userEmail, EntityManager entityManager) throws SQLException {
        AuthenticationToken at = (AuthenticationToken) entityManager.createQuery("SELECT at FROM AuthenticationToken at WHERE at.token=:token AND at.user.email=:uEmail")
        .setParameter("token", token)
        .setParameter("uEmail", userEmail)
        .getSingleResult();
        super.delete(at, entityManager);
    
    }
    
    public void deleteExpiredTokens(Date limitDate, EntityManager entityManager) throws SQLException {
        List<AuthenticationToken> expiredTokens = entityManager
        .createQuery("SELECT t FROM AuthenticationToken t WHERE t.dateCreated < :limit", AuthenticationToken.class)
        .setParameter("limit", limitDate)
        .getResultList();

        for (AuthenticationToken token : expiredTokens) {
            super.delete(token, entityManager);
        }
    }
    
    public AuthenticationToken findByToken(String token, EntityManager entityManager) {
        try {
            return entityManager.createQuery(
                "SELECT t FROM AuthenticationToken t WHERE t.token = :token", AuthenticationToken.class
            ).setParameter("token", token)
            .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
