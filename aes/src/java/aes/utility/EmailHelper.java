/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.controller.ContactController;
import aes.model.Contact;
import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.ContactDAO;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author patri
 */
public class EmailHelper {

    private EMailSSL eMailSSL;
    private ResourceBundle bundle;
    private String htmlTemplate;
    private String tipsTemplate;
    private ContactDAO contactDAO;

    public EmailHelper() {
        eMailSSL = new EMailSSL();
        htmlTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        tipsTemplate = readHTMLTemplate("aes/utility/tips-template.html");
        try {
            contactDAO = new ContactDAO();
        } catch (NamingException ex) {
            Logger.getLogger(EmailHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendContactFormEmail(ActionEvent event, EntityManager entityManager) throws SQLException, MessagingException {
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
        sendPlainTextEmail(contact, entityManager);
        FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.sent", user), null));

    }

    public void sendPasswordRecoveryEmail(User user, EntityManager entityManager) throws MessagingException, SQLException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("passwordrecovery_subj");
        contact.setContent("passwordrecovery");
        sendHTMLEmail(contact, entityManager);
    }

    public void sendPlanEmail(User user, String attachment, ByteArrayOutputStream pdf, EntityManager entityManager) throws SQLException {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender("alcoolesaude@gmail.com");
            contact.setRecipient(user.getEmail());
            contact.setSubject("plan_subj");
            contact.setContent("plan");
            contact.setAttachment(attachment);
            contact.setPdf(pdf);
            sendHTMLEmail(contact, entityManager);
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MissingResourceException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendRecordEmail(User user, String attachment, ByteArrayOutputStream pdf, EntityManager entityManager) throws SQLException, MessagingException {

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("record_subj");
        contact.setContent("record");
        contact.setAttachment(attachment);
        contact.setPdf(pdf);
        sendHTMLEmail(contact, entityManager);

    }

    public void sendSignInEmail(User user, EntityManager entityManager) throws SQLException, MessagingException {

        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("start_subj");
        contact.setContent("start_msg");
        sendHTMLEmail(contact, entityManager);

    }

    public void sendSignUpEmail(User user, EntityManager entityManager) throws MessagingException, MissingResourceException, SQLException {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("welcome_subj");
        contact.setContent("welcome");
        sendHTMLEmail(contact, entityManager);
    }

    public void sendTestEmail(EntityManager entityManager) throws SQLException, MessagingException {
        Contact contact = new Contact();
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient("leomartinsjf@gmail.com");
        contact.setSubject("teste de envio");
        contact.setContent("teste de envio");
        sendPlainTextEmail(contact, entityManager);
    }

    public void sendHTMLEmail(Contact contact, EntityManager entityManager) throws MessagingException, MissingResourceException, SQLException {
        // try {
        String content = getContent(contact, htmlTemplate);
        String subject = getSubject(contact);
        System.out.println(content);
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        //save(contact);
        contactDAO.insertOrUpdate(contact, entityManager);

        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

        // } catch (MessagingException |  MissingResourceException ex) {
        // Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        // }
    }

    public void sendTipsEmail(Contact contact, EntityManager entityManager) throws SQLException, MessagingException {
        String content = getContent(contact, tipsTemplate);
        String subject = getSubject(contact);
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        //save(contact);
        contactDAO.insertOrUpdate(contact, entityManager);
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

    }

    public void sendPlainTextEmail(Contact contact, EntityManager entityManager) throws SQLException, MessagingException {

      
        eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getContent(), contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        //save(contact);
        contactDAO.insertOrUpdate(contact, entityManager);
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

    }
    
        public void sendEmail(Contact contact, EntityManager entityManager, String template) throws SQLException, MessagingException {
        String content = getContent(contact, template);
        String subject = getSubject(contact);
        
        eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
        contact.setDateSent(new Date());
        //save(contact);
        contactDAO.insertOrUpdate(contact, entityManager);
        Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getRecipient());

    }
    
    

    public String readHTMLTemplate(String path) {
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

    private String getSubject(Contact contact) throws MissingResourceException {
        return getString(contact.getSubject(), contact.getUser());
    }

    private String getString(String key, User user) throws MissingResourceException {
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

}
