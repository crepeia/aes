/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.model.User;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author patrick
 */
public class ContactDAO extends GenericDAO<Contact>{
    
    private static final int RANDOM_MAX = 67; //amount of tips
    private static final int RANDOM_MIN = 1;
    private GenericDAO evaluationDAO = new GenericDAO<Evaluation>(Evaluation.class);;

    
     public ContactDAO() throws NamingException {
        super(Contact.class);
    }
     
    public void scheduleDiaryReminderEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("email.msg.subject.diary_started_subj");
            contact.setContent("email.msg.subject.diary_started");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            cal.add(Calendar.DATE, 1);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());
        }
    }

    public void schedulePersistChallengesReduceEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("progress_persistchallenges_reduce_subj");
            contact.setContent("progress_persistchallenges_reduce");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            cal.add(Calendar.DATE, 3);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());        }
    }

    public void schedulePersistChallengesQuitEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("progress_persistchallenges_quit_subj");
            contact.setContent("progress_persistchallenges_quit");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            cal.add(Calendar.DATE, 3);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());
        }
    }

    public void scheduleKeepingResultQuitEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("progress_keepingresult_quit_subj");
            contact.setContent("progress_keepingresult_quit");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            cal.add(Calendar.DATE, 3);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());        }
    }

    public void scheduleKeepingResultReduceEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("progress_keepingresult_reduce_subj");
            contact.setContent("progress_keepingresult_reduce");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            cal.add(Calendar.DATE, 3);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());
        }
    }

    public void scheduleAnnualScreeningEmail(User user) throws SQLException {
        Contact contact;
        contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("annualscreening_subj");
        contact.setContent("annualscreening");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        contact.setDateScheduled(cal.getTime());
        this.insertOrUpdate(contact, getEntityManager());

    }

    public void scheduleWeeklyEmail(User user, Date date) throws SQLException {
        Contact contact;
        int weeks[] = {1, 2, 3, 4, 8, 12, 36, 48};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("week_" + week + "_subj");
            contact.setContent("week_" + week);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 7 * week);
            contact.setDateScheduled(cal.getTime());
            this.insertOrUpdate(contact, getEntityManager());
        }
    }

    public void scheduleTipsEmail(User user) throws SQLException {
        Random random = new Random();
        int randomNumber = random.nextInt((RANDOM_MAX - RANDOM_MIN) + 1) + RANDOM_MIN;
        int frequency = user.getTipsFrequency();        
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("tips_subj");
        contact.setContent("tips" + String.valueOf(randomNumber));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, frequency);
        contact.setDateScheduled(cal.getTime());
        this.insertOrUpdate(contact, getEntityManager());
    }

    public void clearScheduledKeepingResultEmails() {
        try {
            List<Contact> contacts = this.list(getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && contact.getSubject().contains("progress_keepingresult")) {
                    if (contact.getUser().getRecord() != null) {
                        this.delete(contact, getEntityManager());
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
      public void clearAnnualScreeningEmails(User user) {
        try {
            List<Contact> contacts = this.list("user", user, getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && contact.getSubject().contains("annualscreening_subj")) {
                        this.delete(contact, getEntityManager());
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearScheduledEmails(User user) {
        try {
            List<Contact> contacts = this.list("user", user, getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && !contact.getSubject().contains("tips_subj")) {
                    this.delete(contact, getEntityManager());

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactDAO.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }



    /*private String getSubject(Contact contact) throws MissingResourceException {
        return getString(contact.getSubject(), contact.getUser());
    }
    
    private String getString(String key, User user) throws MissingResourceException{
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }*/

    private Evaluation getLatestEvaluation(User user) {
        try {
            List evaluations = evaluationDAO.listOrdered("user", user, "date_created", getEntityManager());
            if (!evaluations.isEmpty()) {
               return  (Evaluation) evaluations.get(evaluations.size() - 1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
        
}