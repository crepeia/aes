/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.TipUser;
import aes.model.Tip;
import aes.model.TipUserKey;
import aes.model.User;


import aes.persistence.GenericDAO;
import aes.persistence.TipUserDAO;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.naming.NamingException;

/**
 *
 * @author bruno
 */
@Named("tipUserController")
@RequestScoped
public class TipUserController  extends BaseController<TipUser> {

    TipUser tipUser;
    
    @PostConstruct
    public void init() {
        tipUser = new TipUser();
        
        try {
            daoBase = new GenericDAO<>(TipUser.class);
        } catch (NamingException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public TipUser getTipUser() {
        return tipUser;
    }

    public void setTipUser(TipUser tipUser) {
        this.tipUser = tipUser;
    }

    
    
    public TipUser getLatestTip(User user) {
        try {
            List<TipUser> tipUserList = this.getDaoBase().list("user", user, getEntityManager());
            if(tipUserList.isEmpty()){
                sendNewTip(user);
                tipUserList = this.getDaoBase().list("user", user, getEntityManager());
            }
            return (tipUserList.get(tipUserList.size()-1));
        } catch (SQLException ex) {
            Logger.getLogger(TipUserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    public void sendNewTip(User user){
        try {
            List<Long> tipsIds = new ArrayList<Long>();
            
            Properties properties = new Properties();
            String propertiesPath = "aes/utility/messages_pt.properties";
            
            InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesPath);
            properties.load(input);
            
            boolean continueSearch = true;
            int counter = 1;
            
            while(continueSearch) {
                String key = properties.getProperty("tip.description." + counter, "");
                if (key.isEmpty()) {
                    continueSearch = false;
                } else {
                    tipsIds.add(Long.parseLong(key));
                    counter++;
                }
            }
            
            List<Long> tipUserIds;
            tipUserIds = this.getEntityManager().createQuery("SELECT tu.tipId FROM TipUser tu WHERE tu.user.id=:userId")
                    .setParameter("userId", user.getId())
                    .getResultList();
            
            List<Long> possibleTipsList = new ArrayList<Long>();
            
            for(Long tip: tipUserIds) {
                if (!tipsIds.contains(tip)) {
                    possibleTipsList.add(tip);
                }
            }

            if (possibleTipsList.isEmpty()) {
               // FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Não há dicas a serem enviadas para o usuário " + user.getEmail() + ".", null));
                Logger.getLogger(TipUserController.class.getName()).log(Level.WARNING, "Não há dicas a serem enviadas para o usuário " + user.getEmail() + ".");
            } else {
                Random rand = new Random();
                Long tipId = possibleTipsList.get(rand.nextInt(possibleTipsList.size()));
                Calendar cal = Calendar.getInstance();
                
                TipUserKey tipUserKey = new TipUserKey(tipId, user.getId());

                tipUser.setId(tipUserKey);
                tipUser.setUser(this.getEntityManager().find(User.class, user.getId()));
                tipUser.setTipId(tipId);
                
                tipUser.setDateCreated(cal.getTime());
                
                daoBase.update(tipUser, this.getEntityManager());
                
                //tipUser = null;
            }
            
        } catch (SQLException | IOException ex) {
                Logger.getLogger(TipUserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
