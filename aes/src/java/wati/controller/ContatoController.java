/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import wati.model.User;
import wati.utility.EMailSSL;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "contatoController")
@RequestScoped
public class ContatoController implements Serializable {

    private EMailSSL eMailSSL;
    private String email;
    private String message;

    public ContatoController() {

        super();

        this.eMailSSL = new EMailSSL();

    }

    public void sendEmail() {

        eMailSSL.send(this.email, "hedersb@gmail.com", "Contato -- Wati", message);
        String message = "Mensagem enviada com sucesso.";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
        Logger.getLogger(BaseFormController.class.getName()).log(Level.INFO, message);

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
