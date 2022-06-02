package aes.utility;

import aes.controller.ContactController;
import aes.controller.MobileOptionsController;
import aes.controller.TipUserController;
import aes.model.Contact;
import aes.persistence.ContactDAO;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class Scheduler {

    @Inject
    private ContactController contactController;
    @Inject
    private MobileOptionsController mobileOptionsController;
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ContactDAO contactDAO;
    
    public Scheduler() {
        try {
            contactDAO = new ContactDAO(em);
        } catch (NamingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    /*@Schedule(second = "0", minute = "0", hour = "7", dayOfWeek = "*")
    public void sendEmails(){
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Morning Task Running");
        contactController.sendScheduledEmails();
        //contactController.sendTestEmail();
    }
    
    
    @Schedule(second = "0", minute = "0", hour = "15", dayOfWeek = "*")
    public void afternoonTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Afternoon task running");
        contactController.sendScheduledEmails();
        //contactController.sendTestEmail();

    }
    
    @Schedule(second = "0", minute = "0", hour = "19", dayOfWeek = "*")
    public void eveningTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Evening task running");
        contactController.sendScheduledEmails();
        //contactController.sendTestEmail();

    }
    
    @Schedule(second = "0", minute = "5", hour = "1", dayOfWeek = "*")
    public void sendTips() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Send Mobile Tips task running");
        mobileOptionsController.sendMobileTips();
    }
    
    @Schedule(second = "0", minute = "0", hour = "*", dayOfWeek = "*", persistent = false)
    public void appNotificationTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - App Notification task running");
        mobileOptionsController.sendScheduledNotifications();
        
    }*/
    
    /*@Schedule(second = "30", minute = "0", hour = "8", dayOfWeek = "*", persistent=false)
    public void securityUpdate() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "TEST DBUPDATE STARTING");
        DBSecurityUpdate.run();
        
    }*/
    
     /* @Schedule(second = "*", minute = "*", hour = "*", dayOfWeek = "*", persistent=false)
    public void testTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - test task running");
      //  mobileOptionsController.sendScheduledNotifications();
        
    }*/
    
        public void sendScheduledEmails(){
        try {
            List<Contact> contacts = contactDAO.list();
            Calendar today = Calendar.getInstance();
            Calendar scheduledDate = Calendar.getInstance();
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
                    scheduledDate.setTime(contact.getDateScheduled());
                    if (today.compareTo(scheduledDate) >= 0) {
                        if(contact.getSubject().contains("tips_subj")){
                            //todo: inserir essas funções quando EmailHelper estiver organizado
                            //sendTipsEmail(contact);
                            contactDAO.scheduleTipsEmail(contact.getUser());
                        }else{
                            //sendHTMLEmail(contact);
                            if(contact.getSubject().contains("annualscreening_subj")){
                                contactDAO.scheduleAnnualScreeningEmail(contact.getUser());
                            }
                        }
                    }
                   
                }
            }
        } catch (SQLException |MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}