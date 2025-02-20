/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.controller.UserController;
import aes.model.Chat;
import aes.model.User;
import aes.service.UserFacadeREST;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
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

    
    public User setInRanking(String userEmail, EntityManager entityManager) throws SQLException{
                    
        User u = (User) entityManager.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", userEmail)
                .getSingleResult();
        /*System.out.println(u.getEmail());
            System.out.println(u.isInRanking());
            System.out.println(u.getNickname());*/

        u.setInRanking(u.isInRanking());
        u.setNickname(u.getNickname());

        super.insertOrUpdate(u, entityManager);
        return u;
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
        User u = (User) entityManager.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", email)
                .getSingleResult();
        System.out.println(u.getEmail());
        u.setRecoverCode(GenerateCode.generate());

        super.insertOrUpdate(u, entityManager);
        // contactController.sendPasswordRecoveryEmail(u);

        return u;
    }

    public void setInRanking(String userEmail, Boolean inRanking, String nickname, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", userEmail)
                .getSingleResult();
        /*System.out.println(u.getEmail());
            System.out.println(u.isInRanking());
            System.out.println(u.getNickname());*/

        u.setInRanking(inRanking);
        u.setNickname(nickname);

        super.insertOrUpdate(u, em);
    }
    
    public User getUserByID(Long userId, EntityManager em){
        
        User user = (User) em.createQuery("SELECT u FROM User u WHERE u.id=:userId")
            .setParameter("userId", userId)
            .getSingleResult();
        return user;
    }
    
    public void setSendTCLE(String userEmail, User entity, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", userEmail)
                .getSingleResult();

        u.setDt_tcle_response(entity.getDt_tcle_response());

        super.insertOrUpdate(u, em);
    }
    
    public void setTitle(String userEmail, User entity, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", userEmail)
                .getSingleResult();

        u.setSelected_title(entity.getSelected_title());

        super.insertOrUpdate(u, em);
    }
    
    public void updateEvaluationProfile(String userEmail, User entity, EntityManager em) throws SQLException {
        User u = (User) em.createQuery("SELECT u from User u WHERE u.email = :email")
                .setParameter("email", userEmail)
                .getSingleResult();
        
        if (entity.getEducation() != null) {
            u.setEducation(entity.getEducation());
        }
        if (entity.getEmployed() != null) {
            u.setEmployed(entity.getEmployed());
        }
        if (entity.getKnowWebsite() != null) {
            u.setKnowWebsite(entity.getKnowWebsite());
        }
        
        super.insertOrUpdate(u, em);
    }

    public void uptadeUser(User user, EntityManager entityManager) throws SQLException {
        super.insertOrUpdate(user, entityManager);
    }

}
