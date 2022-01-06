package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.Encrypter;
import aes.utility.EncrypterException;
import aes.utility.GenerateCode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author thiago
 */
@Named("userController")
@SessionScoped
public class UserController extends BaseController<User> {

    private User user;
    private boolean loggedIn;
    private String password;
    private ResourceBundle bundle;

    @Inject
    private ContactController contactController;

    private String email;
    private Integer recoverCode;
    private String passwordd;
    private String confirmEmail;
    private String editPassword;
    private int editDia;
    private int editMes;
    private int editAno;

    private int dia;
    private int mes;
    private int ano;

    private Map<String, String> dias = new LinkedHashMap<String, String>();
    private Map<String, String> meses = new LinkedHashMap<String, String>();
    private Map<String, String> anos = new LinkedHashMap<String, String>();
    private String[] nomeMeses;
    private boolean showErrorMessage;

    List<User> userList;

    @PostConstruct
    public void init() {
        mes = -1;
        for (int i = 1; i <= 31; i++) {
            dias.put(String.valueOf(i), String.valueOf(i));
        }
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        int lastYear = gc.get(GregorianCalendar.YEAR) - 1;
        for (int i = lastYear; i > lastYear - 100; i--) {
            anos.put(String.valueOf(i), String.valueOf(i));
        }
        try {
            daoBase = new GenericDAO<User>(User.class);
            user = new User();
            user.setDateCreated(new Date());
            user.setIpCreated(getIpAdress());
        } catch (NamingException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void signIn(boolean redirect) {

        try {
            List<User> userList = this.getDaoBase().list("email", user.getEmail(), getEntityManager());

            if (!userList.isEmpty() && Encrypter.compareHash(password, userList.get(0).getPassword(), userList.get(0).getSalt())) {
                user = userList.get(0);
                loggedIn = true;
                password = null;

                if (redirect) {
                    Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
                    if (object != null) {
                        String url = (String) object;
                        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                    } else {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("escolha-uma-etapa.xhtml");
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
        } catch (IOException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EncrypterException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } 

    }

    public void signOut() {
        Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "'saiu do sistema.");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("/aes/index.xhtml");
            user = null;
            loggedIn = false;
            password = null;
        } catch (IOException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearSession() {

    }

    public void signUp() {
        try {
            if (!confirmEmail.equals(user.getEmail())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.not.equals"), null));
            } else {
                List<User> userList = this.getDaoBase().list("email", user.getEmail(), getEntityManager());
                boolean regComplete = userList.stream().reduce(true, (reg, userElement) -> reg && userElement.isRegistration_complete(), Boolean::logicalAnd);

                if (!userList.isEmpty() && regComplete) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.used"), null));
                } else {
                    if (!regComplete) {
                        user.setId(userList.get(0).getId());
                    }
                    byte[] salt = Encrypter.generateRandomSecureSalt(16);
                    user.setRegistration_complete(true);
                    user.setSalt(salt);
                    user.setPassword(Encrypter.hashPassword(password, salt));
                    user.setSignUpDate(new Date());
                    save();
                    contactController.sendSignUpEmail(user);
                    if (user.isReceiveEmails()) {
                        contactController.scheduleTipsEmail(user);
                        contactController.scheduleDiaryReminderEmail(user, new Date());
                        contactController.scheduleWeeklyEmail(user, new Date());
                    }
                    signIn(true);
                    Logger.getLogger(UserController.class.getName()).log(Level.INFO, "Usuário '" + user.getEmail() + "'cadastrou no sistema.");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EncrypterException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public void editProfile() {
        User user = getUser();
        this.showErrorMessage = true;

        try {
            boolean emailAvailable = true;

            List<User> list = this.getDaoBase().list("email", user.getEmail(), getEntityManager());

            if (list.isEmpty()) {
                for (User usr : list) {
                    if (usr.getId() != user.getId()) {
                        emailAvailable = false;
                        String message = getString("email.cadastrado");
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                    }
                }
            }
            if (emailAvailable = true) {
                if (editPassword != null && !editPassword.trim().isEmpty()) {
                    byte[] salt = Encrypter.generateRandomSecureSalt(16);
                    user.setSalt(salt);
                    user.setPassword(Encrypter.hashPassword(editPassword, salt));
                }

                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                } catch (IOException ex) {
                    Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
                }

                getDaoBase().update(user, getEntityManager());
                editPassword = null;
                editAno = 0;
                editMes = 0;
                editDia = 0;
            }

        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (EncrypterException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public void editProfile() {
        User user = getLoggedUser();
        this.showErrorMessage = true;

        try {
            user.setBirthDate(new GregorianCalendar(editAno, editMes, editDia).getTime());
            user.setDtCadastro(new Date());

            boolean emailAvailable = true;
            List<User> list = dao.list("email", user.getEmail(), getEntityManager());
            if (!list.isEmpty()) {
                for (User usr : list) {
                    if (usr.getId() != user.getId()) {
                        emailAvailable = false;
                        String message = this.getText("email.cadastrado");
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                    }
                }
            }
            if (emailAvailable == true) {

                if (editPassword != null && !editPassword.trim().isEmpty()) {
                    user.setPassword(Encrypter.encrypt(editPassword));
                }

                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                } catch (IOException ex) {
                    Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
                }

                dao.update(user, getEntityManager());
                editPassword = null;
                editAno = 0;
                editMes = 0;
                editDia = 0;
            }

        } catch (InvalidKeyException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, this.getText("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);

        }

    }*/
    public void recoverPassword() {
        try {
            List<User> userList = daoBase.list("email", user.getEmail(), this.getEntityManager());
            if (userList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.not.registred"), null));
            } else {
                User foundUser = userList.get(0);
                foundUser.setRecoverCode(GenerateCode.generate());
                daoBase.insertOrUpdate(foundUser, getEntityManager());
                contactController.sendPasswordRecoveryEmail(foundUser);
                FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("email.instructions.password"), null));
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
                    //foundUser.setPassword(Encrypter.encrypt(password));

                    byte[] salt = Encrypter.generateRandomSecureSalt(16);
                    foundUser.setSalt(salt);
                    foundUser.setPassword(Encrypter.hashPassword(password, salt));

                    password = null;
                    foundUser.setRecoverCode(null);
                    daoBase.insertOrUpdate(foundUser, getEntityManager());
                    FacesContext.getCurrentInstance().addMessage("info", new FacesMessage(FacesMessage.SEVERITY_INFO, getString("redefined.password"), null));
                } else {
                    FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("code.invalid"), null));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("error", new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("email.not.registered"), null));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EncrypterException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setBirth() {
        user.setBirth(ano, mes, dia);
    }

    public void setBirthEdit() {
        user.setBirth(editAno, editMes, editDia);
    }

    public void save() {
        try {
            daoBase.insertOrUpdate(user, getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int redirect(boolean redirectLogin, boolean redirectIndex, boolean redirectEvaluation) {
        if (redirectIndex) {
            redirectIndex(true);
            return 0;
        } else if (redirectLogin) {
            redirectLogin(true);
            return 0;
        } else if (redirectEvaluation) {
            redirectEvaluation(true);
            return 0;
        } else {
            return 1;
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

    public void redirectEvaluation(boolean redirect) {
        if (redirect) {
            try {
                String url;
                if (user.getDrink() == null) {
                    url = "quanto-voce-bebe-introducao.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                } else if (!user.isUnderage() && user.isFemale() && user.getPregnant() && !user.getDrink()) {
                    url = "quanto-voce-bebe-nao-gravidez.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);

                } else if (user.isUnderage() && user.isFemale() && user.getPregnant() && !user.getDrink()) {
                    url = "quanto-voce-bebe-nao-adoles-gravidez.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);

                } else if (!user.isUnderage() && user.isFemale() && user.getPregnant() && user.getDrink()) {
                    url = "quanto-voce-bebe-sim-gravidez.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);

                } else if (user.isUnderage() && user.isFemale() && user.getPregnant() && user.getDrink()) {
                    url = "quanto-voce-bebe-sim-adoles-gravidez.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);

                } else if (user.isUnderage() && !user.getDrink()) {
                    url = "quanto-voce-bebe-nao-adoles.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                } else if (user.isUnderage() && user.getDrink()) {
                    url = "quanto-voce-bebe-sim-adoles.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                } else if (!user.getDrink()) {
                    url = "quanto-voce-bebe-abstemio.xhtml";
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                }
            } catch (IOException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getString(String key) {
        bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(user.getPreferedLanguage()));
        return bundle.getString(key);
    }

    public Date getCurrentDate() {
        return new Date();
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

    public int getDia() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            dia = birth.get(Calendar.DAY_OF_MONTH);
        } else {
            dia = 0;

        }
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMes() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            mes = birth.get(Calendar.MONTH);
        } else {
            mes = 12; //Months are being counted from 0 to 11, so 12 is "none"
        }
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            ano = birth.get(Calendar.YEAR);
        } else {
            ano = 0;
        }
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public Map<String, String> getDias() {
        return dias;
    }

    public void setDias(Map<String, String> dias) {
        this.dias = dias;
    }

    public Map<String, String> getMeses() {
        meses.clear();
        for (int i = 1; i <= 12; i++) {
            meses.put(getString("month." + String.valueOf(i)),
                    String.valueOf(i - 1));
        }
        return meses;
    }

    public void setMeses(Map<String, String> meses) {
        this.meses = meses;
    }

    /**
     * @return the anos
     */
    public Map<String, String> getAnos() {
        return anos;
    }

    /**
     * @param anos the anos to set
     */
    public void setAnos(Map<String, String> anos) {
        this.anos = anos;
    }

    @Override
    public User getLoggedUser() {
        return super.getLoggedUser();
    }

    public String getEditPassword() {
        return editPassword;
    }

    public void setEditPassword(String editPassword) {
        this.editPassword = editPassword;
    }

    public int getEditDia() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            editDia = birth.get(Calendar.DAY_OF_MONTH);
        } else {
            editDia = 0;

        }
        return editDia;
    }

    public void setEditDia(int editDia) {
        this.editDia = editDia;
    }

    public int getEditMes() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            editMes = birth.get(Calendar.MONTH);
        } else {
            editMes = 12; //Months are being counted from 0 to 11, so 12 is "none"
        }
        return editMes;
    }

    public void setEditMes(int editMes) {
        this.editMes = editMes;
    }

    public int getEditAno() {
        Calendar birth = Calendar.getInstance();
        if (getUser().getBirthDate() != null) {
            birth.setTime(getUser().getBirthDate());
            editAno = birth.get(Calendar.YEAR);
        } else {
            editAno = 0;
        }
        return editAno;
    }

    public void setEditAno(int editAno) {
        this.editAno = editAno;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
    }

    private String getIpAdress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public boolean isAdmin() {
        return (getUser().isAdmin());
    }

    public boolean isInRanking() {
        return (getUser().isInRanking());
    }

    public List<User> userList() {
        try {
            userList = this.getDaoBase().listNotNull("email", getEntityManager());
        } catch (SQLException ex) {
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"), null));
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userList;
    }

    public void setAdmin(User u) {
        if (u.getEmail() == null ? this.getUser().getEmail() != null : !u.getEmail().equals(this.getUser().getEmail())) {
            u.setAdmin(!u.isAdmin());

            try {
                daoBase.update(u, getEntityManager());
                //administrator = null;
            } catch (SQLException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setConsultant(User u) {
        if (this.getUser().getEmail() != null) {
            u.setConsultant(!u.isConsultant());

            try {
                daoBase.update(u, getEntityManager());
                //administrator = null;
            } catch (SQLException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setUseChatbot(User u) {
        if (this.getUser().getEmail() != null) {
            u.setUse_chatbot(!u.isUse_chatbot());

            try {
                daoBase.update(u, getEntityManager());
                //administrator = null;
            } catch (SQLException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
