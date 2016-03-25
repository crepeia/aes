package aes.controller;

import aes.model.Contact;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import aes.utility.EMailSSL;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

@ManagedBean(name = "contactController")
@ApplicationScoped
public class ContactController extends BaseController implements Serializable {

    private EMailSSL eMailSSL;
    private String contactTemplate;
    private String planTemplate;
    private GenericDAO userDAO;

    @PostConstruct
    public void init() {
        eMailSSL = new EMailSSL();
        contactTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        planTemplate = readHTMLTemplate("aes/utility/plan-template.html");
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
                contact.setText("hello" + " " + name_user + "," + "\n"
                        + "\n"
                        + "email.password.send" + email_user + "email.password.send.2" + " \n"
                        + "\n"
                        + "email.password.send.3" + "\n"
                        + "email.password.send.4" + code + "\n"
                        + "email.password.send.5" + "http://www.aes.com.br/esqueceu-sua-senha.xhtml" + "\n\n"
                        + "cordialmente"
                        + "\n"
                        + "equipe.aes"
                        + "\n");

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

    public void sendPlanEmail(User user, String content[]) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject("subject.email.plano");
        contact.setDateSent(new Date());
        contact.setHtml(getHTMLPlan(content,user.getPreferedLanguage()));
        sendEmail(contact);
    }
    
    public void sendSignUpEmail(User user) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setSender("alcoolesaude@gmail.com");
        contact.setRecipient(user.getEmail());
        contact.setSubject(getSubject("welcome_subj", user.getPreferedLanguage()));
        contact.setDateSent(new Date());
        contact.setHtml(getHTMLMessage("welcome", user.getPreferedLanguage()));
        sendEmail(contact);
    }
    
    private void sendEmail(Contact contact) {
        eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getText(),
                contact.getHtml(), contact.getPdf(), contact.getPdfName());
        contact.setSent(true);
        save(contact);
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

    private String getHTMLMessage(String content, String language) {
        String htmlMessage = contactTemplate;
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(language));
        htmlMessage = htmlMessage.replace("#title#", bundle.getString("title.1"));
        htmlMessage = htmlMessage.replace("#content#", bundle.getString(content));
        htmlMessage = htmlMessage.replace("#footer#", 
                    bundle.getString("title.1") + "<br>" + 
                    bundle.getString("crepeia") + "<br>" + 
                    bundle.getString("ufjf"));
        
        return htmlMessage;
    }

    public String getHTMLPlan(String content[], String language) {
        String newTemplate = planTemplate;
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(language));
        String subtitle[] = new String[6];
        String title = bundle.getString("plan.0");
        subtitle[0] = bundle.getString("plan.1");
        subtitle[1] = bundle.getString("plan.2");
        subtitle[2] = bundle.getString("plan.3");
        subtitle[3] = bundle.getString("plan.4");
        subtitle[4] = bundle.getString("plan.5");
        subtitle[5] = bundle.getString("plan.6");

        newTemplate = newTemplate.replace("#title#", title);
        for (int i = 0; i < 6; i++) {
            newTemplate = newTemplate.replace("#subtitle" + (i + 1) + "#", subtitle[i]);
            newTemplate = newTemplate.replace("#content" + (i + 1) + "#", content[i]);
        }

        return newTemplate;
    }

    private String getSubject(String subject, String language) {
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(language));
        return bundle.getString(subject );
    }

}
