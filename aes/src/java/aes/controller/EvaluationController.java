package aes.controller;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.PDFGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.hibernate.TransientObjectException;

@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int DAYS_LIMIT = -7;

    private Evaluation evaluation;

    @ManagedProperty(value = "#{userController}")
    private UserController userController;
    @ManagedProperty(value = "#{contactController}")
    private ContactController contactController;
    @ManagedProperty(value = "#{pageNavigationController}")
    private PageNavigationController pageNavigationController;

    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Evaluation getEvaluation() {
        if (evaluation == null && getUser().getId() != 0) {
            try {
                List<Evaluation> evaluations = this.getDaoBase().list("user", getUser(), this.getEntityManager());
                if (!evaluations.isEmpty()) {
                    Calendar limit = Calendar.getInstance();
                    limit.add(Calendar.DATE, EvaluationController.DAYS_LIMIT);
                    Calendar eDate = Calendar.getInstance();
                    for (Evaluation e : evaluations) {
                        eDate.setTime(e.getDateCreated());
                        if (eDate.after(limit)) {
                            evaluation = e;
                            limit.setTime(evaluation.getDateCreated());
                        }
                    }
                }
                if (evaluation == null) {
                    evaluation = new Evaluation();
                    evaluation.setDateCreated(new Date());
                    evaluation.setUser(getUser());
                }

            } catch (SQLException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransientObjectException ex) {

            }

        }
        return evaluation;
    }

    public User getUser() {
        return userController.getUser();
    }

    public void save() {
        try {
            daoBase.insertOrUpdate(getEvaluation(), getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String audit3() {
        userController.save();
        if (getEvaluation().audit3LimitExceeded() || getEvaluation().dayLimitExceeded() || getEvaluation().weekLimitExceeded()) {
            return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml?faces-redirect=true";
        } else if (getUser().isMale() && getUser().getAge() <= 65) {
            return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
        } else if (getUser().isMale() && getUser().getAge() > 65 || getUser().isFemale()) {
            return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
        } else {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, "audit3 - expressão booleana não satisfeita" + getUser().getGender() + getUser().getAge() + getEvaluation().getAudit3Sum() + getEvaluation().getWeekTotal() + getEvaluation().dayLimitExceeded());
        }
        return "";
    }

    public String audit7() {
        if (getEvaluation().getAuditFullSum() <= 17) {
            if (getEvaluation().dayLimitExceeded() || getEvaluation().weekLimitExceeded()) {
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-baixo-risco-limites?faces-redirect=true";
           } else if (getUser().isMale() && getUser().getAge() <= 65) {
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
           }else if (getUser().isMale() && getUser().getAge() > 65 || getUser().isFemale()){
               return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
           }
        } else if (getEvaluation().getAuditFullSum() >= 17 && getEvaluation().getAuditFullSum() <= 25) {
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-risco.xhtml?faces-redirect=true";
        } else if (getEvaluation().getAuditFullSum() >= 26 && getEvaluation().getAuditFullSum() <= 29) {
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-nocivo.xhtml?faces-redirect=true";
        } else if (getEvaluation().getAuditFullSum() >= 30 && getEvaluation().getAuditFullSum() <= 50) {
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-dependencia.xhtml?faces-redirect=true";
        }else {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, "audit7 - expressão booleana não satisfeita" + getUser().getGender() + getUser().getAge() + getEvaluation().getAuditFullSum() + getEvaluation().getWeekTotal()+ getEvaluation().dayLimitExceeded());
        }
        return "";
    }

    public String readyToChange() {
        if (getEvaluation().getReady()) {
            saveURL();
            return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
        } else {
            return "preparando-diminuir-parar-nao.xhtml?faces-redirect=true";

        }
    }

    public String goBack() {
        if (getEvaluation().getGoBack()) {
            saveURL();
            contactController.clearAnnualScreeningEmails(getUser());
            return "preparando-diminuir-ou-parar.xhtml?faces-redirect=true";
        } else {
            annualScreening();
            return "";
        }
    }

    public String dependenceContinue() {
        getEvaluation().setQuit(true);
        saveURL();
        return ("estrategia-parar-apoio-intro.xhtml?faces-redirect=true");
    }
    
    public String goToIndex() {
        getEvaluation().setQuit(true);
        saveURL();
        return ("index.xhtml");
    }

    public String cutDownQuit() {
         if (getUser().isReceiveEmails()) {
            if (getEvaluation().getQuit()) {
                contactController.scheduleKeepingResultQuitEmail(getUser(), new Date());
                contactController.schedulePersistChallengesQuitEmail(getUser(), new Date());
            } else {
                contactController.scheduleKeepingResultReduceEmail(getUser(), new Date());
                contactController.schedulePersistChallengesReduceEmail(getUser(), new Date());                
            }
        }
        if (getEvaluation().getQuit()) {
            return "estrategia-diminuir-introducao.xhtml?faces-redirect=true";
        } else {
            saveURL();
            return "estrategia-parar-apoio-intro.xhtml?faces-redirect=true";

        }
    }

    public String goToPlan() {
        saveURL();
        return "plano-mudanca.xhtml?faces-redirect=true";
    }

    public void annualScreening() {
        if(getUserController().getUser().getName() == null){
            getUserController().getUser().setName(getUserController().getUser().getEmail());
        }
        contactController.clearScheduledEmails(getUserController().getUser());
        contactController.scheduleAnnualScreeningEmail(getUserController().getUser());
        disableSaveBtn();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("completed.evaluation"), null));
        if (!userController.isLoggedIn()) {
            ((InputText) getComponent("email")).setDisabled(true);
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        }
    }
    
    public void registerInfo(){
        if(getUserController().getUser().getName() == null){
            getUserController().getUser().setName(getUserController().getUser().getEmail());
        }
        contactController.sendSignInEmail(getUserController().getUser());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("completed.evaluation"), null));
        if (!userController.isLoggedIn()) {
            ((InputText) getComponent("email")).setDisabled(true);
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        }
    }
    
    public void enableSaveBtn() {
        ((CommandButton) getComponent("saveBtn")).setDisabled(false);
    }

    public void disableSaveBtn() {
        ((CommandButton) getComponent("saveBtn")).setDisabled(true);
    }

    public void savePlan() {
        save();
        FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("completed.evaluation.plan"), null));
    }

    public void sendPlan() {
        contactController.sendPlanEmail(getUser(), "meuplano.pdf", getPlanPDF());
        FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, userController.getString("send.plan"), null));
    }

    public StreamedContent printPlan() {
        ByteArrayOutputStream pdf = getPlanPDF();
        return new DefaultStreamedContent(new ByteArrayInputStream(pdf.toByteArray()),
                "application/pdf", "meuplano.pdf", "UTF-8");
    }

    public ByteArrayOutputStream getPlanPDF() {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("aes/utility/plan-template.html");
            byte[] buffer = new byte[102400];
            String template = new String(buffer, 0, input.read(buffer),StandardCharsets.UTF_8);

            ResourceBundle bundle = PropertyResourceBundle.getBundle("aes.utility.messages", new Locale(getUser().getPreferedLanguage()));
            String subtitle[] = new String[6];
            String title = bundle.getString("plan0");
            subtitle[0] = bundle.getString("plan1");
            subtitle[1] = bundle.getString("plan2");
            subtitle[2] = bundle.getString("plan3");
            subtitle[3] = bundle.getString("plan4");
            subtitle[4] = bundle.getString("plan5");
            subtitle[5] = bundle.getString("plan6");
            template = template.replace("#title#", title);
            for (int i = 0; i < 6; i++) {
                template = template.replace("#subtitle" + (i + 1) + "#", subtitle[i]);
                template = template.replace("#content" + (i + 1) + "#", getEvaluation().getPlanContent()[i]);
            }
            template = template.replace("#subintro#", bundle.getString("plan.subintro"));
            template = template.replace("#intro#", bundle.getString("plan.intro"));
            template = template.replace("#user#", getUser().getName());
            template = template.replace("#footer#",
                    bundle.getString("title.1") + "<br/>"
                    + bundle.getString("crepeia") + "<br/>"
                    + bundle.getString("ufjf"));
            PDFGenerator pdfGenerator = new PDFGenerator();
            return pdfGenerator.generatePDF(template);
        } catch (IOException ex) {
            Logger.getLogger(ContactController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void redirectAudit(boolean redirect) {
        if (redirect) {
            try {
                List<Evaluation> evaluations = this.getDaoBase().listOrdered("user", getUser(), "dateCreated", getEntityManager());
                Evaluation evaluation = null;
                if (evaluations.isEmpty()) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-sim-beber-uso-audit-3.xhtml");
                } else {
                    evaluation = evaluations.get(evaluations.size() - 1);
                    if (evaluation.getAudit3() == null) {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-sim-beber-uso-audit-3.xhtml");
                    } else if (evaluation.getAudit3Sum() > 6 && evaluation.getAudit10() == null) {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("quanto-voce-bebe-sim-beber-uso-audit-3.xhtml");
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getURL() {
        String url = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
        if (url == null) {
            return "index.xhtml?faces-redirect=true";
        }
        return url;
    }

    public void saveURL() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = ((HttpServletRequest) request).getRequestURI();
        url = url.substring(url.lastIndexOf('/') + 1);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("url", url);
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public ContactController getContactController() {
        return contactController;
    }

    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

    public PageNavigationController getPageNavigationController() {
        return pageNavigationController;
    }

    public void setPageNavigationController(PageNavigationController pageNavigationController) {
        this.pageNavigationController = pageNavigationController;
    }

}
