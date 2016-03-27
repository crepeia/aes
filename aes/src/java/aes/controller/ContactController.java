package aes.controller;

import aes.model.Contact;
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
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.naming.NamingException;

@ManagedBean(name = "contactController")
@ApplicationScoped
public class ContactController extends BaseController implements Serializable {

    private EMailSSL eMailSSL;
    private String htmlTemplate;
    private GenericDAO userDAO;

    @PostConstruct
    public void init() {
        eMailSSL = new EMailSSL();
        htmlTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
            userDAO = new GenericDAO<User>(User.class);
        } catch (NamingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendPasswordRecoveryEmail(String email, int code) {

        try {
            List<User> userList = userDAO.list("email", email, this.getEntityManager());

            if (userList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "user.not.registered.password", null));
            } else {
                User user = userList.get(0);
                String name_user = userList.get(0).getName();
                String email_user = userList.get(0).getEmail();

                Contact contact = new Contact();
                contact.setSender("alcoolesaude@gmail.com");
                contact.setSubject("subject.email.password");
                contact.setRecipient(email);
                contact.setContent("hello" + " " + name_user + "," + "<br>"
                        + "<br>"
                        + "email.password.send" + email_user + "email.password.send.2" + " <br>"
                        + "<br>"
                        + "email.password.send.3" + "<br>"
                        + "email.password.send.4" + code + "\n"
                        + "email.password.send.5" + "http://www.aes.com.br/esqueceu-sua-senha.xhtml" + "<br><br>"
                        + "cordialmente"
                        + "<br>"
                        + "equipe.aes"
                        + "<br>");

                contact.setDateSent(new Date());
                sendEmail(contact);

                user.setRecoverCode(code);
                userDAO.insertOrUpdate(user, getEntityManager());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "email.sent.password", null));
                Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Password recovery email  sent to:" + user.getEmail());
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        sendEmail(contact);
    }

    public void sendSignUpEmail(User user) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("welcome_subj");
        contact.setContent("welcome");
        sendEmail(contact);
    }

    public void scheduleWeeklyEmail(User user) {
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
            cal.add(Calendar.DATE, 7 * week);
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

    public void clearScheduledEmails(User user) {
        try {
            List<Contact> contacts = daoBase.list("user", user, getEntityManager());
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
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
            for (Contact contact : contacts) {
                if (contact.getDateScheduled() != null && contact.getDateSent() == null) {
                    sendEmail(contact);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    private void sendEmail(Contact contact) {
        try {
            String content = getContent(contact);
            String subject = getSubject(contact);
            eMailSSL.send(contact.getSender(), contact.getRecipient(), subject, content, contact.getPdf(), contact.getAttachment());
            contact.setDateSent(new Date());
            save(contact);
            Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Email enviado para:" + contact.getUser().getEmail());
        } catch (MessagingException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void save(Contact contact) {
        try {
            daoBase.insertOrUpdate(contact, getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String readHTMLTemplate(String path) {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            byte[] buffer = new byte[102400];
            return new String(buffer, 0, input.read(buffer), StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Logger.getLogger(ContactController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getContent(Contact contact) {
        String htmlMessage = htmlTemplate;
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(contact.getUser().getPreferedLanguage()));
        htmlMessage = htmlMessage.replace("#title#", bundle.getString("title.1"));
        htmlMessage = htmlMessage.replace("#content#", bundle.getString(contact.getContent()));
        htmlMessage = htmlMessage.replace("#footer#",
                bundle.getString("title.1") + "<br>"
                + bundle.getString("crepeia") + "<br>"
                + bundle.getString("ufjf"));

        return htmlMessage;
    }

    private String getSubject(Contact contact) {
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(contact.getUser().getPreferedLanguage()));
        return bundle.getString(contact.getSubject());
    }

}
