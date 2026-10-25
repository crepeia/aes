package aes.controller;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import aes.utility.EMailSSL;
import aes.utility.MessageResources;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.naming.NamingException;


@Named("contactController")
@ApplicationScoped
public class ContactController extends BaseController implements Serializable {

    private static final int RANDOM_MAX = 67; //amount of tips
    private static final int RANDOM_MIN = 1;

    private EMailSSL eMailSSL;
    private String htmlTemplate;
    private String tipsTemplate;
    private GenericDAO evaluationDAO;
    private Random random;


    @PostConstruct
    public void init() {
        eMailSSL = new EMailSSL();
        htmlTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        tipsTemplate = readHTMLTemplate("aes/utility/tips-template.html");
        random = new Random();
        System.out.println("PostConstruct!!");
        daoBase = new GenericDAO<Contact>(Contact.class);
        evaluationDAO = new GenericDAO<Evaluation>(Evaluation.class);
    }

    public void sendContactFormEmail(ActionEvent event) {
        String message = (String) event.getComponent().getAttributes().get("message");
        User user = (User) event.getComponent().getAttributes().get("user");
        Contact contact = new Contact();
        if (user.getId() != 0) {
            contact.setUser(user);
        }
        contact.setSender(user.getEmail());
        contact.setRecipient(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setSubject("Contato via formulario - " + user.getEmail());
        contact.setContent(message);
        sendPlainTextEmail(contact);
        FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("emailSent", user), null));

    }

    public void sendPasswordRecoveryEmail(User user) throws MessagingException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient(user.getEmail());
        contact.setSubject("passwordrecoverySubj");
        contact.setContent("passwordrecovery");
        sendHTMLEmail(contact);
    }
    
    public void sendDeleteAccountEmail(User user) throws MessagingException, SQLException {
        String token = "";
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setSubject("deleteaccountSubj");
        contact.setContent("deleteaccount");
        sendHTMLEmailDeleteAccount(contact,token);
            
    }
    
    private void sendHTMLEmailDeleteAccount(Contact contact,String token) throws MessagingException, MissingResourceException, SQLException {
        String content = getContentDeleteAccount(contact, htmlTemplate,token);
        String subject = getSubject(contact);
        System.out.println(content);
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());
    }
    
    private String getContentDeleteAccount(Contact contact, String template,String token) throws MissingResourceException {
        String htmlMessage = template;
        htmlMessage = htmlMessage.replace("#title#", getString("title1", contact.getUser()));
        htmlMessage = htmlMessage.replace("#content#", getString(contact.getContent(), contact.getUser()));
        htmlMessage = htmlMessage.replace("#footer#",
                getString("title1", contact.getUser()) + "<br>"
                + getString("crepeia", contact.getUser()) + "<br>"
                + getString("ufjf", contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe1#", getString("unsubscribe1", contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe2#", getString("unsubscribe2", contact.getUser()));
        htmlMessage = htmlMessage.replace("#user#", contact.getUser().getName());
        htmlMessage = htmlMessage.replace("#email#", contact.getUser().getEmail());
        htmlMessage = htmlMessage.replace("#token#",token);
        htmlMessage = htmlMessage.replace("#id#", String.valueOf(contact.getUser().getId()));
        htmlMessage = htmlMessage.replace("#messageid#", contact.getContent());
        htmlMessage = htmlMessage.replace("#ratingheader#", getString("emailRatingHeader", contact.getUser()));
        return htmlMessage;
    }

    public void sendPlanEmail(User user, String attachment, ByteArrayOutputStream pdf)  {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("planSubj");
            contact.setContent("plan");
            contact.setAttachment(attachment);
            contact.setPdf(pdf);
            sendHTMLEmail(contact);
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendRecordEmail(User user, String attachment, ByteArrayOutputStream pdf){
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("recordSubj");
            contact.setContent("record");
            contact.setAttachment(attachment);
            contact.setPdf(pdf);
            sendHTMLEmail(contact);
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendSignInEmail(User user){
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("startSubj");
            contact.setContent("startMsg");
            sendHTMLEmail(contact);
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendSignUpEmail(User user) throws MessagingException, MissingResourceException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient(user.getEmail());
        contact.setSubject("welcomeSubj");
        contact.setContent("welcome");
        sendHTMLEmail(contact);
    }

    public void scheduleDiaryReminderEmail(User user, Date date) {
        Contact contact;
        int weeks[] = {2, 3, 4};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("emailMsgSubjectDiaryStartedSubj");
            contact.setContent("emailMsgSubjectDiaryStarted");
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
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("progressPersistchallengesReduceSubj");
            contact.setContent("progressPersistchallengesReduce");
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
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("progressPersistchallengesQuitSubj");
            contact.setContent("progressPersistchallengesQuit");
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
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("progressKeepingresultQuitSubj");
            contact.setContent("progressKeepingresultQuit");
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
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("progressKeepingresultReduceSubj");
            contact.setContent("progressKeepingresultReduce");
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
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient(user.getEmail());
        contact.setSubject("annualscreeningSubj");
        contact.setContent("annualscreening");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        contact.setDateScheduled(cal.getTime());
        save(contact);

    }

    public void scheduleWeeklyEmail(User user, Date date) {
        Contact contact;
        int weeks[] = {1, 2, 3, 4, 8, 12, 24, 48};
        for (int week : weeks) {
            contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("week" + week + "Subj");
            contact.setContent("week" + week);
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
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient(user.getEmail());
        contact.setSubject("tipsSubj");
        contact.setContent("tip" + String.valueOf(randomNumber));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, frequency);
        contact.setDateScheduled(cal.getTime());
        save(contact);
    }

    public void clearScheduledKeepingResultEmails() {
        try {
            List<Contact> contacts = daoBase.list(getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && MessageResources.canonicalKey(contact.getSubject()).startsWith("progressKeepingresult")) {
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
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && MessageResources.canonicalKey(contact.getSubject()).equals("annualscreeningSubj")) {
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
                if (contact.getDateScheduled() != null && contact.getDateSent() == null && !MessageResources.canonicalKey(contact.getSubject()).equals("tipsSubj")) {
                    daoBase.delete(contact, getEntityManager());

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendScheduledEmails(){
        try {
            List<Contact> contacts = daoBase.list(getEntityManager());
            Calendar today = Calendar.getInstance();
            Calendar scheduledDate = Calendar.getInstance();
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
                    scheduledDate.setTime(contact.getDateScheduled());
                    if (today.compareTo(scheduledDate) >= 0) {
                        if(MessageResources.canonicalKey(contact.getSubject()).equals("tipsSubj")){
                            sendTipsEmail(contact);
                            scheduleTipsEmail(contact.getUser());
                        }else{
                            sendHTMLEmail(contact);
                            if(MessageResources.canonicalKey(contact.getSubject()).equals("annualscreeningSubj")){
                                scheduleAnnualScreeningEmail(contact.getUser());
                            }
                        }
                    }
                   
                }
            }
        } catch (SQLException |MissingResourceException|MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendTestEmail(){
        Contact contact = new Contact();
        contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
        contact.setRecipient("leomartinsjf@gmail.com");
        contact.setSubject("teste de envio");
        contact.setContent("teste de envio");
        sendPlainTextEmail(contact);
    }

    private void sendHTMLEmail(Contact contact) throws MessagingException, MissingResourceException{
       // try {
            String content = getContent(contact, htmlTemplate);
            String subject = getSubject(contact);
            System.out.println(content);
            eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
            save(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

       // } catch (MessagingException |  MissingResourceException ex) {
           // Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
       // }
    }
    
    private void sendTipsEmail(Contact contact) {
        try {
            String content = getContent(contact, tipsTemplate);
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
    

    private String getContent(Contact contact, String template) throws MissingResourceException {
        String htmlMessage = template;
        htmlMessage = htmlMessage.replace("#title#", getString("title1",contact.getUser()));
        htmlMessage = htmlMessage.replace("#content#", getString(contact.getContent(),contact.getUser()));
        htmlMessage = htmlMessage.replace("#footer#",   
                getString("title1",contact.getUser()) + "<br>"
                + getString("crepeia",contact.getUser()) + "<br>"
                + getString("ufjf",contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe1#", getString("unsubscribe1", contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe2#", getString("unsubscribe2", contact.getUser()));
        htmlMessage = htmlMessage.replace("#user#", contact.getUser().getName());
        htmlMessage = htmlMessage.replace("#email#", contact.getUser().getEmail());
        htmlMessage = htmlMessage.replace("#code#", String.valueOf(contact.getUser().getRecoverCode()));
        htmlMessage = htmlMessage.replace("#id#", contact.getUser().getHashedId());
        htmlMessage = htmlMessage.replace("#messageid#", contact.getContent());
        htmlMessage = htmlMessage.replace("#ratingheader#", getString("emailRatingHeader",contact.getUser()));
        return htmlMessage;
    }

    private String getSubject(Contact contact) throws MissingResourceException {
        return getString(contact.getSubject(), contact.getUser());
    }
    
    private String getString(String key, User user) throws MissingResourceException{
        return MessageResources.getString(key, user.getPreferedLanguage());
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
