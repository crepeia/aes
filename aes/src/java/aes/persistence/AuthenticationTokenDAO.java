/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AnonymousKey;
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
    
    
    public AuthenticationToken issueToken(User usr, EntityManager entityManager) throws SQLException{
        String token = SecureRandomString.generate();
        
        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken(token);
        authToken.setUser(usr);
        authToken.setDateCreated(new Date());
        super.update(authToken, entityManager);
        
        return authToken;
    }
    
    public String issueAnonymousToken(AnonymousKey anonymousKey, Long userId, EntityManager em) throws SQLException {
        // Use o identificador para criar um token anonimo
        String token = SecureRandomString.generate() + "-" + "anonymous";
        
        // Salvar o token para rastreamento
        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken(token);
        
        authToken.setUser(null);
        if (userId != null) {
            User usr = (User) em.createQuery("SELECT u From User u WHERE u.id=:userId")
                .setParameter("userId", userId)
                .getSingleResult();
            
            authToken.setUser(usr);
        }
        
        authToken.setAnonymousKey(anonymousKey);
        authToken.setDateCreated(new Date());
        super.update(authToken, em);
        
        return token;
    }
    
    public void revokeToken(String token, String userEmail, EntityManager entityManager) throws SQLException {
        AuthenticationToken at = (AuthenticationToken) entityManager.createQuery("SELECT at FROM AuthenticationToken at WHERE at.token=:token AND at.user.email=:uEmail")
        .setParameter("token", token)
        .setParameter("uEmail", userEmail)
        .getSingleResult();
        super.delete(at, entityManager);
    
    }
    
    public void revokeAnonymousToken(String token, String instanceId, EntityManager entityManager) throws SQLException {
        AuthenticationToken at = findByToken(token, entityManager);
        
        if (at == null) {
            throw new SQLException("Token não encontrado.");
        }
        
        // Obtém a AnonymousKey associada ao token
        AnonymousKey ak = entityManager.find(AnonymousKey.class, at.getAnonymousKey().getId());
        
        if (ak == null) {
            throw new SQLException("Chave anônima não encontrada.");
        }
        
        // Verifica se o instance_id é o mesmo
        if (!ak.getInstanceId().equals(instanceId)) {
            throw new SQLException("Instance ID não corresponde ao token.");
        }
        
        // Revoga o token
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
    
    public AuthenticationToken updateToken(String oldToken, String userEmail, EntityManager entityManager) throws SQLException {
        AuthenticationToken existingToken = entityManager.createQuery(
            "SELECT at FROM AuthenticationToken at WHERE at.token = :token AND at.user.email = :email", AuthenticationToken.class
        ).setParameter("token", oldToken)
        .setParameter("email", userEmail)
        .getSingleResult();
        
        // Gera novo token
        String newToken = SecureRandomString.generate();
        existingToken.setToken(newToken);
        existingToken.setDateCreated(new Date());

        super.update(existingToken, entityManager);

        return existingToken;
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
