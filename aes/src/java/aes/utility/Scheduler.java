package aes.utility;

import aes.controller.ContactController;
import aes.controller.MobileOptionsController;
import aes.controller.TipUserController;
import aes.model.Contact;
import aes.persistence.ContactDAO;
import aes.service.UserFacadeREST;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
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
    private EmailHelper emailHelper;

    public Scheduler() {
        emailHelper = new EmailHelper();
        try {
            contactDAO = new ContactDAO();
            contactDAO.setEntityManager(em);
        } catch (NamingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(second = "0", minute = "0", hour = "7", dayOfWeek = "*")
    public void sendEmails() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Morning Task Running");
        sendScheduledEmails();
        //contactController.sendTestEmail();
    }

    @Schedule(second = "0", minute = "0", hour = "15", dayOfWeek = "*")
    public void afternoonTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Afternoon task running");
        sendScheduledEmails();
        //contactController.sendTestEmail();

    }

    @Schedule(second = "0", minute = "0", hour = "19", dayOfWeek = "*")
    public void eveningTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Evening task running");
        sendScheduledEmails();
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

    }
    
     /*   @Schedule(second = "30", minute = "09", hour = "14", dayOfWeek = "*")
    public void sendEmailsTest() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - Test Task Running");
        try {
            emailHelper.sendTestEmail("patrickcarvalho448@gmail.com");
            //sendScheduledEmails();
            //contactController.sendTestEmail();
        } catch (SQLException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    
      /*@Schedule(second = "30", minute = "15", hour = "14", dayOfWeek = "*", persistent=false)
    public void testTask() {
        Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "AES - tip correction task running");
      contactDAO.scheduleAllTipsEmails(em);
        
    }*/

    public void sendScheduledEmails() {

        Date today = new Date();
        List<Contact> contacts = contactDAO.getScheduledEmailsToDate(em, today);
     
        for (Contact contact : contacts) {
            try {
                if (contact.getSubject().contains("tips_subj")) {
                    emailHelper.sendTipsEmail(contact, em);
                   contactDAO.scheduleTipsEmail(contact.getUser(), em);
                } else {
                    emailHelper.sendHTMLEmail(contact, em);
                    if (contact.getSubject().contains("annualscreening_subj")) {
                      contactDAO.scheduleAnnualScreeningEmail(contact.getUser(), em);
                    }
                }

            }catch (SQLException ex){
                Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "Envio de email não pôde ser registrado. ");
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }catch(MessagingException ex){
                Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "Envio de email não pôde ser realizado. ");
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
