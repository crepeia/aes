package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.Encrypter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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
    private ResourceBundle bundle;

    @ManagedProperty(value = "#{contactController}")
    private ContactController contactController;

    private String email;
    private Integer recoverCode;
    private String passwordd;

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
                } else {
                    Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
                    String url = ((HttpServletRequest) request).getRequestURI();
                    url = url.substring(url.lastIndexOf('/') + 1);
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
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
        try {
            Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "'saiu do sistema.");
            user = null;
            loggedIn = false;
            password = null;
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearSession() {

    }

    public void signUp() {
        try {
            List<User> userList = this.getDaoBase().list("email", user.getEmail(), getEntityManager());
            if (!userList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail já está sendo usado.", null));
            } else {
                user.setPassword(Encrypter.encrypt(password));
                user.setSignUpDate(new Date());
                user.setPreferedLanguage(FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage());
                save();
                contactController.sendSignUpEmail(user);
                signIn(true);
                Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "'cadastrou no sistema.");
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
        try {
            List<User> userList = daoBase.list("email", user.getEmail(), this.getEntityManager());
            if (userList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email não registrado", null));
            } else {
                User foundUser = userList.get(0);
                foundUser.setRecoverCode(generateCode());
                daoBase.insertOrUpdate(foundUser, getEntityManager());
                contactController.sendPasswordRecoveryEmail(foundUser);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Você receberá um email com as instruções.", null));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void checkCode() {
        try {
            List<User> userList = this.getDaoBase().list("email", user.getEmail(), this.getEntityManager());
            if (!userList.isEmpty()) {
                User foundUser = userList.get(0);
                if (foundUser.getRecoverCode() != null && user.getRecoverCode().equals(foundUser.getRecoverCode())) {
                    foundUser.setPassword(Encrypter.encrypt(password));
                    password = null;
                    foundUser.setRecoverCode(null);
                    daoBase.insertOrUpdate(foundUser, getEntityManager());
                    FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, "Senha redefinida com sucesso.", null));
                } else {
                    FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Código de recuperação inválido.", null));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email não cadastrado.", null));
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

    public String getString(String key) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public ContactController getContactController() {
        return contactController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

}
