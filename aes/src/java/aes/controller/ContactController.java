package aes.controller;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import aes.utility.EMailSSL;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;
import javax.naming.NamingException;

@ManagedBean(name = "contactController")
@ApplicationScoped
public class ContactController extends BaseController implements Serializable {

    private static final int RANDOM_MAX = 67; //amount of tips
    private static final int RANDOM_MIN = 1;

    private EMailSSL eMailSSL;
    private String htmlTemplate;
    private GenericDAO evaluationDAO;
    private Random random;
    private ResourceBundle bundle;


    @PostConstruct
    public void init() {
        eMailSSL = new EMailSSL();
        htmlTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        random = new Random();
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
            evaluationDAO = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendContactFormEmail(ActionEvent event) {
        String message = (String) event.getComponent().getAttributes().get("message");
        User user = (User) event.getComponent().getAttributes().get("user");
        Contact contact = new Contact();
        if (user.getId() != 0) {
            contact.setUser(user);
        }
        contact.setSender(user.getEmail());
        contact.setRecipient("alcoolesaude@gmail.com");
        contact.setSubject("Contato via formulario - " + user.getEmail());
        contact.setContent(message);
        sendPlainTextEmail(contact);
        FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.sent", user), null));

    }

    public void sendPasswordRecoveryEmail(User user) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("passwordrecovery_subj");
        contact.setContent("passwordrecovery");
        sendHTMLEmail(contact);
    }

    public void sendPlanEmail(User user, String attachment, ByteArrayOutputStream pdf) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("plan_subj");
        contact.setContent("plan");
        contact.setAttachment(attachment);
        contact.setPdf(pdf);
        sendHTMLEmail(contact);
    }

    public void sendRecordEmail(User user, String attachment, ByteArrayOutputStream pdf) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("record_subj");
        contact.setContent("record");
        contact.setAttachment(attachment);
        contact.setPdf(pdf);
        sendHTMLEmail(contact);
    }

    public void sendSignUpEmail(User user) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("welcome_subj");
        contact.setContent("welcome");
        sendHTMLEmail(contact);
    }

    public void scheduleDiaryReminderEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void schedulePersistChallengesReduceEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void schedulePersistChallengesQuitEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void scheduleKeepingResultQuitEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void scheduleKeepingResultReduceEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void scheduleAnnualScreeningEmail(User user) {
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
        save(contact);

    }

    public void scheduleWeeklyEmail(User user, Date date) {
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
            save(contact);
        }
    }

    public void scheduleTipsEmail(User user) {
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
        save(contact);
    }

    public void clearScheduledKeepingResultEmails() {
        try {
            List<Contact> contacts = daoBase.list(getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && contact.getSubject().contains("progress_keepingresult")) {
                    if (contact.getUser().getRecord() != null) {
                        daoBase.delete(contact, getEntityManager());
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
      public void clearAnnualScreeningEmails(User user) {
        try {
            List<Contact> contacts = daoBase.list("user", user, getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && contact.getSubject().contains("annualscreening_subj")) {
                        daoBase.delete(contact, getEntityManager());
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearScheduledEmails(User user) {
        try {
            List<Contact> contacts = daoBase.list("user", user, getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && !contact.getSubject().contains("tips_subj")) {
                    daoBase.delete(contact, getEntityManager());

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendScheduledEmails() {
        try {
            List<Contact> contacts = daoBase.list(getEntityManager());
            Calendar today = Calendar.getInstance();
            Calendar scheduledDate = Calendar.getInstance();
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
                    scheduledDate.setTime(contact.getDateScheduled());
                    if (today.compareTo(scheduledDate) >= 0) {
                        sendHTMLEmail(contact);
                        if(contact.getSubject().contains("annualscreening_subj")){
                            scheduleAnnualScreeningEmail(contact.getUser());
                        }
                        if(contact.getSubject().contains("tips_subj")){
                            scheduleTipsEmail(contact.getUser());
                        }
                    }
                   
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendHTMLEmail(Contact contact) {
        try {
            String content = getContent(contact);
            String subject = getSubject(contact);
            eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
            save(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

        } catch (MessagingException |  MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendPlainTextEmail(Contact contact) {
        try {
            eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getContent(), contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
            save(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void save(Contact contact) {
        try {
            daoBase.insertOrUpdate(contact, getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String readHTMLTemplate(String path) {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            byte[] buffer = new byte[102400];
            return new String(buffer, 0, input.read(buffer), StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getContent(Contact contact) throws MissingResourceException {
        String htmlMessage = htmlTemplate;
        htmlMessage = htmlMessage.replace("#title#", getString("title.1",contact.getUser()));
        htmlMessage = htmlMessage.replace("#content#", getString(contact.getContent(),contact.getUser()));
        htmlMessage = htmlMessage.replace("#footer#",
                getString("title.1",contact.getUser()) + "<br>"
                + getString("crepeia",contact.getUser()) + "<br>"
                + getString("ufjf",contact.getUser()));
        htmlMessage = htmlMessage.replace("#user#", contact.getUser().getName());
        htmlMessage = htmlMessage.replace("#email#", contact.getUser().getEmail());
        htmlMessage = htmlMessage.replace("#code#", String.valueOf(contact.getUser().getRecoverCode()));
        htmlMessage = htmlMessage.replace("#id#", contact.getUser().getHashedId());
        htmlMessage = htmlMessage.replace("#messageid#", contact.getContent());
        return htmlMessage;
    }

    private String getSubject(Contact contact) throws MissingResourceException {
        return getString(contact.getSubject(), contact.getUser());
    }
    
    private String getString(String key, User user) throws MissingResourceException{
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

    private Evaluation getLatestEvaluation(User user) {
        try {
            List evaluations = evaluationDAO.listOrdered("user", user, "date_created", getEntityManager());
            if (!evaluations.isEmpty()) {
               return  (Evaluation) evaluations.get(evaluations.size() - 1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
