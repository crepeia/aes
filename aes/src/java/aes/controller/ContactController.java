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
import java.text.SimpleDateFormat;
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
    private String contactTemplate;
    private String planTemplate;
    private UserDAO userDAO;

    @PostConstruct
    public void init() {
        eMailSSL = new EMailSSL();
        contactTemplate = readHTMLTemplate("aes/utility/contact-template.html");
        planTemplate = readHTMLTemplate("aes/utility/plan-template.html");
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
            userDAO = new UserDAO();
        } catch (NamingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendEmail(Contact contact) {
        eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getText(),
                contact.getHtml(), contact.getPdf(), contact.getPdfName());
        save(contact);
    }

    public void save(Contact contact) {
        try {
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
        contact.setDateSent(new Date());
        contact.setSubject("subject.email.plano");
        contact.setHtml(fillHTMLTemplate(content));
        sendEmail(contact);
    }

    public String getFooter(String language) {
        return this.getText("title.1", language) + "<br>"
                + this.getText("crepeia", language) + "<br>"
                + this.getText("ufjf", language);
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

    public String fillHTMLTemplate(String title, String subtitle, String content, String footer) {
        String newTemplate = contactTemplate;
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

    public String fillHTMLTemplate(String content[]) {
        System.out.println(planTemplate);
        String newTemplate = planTemplate;

        String subtitle[] = new String[6];
        String title = "Meu Plano";
        subtitle[0] = "Data para começar";
        subtitle[1] = "As razões mais importantes que eu tenho para mudar a forma que bebo são:";
        subtitle[2] = "Eu irei usar as seguintes estratégias:";
        subtitle[3] = "As pessoas que podem me ajudar são:";
        subtitle[4] = "Eu saberei que meu plano está funcionando quando:";
        subtitle[5] = "O que pode interferir e como posso lidar com estas situações:";

        newTemplate = newTemplate.replace("#title#", title);
        for (int i = 0; i < 6; i++) {
            newTemplate = newTemplate.replace("#subtitle" + (i + 1) + "#", subtitle[i]);
            newTemplate = newTemplate.replace("#content" + (i + 1) + "#", content[i]);
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
