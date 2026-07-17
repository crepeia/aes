package aes.controller;

import aes.model.User;
import aes.persistence.UserDAOv2;
import aes.utility.EncrypterException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author thiago
 * @author luansb (refactor)
 */
@Named("userController")
@SessionScoped
public class UserController extends BaseController<User> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(UserController.class.getName());

    private static final String BUNDLE_NAME = "aes.utility.messages";
    private static final String CACHE_USER_LIST = "userController.userList";
    private static final String CACHE_MESES = "userController.meses";
    private static final String COOKIE_POLICY = "cookiesPolicy";
    private static final String SESSION_URL_KEY = "url";

    @Inject
    private UserDAOv2 userDAO;

    @Inject
    private ContactController contactController;

    private User user;
    private boolean loggedIn;

    private transient String password;
    private transient String editPassword;
    private transient String confirmEmail;
    
    private String email;
    private Integer recoverCode;
    private transient String passwordd;

    private int dia;
    private int mes;
    private int ano;
    private int editDia;
    private int editMes;
    private int editAno;

    public void signIn(boolean redirect) {
        String email = getUser().getEmail();
        try {
            User found = userDAO.checkCredentials(email, password);

            if (found == null) {
                LOG.log(Level.WARNING, "Usuario ''{0}'' nao conseguiu logar.", email);
                addMessage(null, FacesMessage.SEVERITY_ERROR, "E-mail ou senha inválida.");
                return;
            }

            this.user = userDAO.registerSignIn(found, new Date());
            this.loggedIn = true;
            LOG.log(Level.INFO, "Usuario ''{0}'' logou no sistema.", email);

            if (redirect) {
                Object stored = FacesContext.getCurrentInstance()
                        .getExternalContext().getSessionMap().get(SESSION_URL_KEY);
                redirectTo(stored != null ? (String) stored : "escolha-uma-etapa.xhtml");
            } else {
                redirectTo(currentViewName());
            }

        } catch (EncrypterException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            this.password = null;
        }
    }

    public void signOut() {
        if (user != null) {
            LOG.log(Level.INFO, "Usuario ''{0}'' saiu do sistema.", user.getEmail());
        }
        this.user = null;
        this.loggedIn = false;
        this.password = null;

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("/aes/index.xhtml");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void clearSession() {
        // mantido para compatibilidade de EL
    }

    public void signUp() {
        User candidate = getUser();

        if (!Objects.equals(confirmEmail, candidate.getEmail())) {
            addMessage(null, FacesMessage.SEVERITY_ERROR, getString("email.not.equals"));
            return;
        }

        try {
            List<User> existing = userDAO.findByEmail(candidate.getEmail());
            // lista vazia => registrationComplete == true (mesma semantica do reduce original)
            boolean regComplete = existing.stream()
                    .allMatch(User::isRegistration_complete);

            if (!existing.isEmpty() && regComplete) {
                addMessage(null, FacesMessage.SEVERITY_ERROR, getString("email.used"));
                return;
            }

            if (!existing.isEmpty()) { // cadastro incompleto: reaproveita o registro
                candidate.setId(existing.get(0).getId());
            }

            candidate.setRegistration_complete(true);
            candidate.setSignUpDate(new Date());
            stampCreation(candidate); // rede de seguranca: dateCreated/ipCreated nunca nulos

            this.user = userDAO.createUser(candidate, password);

            try {
                contactController.sendSignUpEmail(user);
            } catch (MessagingException | MissingResourceException ex) {
                LOG.log(Level.INFO, "{0}: sign-up email NOT sent.", user.getEmail());
                LOG.log(Level.SEVERE, null, ex);
            }

            if (user.isReceiveEmails()) {
                contactController.scheduleTipsEmail(user);
                contactController.scheduleDiaryReminderEmail(user, new Date());
                contactController.scheduleWeeklyEmail(user, new Date());
            }

            LOG.log(Level.INFO, "Usuario ''{0}'' cadastrou no sistema.", user.getEmail());
            signIn(true); // limpa password no finally dele

        } catch (EncrypterException ex) {
            LOG.log(Level.SEVERE, null, ex);
            addMessage(null, FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"));
            this.password = null;
        }
    }

    public void editProfile() {
        final User current = getUser();

        try {
            boolean emailAvailable = userDAO.findByEmail(current.getEmail()).stream()
                    .allMatch(u -> Objects.equals(u.getId(), current.getId()));

            if (!emailAvailable) {
                addMessage(null, FacesMessage.SEVERITY_FATAL, getString("email.cadastrado"));
                return;
            }

            if (editPassword != null && !editPassword.trim().isEmpty()) {
                this.user = userDAO.changePassword(current, editPassword);
            } else {
                this.user = userDAO.insertOrUpdate(current);
            }

            editAno = 0;
            editMes = 0;
            editDia = 0;

            redirectTo("index.xhtml");

        } catch (EncrypterException ex) {
            LOG.log(Level.SEVERE, null, ex);
            addMessage(null, FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) { // PersistenceException & cia
            LOG.log(Level.SEVERE, null, ex);
            addMessage(null, FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"));
        } finally {
            this.editPassword = null;
        }
    }

    public void recoverPassword() {
        try {
            List<User> found = userDAO.findByEmail(getUser().getEmail());

            if (found.isEmpty()) {
                addMessage("error", FacesMessage.SEVERITY_ERROR, getString("email.not.registred"));
                return;
            }

            User target = userDAO.createRecoveryCode(found.get(0));
            contactController.sendPasswordRecoveryEmail(target);
            addMessage("info", FacesMessage.SEVERITY_INFO, getString("email.instructions.password"));

        } catch (MessagingException | MissingResourceException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void checkCode() {
        if (password == null || password.trim().isEmpty()) {
            addMessage("error", FacesMessage.SEVERITY_ERROR, getString("problemas.gravar.usuario"));
            return;
        }
        try {
            List<User> found = userDAO.findByEmail(getUser().getEmail());

            if (found.isEmpty()) {
                addMessage("error", FacesMessage.SEVERITY_ERROR, getString("email.not.registered"));
                return;
            }

            User target = found.get(0);
            Integer informed = getUser().getRecoverCode();

            if (target.getRecoverCode() == null || !target.getRecoverCode().equals(informed)) {
                addMessage("error", FacesMessage.SEVERITY_ERROR, getString("code.invalid"));
                return;
            }

            target.setRecoverCode(null);
            userDAO.changePassword(target, password);
            addMessage("info", FacesMessage.SEVERITY_INFO, getString("redefined.password"));

        } catch (EncrypterException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            this.password = null;
        }
    }

    public void sendEmailRequestingDeleteAccount() throws MessagingException, SQLException {
        String email = getUser().getEmail();
        try {
            User found = userDAO.checkCredentials(email, password);

            if (found == null) {
                LOG.log(Level.WARNING, "Usuario ''{0}'' nao conseguiu solicitar remocao de conta.", email);
                addMessage("info", FacesMessage.SEVERITY_ERROR, "E-mail ou senha inválida.");
                return;
            }

            contactController.sendDeleteAccountEmail(found);
            addMessage("info", FacesMessage.SEVERITY_INFO, getString("email.instructions.deleteAccount"));
            LOG.log(Level.INFO, "Usuario ''{0}'' solicitou a remocao da conta.", email);

        } catch (EncrypterException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            this.password = null;
        }
    }

    public void save() {
        this.user = userDAO.insertOrUpdate(getUser());
    }

    public void setBirth() {
        getUser().setBirth(ano, mes, dia);
    }

    public void setBirthEdit() {
        getUser().setBirth(editAno, editMes, editDia);
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
        }
        return 1;
    }

    public void redirectLogin(boolean redirect) {
        if (!redirect || loggedIn) {
            return;
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put(SESSION_URL_KEY, currentViewName());
            redirectTo("cadastrar-nova-conta.xhtml");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void redirectIndex(boolean redirect) {
        if (!redirect || !loggedIn) {
            return;
        }
        try {
            redirectTo("index.xhtml");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void redirectEvaluation(boolean redirect) {
        if (!redirect) {
            return;
        }
        String url = evaluationUrl(getUser());
        if (url == null) {
            return;
        }
        try {
            redirectTo(url);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private String evaluationUrl(User u) {
        if (u.getDrink() == null) {
            return "quanto-voce-bebe-introducao.xhtml";
        }
        boolean pregnant = u.isFemale() && Boolean.TRUE.equals(u.getPregnant());
        boolean drinks = Boolean.TRUE.equals(u.getDrink());
        boolean underage = u.isUnderage();

        if (pregnant && !drinks) {
            return underage ? "quanto-voce-bebe-nao-adoles-gravidez.xhtml"
                            : "quanto-voce-bebe-nao-gravidez.xhtml";
        }
        if (pregnant) {
            return underage ? "quanto-voce-bebe-sim-adoles-gravidez.xhtml"
                            : "quanto-voce-bebe-sim-gravidez.xhtml";
        }
        if (underage) {
            return drinks ? "quanto-voce-bebe-sim-adoles.xhtml"
                          : "quanto-voce-bebe-nao-adoles.xhtml";
        }
        if (!drinks) {
            return "quanto-voce-bebe-abstemio.xhtml";
        }
        return null; // bebe, maior de idade, nao gestante: sem redirect (igual ao original)
    }

    public List<User> userList() {
        return requestCache(CACHE_USER_LIST, () -> userDAO.listNotNull("email"));
    }

    public void setAdmin(User u) {
        if (!Objects.equals(u.getEmail(), getUser().getEmail())) {
            u.setAdmin(!u.isAdmin());
            userDAO.insertOrUpdate(u);
            invalidateUserListCache();
        }
    }

    public void setConsultant(User u) {
        if (getUser().getEmail() != null) {
            u.setConsultant(!u.isConsultant());
            userDAO.insertOrUpdate(u);
            invalidateUserListCache();
        }
    }

    public void setUseChatbot(User u) {
        if (getUser().getEmail() != null) {
            u.setUse_chatbot(!u.isUse_chatbot());
            userDAO.insertOrUpdate(u);
            invalidateUserListCache();
        }
    }

    public boolean isAdmin() {
        return getUser().isAdmin();
    }

    public boolean isInRanking() {
        return getUser().isInRanking();
    }

    public void acceptCookies() {
        writePolicyCookie(30 * 24 * 60 * 60);
        try {
            redirectTo(currentViewName());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void knowMoreCookies() {
        writePolicyCookie(3);
        try {
            redirectTo("politica-do-site.xhtml");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void exitCookies() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("https://google.com");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public boolean showCookiesAlert() {
        HttpServletRequest request = currentRequest();
        if (request == null || request.getCookies() == null) {
            return true;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_POLICY.equals(cookie.getName())) {
                return Boolean.parseBoolean(cookie.getValue());
            }
        }
        return true;
    }

    private void writePolicyCookie(int maxAgeSeconds) {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
        Cookie cookie = new Cookie(COOKIE_POLICY, "false");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setHttpOnly(true);
        cookie.setPath(currentRequest() != null ? currentRequest().getContextPath() : "/");
        response.addCookie(cookie);
    }

    public String getString(String key) {
        return ResourceBundle.getBundle(BUNDLE_NAME, currentLocale()).getString(key);
    }

    private Locale currentLocale() {
        String lang = (user != null) ? user.getPreferedLanguage() : null;
        if (lang == null || lang.trim().isEmpty()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null && fc.getViewRoot() != null && fc.getViewRoot().getLocale() != null) {
                return fc.getViewRoot().getLocale();
            }
            return Locale.getDefault();
        }
        return new Locale(lang);
    }

    public Map<String, String> getMeses() {
        return requestCache(CACHE_MESES, () -> {
            Map<String, String> meses = new LinkedHashMap<>();
            for (int i = 1; i <= 12; i++) {
                meses.put(getString("month." + i), String.valueOf(i - 1));
            }
            return meses;
        });
    }

    private HttpServletRequest currentRequest() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return null;
        }
        Object request = fc.getExternalContext().getRequest();
        return (request instanceof HttpServletRequest) ? (HttpServletRequest) request : null;
    }

    private String currentViewName() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return "index.xhtml";
        }
        String url = request.getRequestURI();
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private void redirectTo(String url) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    }

    private void addMessage(String clientId, FacesMessage.Severity severity, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(clientId, new FacesMessage(severity, detail, null));
    }

    private String getIpAdress() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    @SuppressWarnings("unchecked")
    private <V> V requestCache(String key, Supplier<V> supplier) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return supplier.get();
        }
        Map<Object, Object> attrs = fc.getAttributes();
        V cached = (V) attrs.get(key);
        if (cached == null) {
            cached = supplier.get();
            if (cached != null) {
                attrs.put(key, cached);
            }
        }
        return cached;
    }

    private void invalidateUserListCache() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.getAttributes().remove(CACHE_USER_LIST);
        }
    }

    /** Preserva EXATAMENTE as informacoes que o @PostConstruct antigo gravava. */
    private void stampCreation(User u) {
        if (u.getDateCreated() == null) {
            u.setDateCreated(new Date());
        }
        if (u.getIpCreated() == null) {
            u.setIpCreated(getIpAdress());
        }
    }

    public User getUser() {
        if (user == null) {
            user = new User();
            stampCreation(user);
        }
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

    public String getEditPassword() {
        return editPassword;
    }

    public void setEditPassword(String editPassword) {
        this.editPassword = editPassword;
    }

    public String getConfirmEmail() {
        return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        this.confirmEmail = confirmEmail;
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

    public Date getCurrentDate() {
        return new Date();
    }

    @Override
    public User getLoggedUser() {
        return super.getLoggedUser();
    }

    public int getDia() {
        dia = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getDayOfMonth() : 0;
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMes() {
        mes = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getMonthValue() - 1 : 12;
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        ano = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getYear() : 0;
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getEditDia() {
        editDia = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getDayOfMonth() : 0;
        return editDia;
    }

    public void setEditDia(int editDia) {
        this.editDia = editDia;
    }

    public int getEditMes() {
        editMes = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getMonthValue() - 1 : 12;
        return editMes;
    }

    public void setEditMes(int editMes) {
        this.editMes = editMes;
    }

    public int getEditAno() {
        editAno = (getUser().getBirthDate() != null) ? getUser().getBirthDate().getYear() : 0;
        return editAno;
    }

    public void setEditAno(int editAno) {
        this.editAno = editAno;
    }
}