/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.controller.ContactController;
import aes.model.Contact;
import aes.model.User;
import aes.persistence.ContactDAO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author patri
 */
public class EmailHelper {

    private EMailSSL eMailSSL;
    private ResourceBundle bundle;
    private ContactDAO contactDAO;

    public EmailHelper(ContactDAO contactDAO) {
        eMailSSL = new EMailSSL();
        this.contactDAO = contactDAO;
    }

    public void sendContactFormEmail(String message, User user) throws SQLException {
        //String message = (String) event.getComponent().getAttributes().get("message");
        //User user = (User) event.getComponent().getAttributes().get("user");
        Contact contact = new Contact();
        if (user.getId() != 0) {
            contact.setUser(user);
        }
        contact.setSender(user.getEmail());
        contact.setRecipient("alcoolesaude@gmail.com");
        contact.setSubject("Contato via formulario - " + user.getEmail());
        contact.setContent(message);
        sendPlainTextEmail(contact);
        //FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.sent", user), null));

    }

    public void sendPasswordRecoveryEmail(User user) throws MessagingException, MissingResourceException, SQLException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("passwordrecovery_subj");
        contact.setContent("passwordrecovery");
        sendHTMLEmail(contact);
    }

    public void sendPlanEmail(User user, String attachment, ByteArrayOutputStream pdf) throws SQLException {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("plan_subj");
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

    public void sendRecordEmail(User user, String attachment, ByteArrayOutputStream pdf) throws SQLException {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("record_subj");
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

    public void sendSignInEmail(User user) throws SQLException {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("start_subj");
            contact.setContent("start_msg");
            sendHTMLEmail(contact);
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendSignUpEmail(User user) throws MessagingException, MissingResourceException, SQLException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("welcome_subj");
        contact.setContent("welcome");
        sendHTMLEmail(contact);
    }

    /*public void sendScheduledEmails(){
        try {
            List<Contact> contacts = daoBase.list(getEntityManager());
            Calendar today = Calendar.getInstance();
            Calendar scheduledDate = Calendar.getInstance();
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
                    scheduledDate.setTime(contact.getDateScheduled());
                    if (today.compareTo(scheduledDate) >= 0) {
                        if(contact.getSubject().contains("tips_subj")){
                            sendTipsEmail(contact);
                            scheduleTipsEmail(contact.getUser());
                        }else{
                            sendHTMLEmail(contact);
                            if(contact.getSubject().contains("annualscreening_subj")){
                                scheduleAnnualScreeningEmail(contact.getUser());
                            }
                        }
                    }
                   
                }
            }
        } catch (SQLException |MissingResourceException|MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    public void sendTestEmail() throws SQLException {
        Contact contact = new Contact();
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient("leomartinsjf@gmail.com");
        contact.setSubject("teste de envio");
        contact.setContent("teste de envio");
        sendPlainTextEmail(contact);
    }

    private void sendEmailUsingTemplate(Contact contact, String template) throws MessagingException, MissingResourceException, SQLException {
        String content = getContent(contact, template);
        String subject = getSubject(contact);
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        contactDAO.insert(contact);
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

    }

    private void sendHTMLEmail(Contact contact) throws MessagingException, MissingResourceException, SQLException {
        String htmlTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        String content = getContent(contact, htmlTemplate);
        String subject = getSubject(contact);
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        contactDAO.insert(contact);
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

    }

    private void sendTipsEmail(Contact contact) throws SQLException {
        try {
            String tipsTemplate = readHTMLTemplate("aes/utility/tips-template.html");
            String content = getContent(contact, tipsTemplate);
            String subject = getSubject(contact);
            eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
             contactDAO.insert(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

        } catch (MessagingException | MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendPlainTextEmail(Contact contact) throws SQLException {
        try {
            eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getContent(), contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
             contactDAO.insert(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
  

    private String getSubject(Contact contact) throws MissingResourceException {
        return getString(contact.getSubject(), contact.getUser());
    }

    private String getString(String key, User user) throws MissingResourceException {
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

    private String getContent(Contact contact, String template) throws MissingResourceException {
        String htmlMessage = template;
        htmlMessage = htmlMessage.replace("#title#", getString("title.1", contact.getUser()));
        htmlMessage = htmlMessage.replace("#content#", getString(contact.getContent(), contact.getUser()));
        htmlMessage = htmlMessage.replace("#footer#",
                getString("title.1", contact.getUser()) + "<br>"
                + getString("crepeia", contact.getUser()) + "<br>"
                + getString("ufjf", contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe1#", getString("unsubscribe.1", contact.getUser()));
        htmlMessage = htmlMessage.replace("#unsubscribe2#", getString("unsubscribe.2", contact.getUser()));
        htmlMessage = htmlMessage.replace("#user#", contact.getUser().getName());
        htmlMessage = htmlMessage.replace("#email#", contact.getUser().getEmail());
        htmlMessage = htmlMessage.replace("#code#", String.valueOf(contact.getUser().getRecoverCode()));
        htmlMessage = htmlMessage.replace("#id#", contact.getUser().getHashedId());
        htmlMessage = htmlMessage.replace("#messageid#", contact.getContent());
        htmlMessage = htmlMessage.replace("#ratingheader#", getString("email.rating.header", contact.getUser()));
        return htmlMessage;
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

}
