/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.User;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


/**
 *
 * @author patrick
 */
public class UserDAO extends GenericDAO<User>{
    private ContactDAO contactDAO;
    
    
     public UserDAO(EntityManager entityManager) throws NamingException {
        super(User.class);
        contactDAO = new ContactDAO(entityManager);
    }
     

    public User checkCredentials(String email, String providedPassword) throws SQLException, EncrypterException{
        List<User> userList = this.list("email", email);
        
        if(!userList.isEmpty() && Encrypter.compareHash(providedPassword, userList.get(0).getPassword(), userList.get(0).getSalt())){
            return userList.get(0);
        }
        return null;

    }

    public void createUser(User entity, String passwordString) throws SQLException, EncrypterException {
       /// List<User> userList = getEntityManager().createQuery("SELECT u FROM User u WHERE u.email=:e").setParameter("e", entity.getEmail()).getResultList();
        
        //if (!userList.isEmpty()) {
            //todo: throw exception
       /// } else {
            //try {

                byte[] salt =  Encrypter.generateRandomSecureSalt(16);
                entity.setSalt(salt);
                entity.setPassword(Encrypter.hashPassword(passwordString, salt));
                
                insertOrUpdate(entity);

                Logger.getLogger(UserDAO.class.getName()).log(Level.INFO, "Usuário '" + entity.getEmail() + "'cadastrou no sistema.");
                        
            // } catch (SQLException |SecurityException | IllegalStateException ex) {
             //   Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);

           // } catch (EncrypterException ex) {
             //   Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
          //  } 
            
       // }
        
    }
    
    
        public void createRecoveryCode(User user) {
        try {
           // List<User> userList = this.list("email", u, this.getEntityManager());
            //if (userList.isEmpty()) {
                //todo: throw exception: user does't exist
               // FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.not.registred"), null));
           // } else {
                //User foundUser = userList.get(0);
                user.setRecoverCode(GenerateCode.generate());
                this.insertOrUpdate(user);
                //contactController.sendPasswordRecoveryEmail(foundUser);
                //FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.instructions.password"), null));
           // }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public User login(String token) {
        User at = (User) getEntityManager().createQuery("SELECT u FROM AuthenticationToken a INNER JOIN a.user AS u WHERE a.token=:t").setParameter("t", token).getSingleResult();
        return at;
    }
    
    public User login(String e, String p) throws DecoderException{
        byte[] b =  Hex.decodeHex(p.toCharArray());
        return (User) getEntityManager().createNamedQuery("User.login").setParameter("email", e).setParameter("password", b).getSingleResult();
    }
   
    
    public User setInRanking(String userEmail) throws SQLException{
                    
            User u = (User) getEntityManager().createQuery("SELECT u from User u WHERE u.email = :email")
                                .setParameter("email", userEmail)
                                .getSingleResult();
            /*System.out.println(u.getEmail());
            System.out.println(u.isInRanking());
            System.out.println(u.getNickname());*/

            u.setInRanking(u.isInRanking());
            u.setNickname(u.getNickname());

            super.insertOrUpdate(u);
            return u;
    }
    
    public User toggleConsultant(String email) throws SQLException{
        User u = (User) getEntityManager().createQuery("SELECT u from User u WHERE u.email = :email")
                              .setParameter("email", email)
                              .getSingleResult();
          u.setConsultant(!u.isConsultant());
          super.insertOrUpdate(u);

        return u;
    }
    
    
    public User generateRecoverCode(String email) throws SQLException{
        User u = (User) getEntityManager().createQuery("SELECT u from User u WHERE u.email = :email")
                    .setParameter("email", email)
                    .getSingleResult();
            System.out.println(u.getEmail());
            u.setRecoverCode(GenerateCode.generate());

            super.insertOrUpdate(u);
           // contactController.sendPasswordRecoveryEmail(u);
           
           return u;
    }
        
}
