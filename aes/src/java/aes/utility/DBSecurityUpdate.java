/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.model.User;
import aes.persistence.GenericDAO;
import aes.service.UserFacadeREST;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author patri
 */

@TransactionManagement(TransactionManagementType.BEAN)
public class DBSecurityUpdate {
    //@PersistenceContext(unitName = "aesPU")
    private static EntityManager em;
    private static GenericDAO<User> userDAO;
    
    @Resource
    private static UserTransaction userTransaction;
    
    public static void run(){
        
        em = Persistence.createEntityManagerFactory("aesPU").createEntityManager();
        Logger.getLogger(DBSecurityUpdate.class.getName()).log(Level.INFO, "Running DBSecurityUpdate test");
        List<User> users = em.createQuery("SELECT u FROM User u where u.password is not null", User.class).getResultList();
        
        try{
            userDAO = new GenericDAO(User.class);
            
            for(User u: users){
        
                String oldEncryptedPassword = new String(Hex.encodeHex(u.getPassword()));
                String oldDecryptedPassword =Encrypter.decrypt(oldEncryptedPassword);       
                byte[] salt = Encrypter.generateRandomSecureSalt(16);
                byte[] newHash = Encrypter.hashPassword(oldDecryptedPassword, salt);
                //String newHash = new String(Hex.encodeHex(Encrypter.hashPassword(oldDecryptedPassword, salt)));
                
                Logger.getLogger(DBSecurityUpdate.class.getName()).log(Level.INFO, "Reencrypted: "+  u.getEmail()); 
               
                u.setPassword(newHash);
                u.setSalt(salt);
           
                Logger.getLogger(DBSecurityUpdate.class.getName()).log(Level.INFO, "Committing..."); 
                userDAO.insertOrUpdate(u, em);
                Logger.getLogger(DBSecurityUpdate.class.getName()).log(Level.INFO, "Commited"); 

            }
        }catch(Exception ex){
            Logger.getLogger(DBSecurityUpdate.class.getName()).log(Level.SEVERE,null, ex);
        }
    }
    
}
