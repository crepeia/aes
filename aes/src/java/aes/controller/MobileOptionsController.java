/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.MobileOptions;
import aes.model.TipUser;

import aes.persistence.GenericDAO;
import aes.utility.ExpoNotification;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author bruno
 */
@Named("mobileOptionsController")
@ApplicationScoped
public class MobileOptionsController extends BaseController<MobileOptions> {
    
    private MobileOptions mobileOptions;
    private ExpoNotification expoNotification;
    
    @Inject
    private TipUserController tipUserController;
    
    public MobileOptionsController() {
    }
    
    @PostConstruct
    public void init() {
        mobileOptions = new MobileOptions();
        expoNotification = new ExpoNotification();
        try {
            daoBase = new GenericDAO<>(MobileOptions.class);
            daoBase.setEntityManager(getEntityManager());

        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMobileTips(){
        try {
            List<MobileOptions> mobileOptionsList = daoBase.list();
            for (MobileOptions mo : mobileOptionsList) {
                tipUserController.sendNewTip(mo.getUser());
            }
        } catch (SQLException ex) {
            Logger.getLogger(MobileOptionsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void sendScheduledNotifications() {
        try {
            List<MobileOptions> mobileOptionsList = daoBase.list();
            Calendar today = Calendar.getInstance();
            
            int todayHour = today.get(Calendar.HOUR_OF_DAY);
            
            for (MobileOptions mo : mobileOptionsList) {
                if (mo.getTipNotificationTime() != null && mo.isAllowTipNotifications()) {
                    TipUser tu = tipUserController.getLatestTip(mo.getUser());
                    
                    
                    
                    int scheduledHour = mo.getTipNotificationTime().getHour();
                    
                    if (todayHour == scheduledHour) {
                        expoNotification.send(mo.getNotificationToken(), tu);
                    }
                   
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
