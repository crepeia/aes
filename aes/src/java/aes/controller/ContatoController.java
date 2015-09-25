/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.persistence.GenericDAO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import aes.utility.EMailSSL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import javax.naming.NamingException;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "contatoController")
@RequestScoped
public class ContatoController extends BaseController implements Serializable {

    private EMailSSL eMailSSL;
    private String email;
    private String message;
    private Contact contact;

    public ContatoController() {

        super();
        eMailSSL = new EMailSSL();
        contact = new Contact();
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
        } catch (NamingException ex) {
            Logger.getLogger(ContatoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendEmail() {
        try {
            contact.setFrom(email);
            contact.setTo("acoolesaude@gmail.com");
            contact.setSubject("Contato -- Álcool e Saúde");
            contact.setTextContent(message);
            contact.setSentDate(Calendar.getInstance());
            if(loggedUser()){
                contact.setUser(getLoggedUser());
            }
            eMailSSL.send(contact);
            daoBase.insertOrUpdate(contact, this.getEntityManager());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Mensagem enviada com sucesso.", null));
        } catch (SQLException ex) {
            Logger.getLogger(ContatoController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
