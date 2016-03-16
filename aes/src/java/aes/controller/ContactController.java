/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Contact;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.persistence.UserDAO;
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

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "contactController")
@ApplicationScoped
public class ContactController extends BaseController implements Serializable {

    private EMailSSL eMailSSL;
    private String htmlTemplate;
    private UserDAO userDAO;
    
    @PostConstruct
    public void init(){
        eMailSSL = new EMailSSL();
        htmlTemplate = readHTMLTemplate();
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public void sendEmail(Contact contact) {
        try {
            eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getText(),
                    contact.getHtml(), contact.getPdf(), contact.getPdfName());
            daoBase.insertOrUpdate(contact, getEntityManager());
        } catch (SQLException ex) {
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
                daoBase.insertOrUpdate(contact, getEntityManager());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "email.sent.password", null));
                Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Password recovery email  sent to:" + user.getEmail());
            }
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /*public void sendDifferentDateEmail() {
        List<User> users = userDAO.followUpDifferentDate(getEntityManager());
        Contact contact;
        if (!users.isEmpty()) {
            for (User user : users) {
                try {
                    contact = new Contact();
                    contact.setSender("watiufjf@gmail.com");
                    contact.setRecipient(user.getEmail());
                    contact.setSubject(getText("subject.email.followup", user.getPreferedLanguage()));
                    contact.setHtml(fillTemplate(
                            getText("vivasemtabaco", user.getPreferedLanguage()),
                            getText("subject.email.followup", user.getPreferedLanguage()),
                            getText("subject.email.followup", user.getPreferedLanguage()),
                            getFooter(user.getPreferedLanguage())));
                    contact.setDateSent(new Date());
                    contact.setUser(user);
                    sendEmail(contact);
                    user.getProntoParaParar().setFollowUpCount(1);
                    prontoDAO.insertOrUpdate(user.getProntoParaParar(), getEntityManager());
                    daoBase.insertOrUpdate(contact, getEntityManager());
                    Logger.getLogger(ContactController.class.getName()).log(Level.INFO, "Different date follow up email  sent to:" + user.getEmail());
                } catch (SQLException ex) {
                    Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }*/
    public String getFooter(String language) {
        return this.getText("vivasemtabaco", language) + "<br>"
                + this.getText("crepeia", language) + "<br>"
                + this.getText("ufjf", language);
    }

    public String readHTMLTemplate() {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/contact-template.html");
            byte[] buffer = new byte[10240];
            return new String(buffer, 0, input.read(buffer), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String fillHTMLTemplate(String title, String subtitle, String content, String footer) {
        String newTemplate = htmlTemplate;
        if (title != null) {
            newTemplate = newTemplate.replace("#title#", title);
        }
        if (subtitle != null) {
            newTemplate = newTemplate.replace("#subtitle#", subtitle);
        }
        if (content != null) {
            newTemplate = newTemplate.replace("#content#", content);
        }
        if (footer != null) {
            newTemplate = newTemplate.replace("#footer#", footer);
        }

        return newTemplate;
    }

    public String getText(String key, String language) {
        ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(language));
        return bundle.getString(key);
    }

    public EMailSSL geteMailSSL() {
        return eMailSSL;
    }

    public void seteMailSSL(EMailSSL eMailSSL) {
        this.eMailSSL = eMailSSL;
    }

}
