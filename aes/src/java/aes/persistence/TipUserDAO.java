/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Tip;
import aes.model.TipUser;
import aes.model.TipUserKey;
import aes.model.User;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author patri
 */
public class TipUserDAO extends GenericDAO<TipUser> {
    
    
    public TipUserDAO() throws NamingException {
        super(TipUser.class);
    }
    
 
    public TipUser getLatestTip(User user) {
        TipUser tipUser = new TipUser();
        tipUser.setTip(new Tip());
            
        try {
            List<TipUser> tipUserList = this.list("user", user,getEntityManager());
            if(tipUserList.isEmpty()){
                sendNewTip(user);
                tipUserList = this.list("user", user, getEntityManager());
            }
            return (tipUserList.get(tipUserList.size()-1));
        } catch (SQLException ex) {
            Logger.getLogger(TipUserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public void sendNewTip(User user){
        TipUser tipUser = new TipUser();
        tipUser.setTip(new Tip());
        try {
            
            List<Tip> possibleTipsList;
            possibleTipsList = this.getEntityManager().createQuery("SELECT t FROM Tip t WHERE t.id NOT IN (SELECT tu.tip.id FROM TipUser tu WHERE tu.user.id=:userId)")
                    .setParameter("userId", user.getId())
                    .getResultList();


            if (possibleTipsList.isEmpty()) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.WARNING, "Não há dicas a serem enviadas para o usuário " + user.getEmail() + ".");
            } else {
                Random rand = new Random();
                Tip newtip = possibleTipsList.get(rand.nextInt(possibleTipsList.size()));
                Calendar cal = Calendar.getInstance();
                
                TipUserKey tipUserKey = new TipUserKey(newtip.getId(), user.getId());

                tipUser.setId(tipUserKey);
                tipUser.setUser(this.getEntityManager().find(User.class, user.getId()));
                tipUser.setTip(this.getEntityManager().find(Tip.class, newtip.getId()));
                
                tipUser.setDateCreated(cal.getTime());
                
                update(tipUser, this.getEntityManager());               
            }
            
        } catch (SQLException ex) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
