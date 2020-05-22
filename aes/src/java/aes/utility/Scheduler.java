package aes.utility;

import aes.controller.ContactController;
import aes.controller.MobileOptionsController;
import aes.controller.TipUserController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class Scheduler {

    @Inject
    private ContactController contactController;
    @Inject
    private MobileOptionsController mobileOptionsController;
    
    
    
    
    public Scheduler() {
        
    }
   
    
    @Schedule(second = "0", minute = "0", hour = "7", dayOfWeek = "*")
    public void sendEmails(){
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Morning Task Running");
        contactController.sendScheduledEmails();
        contactController.sendTestEmail();
    }
    
    
    @Schedule(second = "0", minute = "0", hour = "15", dayOfWeek = "*")
    public void afternoonTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Afternoon task running");
        contactController.sendScheduledEmails();
        contactController.sendTestEmail();

    }
    
    @Schedule(second = "0", minute = "0", hour = "19", dayOfWeek = "*")
    public void eveningTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Evening task running");
        contactController.sendScheduledEmails();
        contactController.sendTestEmail();

    }
    
    @Schedule(second = "0", minute = "5", hour = "1", dayOfWeek = "*")
    public void sendTips() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Send Mobile Tips task running");
        mobileOptionsController.sendMobileTips();

    }
    
    @Schedule(second = "0", minute = "10", hour = "*", dayOfWeek = "*")
    public void appNotificationTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - App Notification task running");
        mobileOptionsController.sendScheduledNotifications();
        
    }

}