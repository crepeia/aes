/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiagorizuti
 */
@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int HOURS_LIMIT = 24 * 7;

    private User user;
    private Evaluation evaluation;
   
    private String gender;
    private Integer drink;
    private Integer day;
    private Integer month;
    private Integer year;
    private String email;
    
    private String message;

    private GenericDAO userDAO;

    public EvaluationController() {
        try {
            this.userDAO = new GenericDAO(User.class);
            this.daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            String message = "Ocorreu um erro inesperado.";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public Evaluation getEvaluation() {
        if (this.evaluation == null) {
            System.out.println("Procurando avaliação");

            GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
            gc.add(GregorianCalendar.HOUR, EvaluationController.HOURS_LIMIT);

            if (this.getUser() != null) {

                try {

                    List<Evaluation> evaluations = this.getDaoBase().list("user", this.getUser(), this.getEntityManager());
                    for (Evaluation e : evaluations) {
                        if (gc.after(e.getDate()) && e.isDrink() == this.getDrinkBoolean()) {
                            this.evaluation = e;
                            System.out.println("Carregou avaliação " + e.getId());
                        }
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                Logger.getLogger(Evaluation.class.getName()).log(Level.SEVERE, "Usuário não identificado sendo avaliado.");
                this.evaluation = new Evaluation();
            }

        }

        return evaluation;
    }

    public User getUser() {
        if (loggedUser()) {
            return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
        }
        return user;
    }

    public String intro() {
        if (this.getUser() == null) {
            this.user = new User();
            this.user.setGender(this.gender.charAt(0));
            this.user.setBirth(this.year, this.month, this.day);
        }
        try {
            this.userDAO.insertOrUpdate(this.getUser(), this.getEntityManager());

            if (this.getEvaluation() == null) {
                this.evaluation = new Evaluation();
                System.out.println("Nova avaliação");
                this.evaluation.setDate(GregorianCalendar.getInstance());
                this.evaluation.setUser(this.getUser());
                if (this.drink == 1) {
                    this.getEvaluation().setDrink(true);
                } else {
                    this.getEvaluation().setDrink(false);
                }
            }

            this.daoBase.insertOrUpdate(this.getEvaluation(), this.getEntityManager());

            if (this.drink == 1) {
                return "quanto-voce-bebe-sim-beber-uso-audit-3?faces-redirect=true";
            } else {
                return "quanto-voce-bebe-abstemio.xhtml?faces-redirect=true";
            }
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ocorreu um erro inesperado.", null));
            return "";
        }

    }

    public String heavyDrinking() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (this.getEvaluation().getSum() <= 3) {
            if (this.getEvaluation().getUser().getGender() == 'M' && this.getEvaluation().getUser().getAge() <= 65) {
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
            } else {
                return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
            }
        } else {
            Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
            if (obj != null) {
                User user = (User) obj;
                if (user.getId() > 0) {
                    return "quanto-voce-bebe-sim-beber-dependencia.xhtml?faces-redirect=true";
                }
            }

            return "cadastrar-nova-conta.xhtml?faces-redirect=true";
        }
    }

    public void sendEmail() {
        this.evaluation.setYearEmail(true);
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        String message = "Email enviado";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
        Logger.getLogger(BaseFormController.class.getName()).log(Level.INFO, message);
        this.clear();
    }
    
    public void clear(){
        this.email = "";
        this.message = "";
    }

    public boolean loggedUser() {
        Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
        return obj != null;
    }

    public String getGender() {
        if (loggedUser()) {
            return String.valueOf(this.getUser().getGender());
        }
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getDrink() {
        return drink;
    }

    public Boolean getDrinkBoolean() {
        if (this.drink == 1) {
            return true;
        } else if (this.drink == 0) {
            return false;
        } else {
            return null;
        }
    }

    public void setDrink(Integer drink) {
        this.drink = drink;
    }

    public Integer getDay() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.DAY_OF_MONTH);
        }
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.MONTH);
        }
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        if (loggedUser()) {
            return this.getUser().getBirth().get(Calendar.YEAR);
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getEmail() {
        if (loggedUser()) {
            return this.getUser().getEmail();
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
    public boolean sameDate(Calendar firstDate, Calendar secondDate) {
        return firstDate.get(Calendar.DAY_OF_MONTH) == secondDate.get(Calendar.DAY_OF_MONTH)
                && firstDate.get(Calendar.MONTH) == secondDate.get(Calendar.MONTH)
                && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR);
    }

}
