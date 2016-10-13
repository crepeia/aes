package aes.utility;

import aes.controller.ContactController;
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
    
    public Scheduler() {
        
    }
   
    
    @Schedule(second = "0", minute = "0", hour = "7", dayOfWeek = "*")
    public void sendEmails(){
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Morning Task Running");
        contactController.sendScheduledEmails();
    }
    
    
    @Schedule(second = "0", minute = "0", hour = "15", dayOfWeek = "*")
    public void afternoonTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Afternoon task running");
        contactController.sendScheduledEmails();
    }
    
    @Schedule(second = "0", minute = "0", hour = "19", dayOfWeek = "*")
    public void eveningTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Evening task running");
        contactController.sendScheduledEmails();
    }

}