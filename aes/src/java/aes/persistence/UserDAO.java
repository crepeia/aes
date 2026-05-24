/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.controller.UserController;
import aes.model.AuthenticationToken;
import aes.model.Chat;
import aes.model.User;
import aes.service.UserFacadeREST;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


/**
 *
 * @author patrick
 */
public class UserDAO extends GenericDAO<User>{
    private ContactDAO contactDAO = new ContactDAO();

    
    public UserDAO() throws NamingException {
        super(User.class);
    }


    public User checkCredentials(String email, String providedPassword, EntityManager entityManager) throws SQLException, EncrypterException{
        List<User> userList = this.list("email", email, entityManager);

        if(!userList.isEmpty() && Encrypter.compareHash(providedPassword, userList.get(0).getPassword(), userList.get(0).getSalt())){
            return userList.get(0);
        }
        return null;

    }

    public void createUser(User entity, String passwordString, EntityManager entityManager) throws SQLException, EncrypterException {
        /// List<User> userList = getEntityManager().createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();

        //if (!userList.isEmpty()) {
        //todo: throw exception
        /// } else {
        //try {

                byte[] salt =  Encrypter.generateRandomSecureSalt(16);
        entity.setSalt(salt);
        entity.setPassword(Encrypter.hashPassword(passwordString, salt));

        insertOrUpdate(entity, entityManager);

        Logger.getLogger(UserDAO.class.getName()).log(Level.INFO, "Usuário '" + entity.getEmail() + "'cadastrou no sistema.");

        // } catch (SQLException |SecurityException | IllegalStateException ex) {
        //   Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);

        // } catch (EncrypterException ex) {
        //   Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        //  } 
            
        // }   
    }
    
    public void createAnonymousUser(User entity, EntityManager entityManager) throws SQLException {
        super.insertOrUpdate(entity, entityManager);
    }

    
    public void createRecoveryCode(User user, EntityManager entityManager) {
        try {
            // List<User> userList = this.list("email", u, this.getEntityManager());
            //if (userList.isEmpty()) {
            //todo: throw exception: user does't exist
            // FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.not.registred"), null));
            // } else {
            //User foundUser = userList.get(0);
            user.setRecoverCode(GenerateCode.generate());
            this.insertOrUpdate(user, entityManager);
            //contactController.sendPasswordRecoveryEmail(foundUser);
            //FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.instructions.password"), null));
            // }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public User login(String token, EntityManager entityManager) {
        User at = (User) entityManager.createQuery("SELECT u FROM AuthenticationToken a INNER JOIN a.user AS u WHERE a.token=:t").setParameter("t", token).getSingleResult();
        return at;
    }

    public User login(String e, String p) throws DecoderException{
        byte[] b =  Hex.decodeHex(p.toCharArray());
        return (User) getEntityManager().createNamedQuery("User.login").setParameter("email", e).setParameter("password", b).getSingleResult();
    }

    
    public void setInRanking(Long userId, Boolean inRanking, String nickname, EntityManager entityManager) throws SQLException{
        User u = (User) entityManager.createQuery("SELECT u FROM User u WHERE u.id =: userId")
            .setParameter("userId", userId)
            .getSingleResult();
        
        u.setInRanking(inRanking);
        u.setNickname(nickname);

        super.insertOrUpdate(u, entityManager);
    }

    public User toggleConsultant(String email, EntityManager entityManager) throws SQLException {
        User u = (User) entityManager.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", email)
                .getSingleResult();
        u.setConsultant(!u.isConsultant());
        super.insertOrUpdate(u, entityManager);

        return u;
    }

    public User generateRecoverCode(String email, EntityManager entityManager) throws SQLException {
        List<User> result = entityManager.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", email)
                .getResultList();

        if (result.isEmpty()) {
            throw new SQLException("Usuário não encontrado para o email: " + email);
        }

        User u = result.get(0);
        u.setRecoverCode(GenerateCode.generate());
        super.insertOrUpdate(u, entityManager);

        return u;
    }
    
    public User getUserByID(Long userId, EntityManager em){
        
        User user = (User) em.createQuery("SELECT u FROM User u WHERE u.id=:userId")
            .setParameter("userId", userId)
            .getSingleResult();
        return user;
    }
    
    public void setSendTCLE(Long userId, EntityManager entityManager) throws SQLException {
        User u = (User) entityManager.createQuery("SELECT u FROM User u WHERE u.id =: id")
                .setParameter("id", userId)
                .getSingleResult();
        
        u.setDt_tcle_response(new Date());
        super.insertOrUpdate(u, entityManager);
    }
    
    public void setTitle(Long userId, Long selected_title, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.id =: id")
                .setParameter("id", userId)
                .getSingleResult();

        u.setSelected_title(selected_title);

        super.insertOrUpdate(u, em);
    }
    
    public void updateEvaluationProfile(Long userId, User entity, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.id =: id")
                .setParameter("id", userId)
                .getSingleResult();

        u.setEducation(entity.getEducation());
        u.setEmployed(entity.getEmployed());
        u.setKnowWebsite(entity.getKnowWebsite());

        super.insertOrUpdate(u, em);
    }

    public void uptadeUser(User user, EntityManager entityManager) throws SQLException {
        super.insertOrUpdate(user, entityManager);
    }
    
    public User findByReferralCode(String referralCode, EntityManager entityManager) {
        try {
            List<User> result = entityManager.createQuery("SELECT u FROM User u WHERE u.myReferralCode =: mRC")
                .setParameter("mRC", referralCode)
                .setMaxResults(1)
                .getResultList();
            
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Erro ao buscar por código de referência", ex);
            throw new RuntimeException("Erro ao buscar por código de referência", ex);
        }
    }

    public long countReferralCodeUsage(String referralCode, EntityManager entityManager) {
        return (long) entityManager.createQuery("SELECT COUNT(*) FROM User u WHERE u.friendReferralCode =: rf")
            .setParameter("rf", referralCode)
            .getSingleResult();
    }

    public void updateReferralCode(Long userId, String referralCode, EntityManager em) throws Exception {
        User user = em.find(User.class, userId);
        user.setMyReferralCode(referralCode);
        super.insertOrUpdate(user, em);
    }
    
    public List<User> findByEmail(String email, EntityManager entityManager) {
        return (List<User>) entityManager.createQuery("SELECT u From User u WHERE u.email=:e")
                .setParameter("e", email).getResultList();
    }
    
    public User findByUnauthenticatedId(String id, EntityManager entityManager) {
        List<User> result = entityManager.createQuery("SELECT u FROM User u WHERE u.unauthenticatedId = :id ORDER BY u.id DESC", User.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        
        return result.isEmpty() ? null : result.get(0);
    }
    
    public List<User> findUsersByConsultantId(Long consultantId, EntityManager entityManager) {
        return (List<User>) entityManager.createQuery("SELECT u FROM User u WHERE u.relatedConsultant.id =: consultantId", User.class)
                .setParameter("consultantId", consultantId)
                .getResultList();
    }
    
    public User findUserByToken(String token, EntityManager entityManager) {
        return (User) entityManager.createQuery(
            "SELECT a.user FROM AuthenticationToken a WHERE a.token=:t").setParameter("t", token).getSingleResult();
    }
}
