/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.FollowUp;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.Encrypter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtext.InputText;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "userController")
@SessionScoped
public class UserController extends BaseController<User> {

    private User user;
    private boolean loggedIn;
    private String password;
    ResourceBundle bundle;

    @ManagedProperty(value = "#{contactController}")
    private ContactController contactController;
    
    private String email;
    private Integer recoverCode;
    private String passwordd;
    private boolean showErrorMessage;
    
    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<User>(User.class);
            user = new User();
        } catch (NamingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void signIn(boolean redirect) {

        try {
            List<User> userList = this.getDaoBase().list("email", user.getEmail(), getEntityManager());

            if (!userList.isEmpty() && Encrypter.compare(password, userList.get(0).getPassword())) {
                user = userList.get(0);
                loggedIn = true;
                password = null;

                if (redirect) {
                    Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
                    if (object != null) {
                        String url = (String) object;
                        FacesContext.getCurrentInstance().getExternalContext().redirect(url);

                    } else {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                    }
                }

                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "' logou no sistema.");

            } else {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, "Usuário '" + user.getEmail() + "' não conseguiu logar.");
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail ou senha inválida.", null));
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void signOut() {
        String email = user.getEmail();
        user = null;
        loggedIn = false;
        password = null;
        try {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + email + "'saiu no sistema.");

        } catch (IOException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void signUp() {
        try {
            List<User> userList = this.getDaoBase().list("email", user.getEmail(), getEntityManager());
            if (!userList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail já está sendo usado.", null));
            } else {
                user.setPassword(Encrypter.encrypt(password));
                user.setSignUpDate(new Date());
                save();
                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "'cadastrou no sistema.");
                signIn(true);
            }

        } catch (InvalidKeyException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int generateCode() {
        long codigo;
        float valor;

        Random generate = new Random();
        valor = (float) generate.nextInt(100000) / 10;
        while (valor > 999 && valor < 10000) {
            valor = (float) generate.nextInt(10000) / 10;
        }
        valor *= 10;

        codigo = (int) valor;
        return (int) codigo;
    }

    public void recoverPassword() {
        contactController.sendPasswordRecoveryEmail(user.getEmail(), generateCode());
    }
    
    public void alterPassword() throws SQLException, InvalidKeyException, IOException {
        this.showErrorMessage = true;
        List<User> userList = this.getDaoBase().list("email", this.email, this.getEntityManager());
        try{
                if (!userList.isEmpty() && userList.get(0).getId() != 0) {
                    user = userList.get(0);
                    this.user.setPassword(Encrypter.encrypt(this.passwordd));
                    /*if (!Encrypter.compare(this.passwordd, this.user.getPassword())) {
                        //incluir criptografia da senha
                        this.user.setPassword(Encrypter.encrypt(this.passwordd));
                    }*/
                    this.getDaoBase().insertOrUpdate(user, this.getEntityManager());
                    String message = "password.changed";
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                    FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                    //this.returnIndex();
                }
                else{
                    String message = "user.not.registered.requesting.password.change";
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                }
            
        }catch (InvalidKeyException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setPasswordAlter(user);
        this.getDaoBase().insertOrUpdate(user, this.getEntityManager());
    }
    
    public void setPasswordAlter(User user){
        int setCode = 0;
        user.setRecoverCode(setCode);
    }
    
    public void annualScreening() {
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            user.getFollowUp().setAnnualScreening(cal.getTime());
            save();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Você será contactado em breve.", null));
            ((InputText) getComponent("email")).setDisabled(true);
            ((CommandButton) getComponent("sendButton")).setDisabled(true);
            if (!loggedIn) {
                FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            }
    }
    
    public String checkCode() {
        try {
            String message;
            List<User> userList = this.getDaoBase().list("email", this.email, this.getEntityManager());
            if (!userList.isEmpty() && userList.get(0).getRecoverCode() != null && userList.get(0).getRecoverCode().equals(recoverCode) && recoverCode != 0) {
                return "esqueceu-sua-senha-concluir.xhtml";
            } else {
                message = "email.code.incorrect";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                return "";
            }
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "problemas.gravar.usuario", null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public String preEvaluation() {
        if (user.getDrink() != null) {
            if (user.getPregnant() && !user.getDrink()) {
                return "quanto-voce-bebe-nao-gravidez.xhtml?faces-redirect=true";
            } else if (user.getPregnant() && user.getDrink()) {
                return "quanto-voce-bebe-sim-gravidez.xhtml?faces-redirect=true";
            } else if (user.isUnderage() && !user.getDrink()) {
                return "quanto-voce-bebe-nao-adoles.xhtml?faces-redirect=true";
            } else if (user.isUnderage() && user.getDrink()) {
                return "quanto-voce-bebe-sim-adoles.xhtml?faces-redirect=true";
            } else if (!user.getDrink()) {
                return "quanto-voce-bebe-abstemio.xhtml?faces-redirect=true";
            } else {
                return "quanto-voce-bebe-sim-beber-uso-audit-3.xhtml?faces-redirect=true";
            }
        }
        else{
            return "cadastrar-nova-conta.xhtml?faces-redirect=true";
        }
    }

    public void save() {
        try {
            daoBase.insertOrUpdate(user, getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void redirectLogin(boolean redirect) {
        if (redirect && !loggedIn) {
            try {
                Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
                String url = ((HttpServletRequest) request).getRequestURI();
                url = url.substring(url.lastIndexOf('/') + 1);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("url", url);
                FacesContext.getCurrentInstance().getExternalContext().redirect("cadastrar-nova-conta.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void redirectIndex(boolean redirect) {
        if (redirect && loggedIn) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getTranslation(String key) {
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ContactController getContactController() {
        return contactController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getRecoverCode() {
        return recoverCode;
    }

    public void setRecoverCode(Integer recoverCode) {
        this.recoverCode = recoverCode;
    }
    
    public String getPasswordd() {
        return passwordd;
    }

    public void setPasswordd(String passwordd) {
        this.passwordd = passwordd;
    }
    

}