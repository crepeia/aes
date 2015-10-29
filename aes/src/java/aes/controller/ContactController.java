/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Contact;
import aes.model.Evaluation;
import aes.persistence.EvaluationDAO;
import aes.persistence.GenericDAO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import aes.utility.EMailSSL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.faces.bean.ApplicationScoped;
import javax.naming.NamingException;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "contactController")
@ApplicationScoped
@Stateless
public class ContactController extends BaseController implements Serializable {

    private EMailSSL eMailSSL;
    private Contact contact;

    public ContactController() {
        eMailSSL = new EMailSSL();
        contact = new Contact();
        try {
            daoBase = new GenericDAO<Contact>(Contact.class);
        } catch (NamingException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void contactFormSend(){
        contact.setDate(new Date());
        contact.setSubject("Contato via formulário - " + contact.getSender());
        contact.setRecipient("alcoolesaude@gmail.com");
        sendEmail();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Mensagem enviada com sucesso.", null));

    }

    public void sendEmail() {
        try {          
            eMailSSL.send(contact.getSender(), contact.getRecipient(), contact.getSubject(), contact.getText(),
            contact.getHtml(), contact.getPdf(), contact.getPdfName());
            daoBase.insertOrUpdate(contact, getEntityManager());
            contact = new Contact();
        } catch (SQLException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
       
        
    public EMailSSL geteMailSSL() {
        return eMailSSL;
    }

    public void seteMailSSL(EMailSSL eMailSSL) {
        this.eMailSSL = eMailSSL;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
        
}