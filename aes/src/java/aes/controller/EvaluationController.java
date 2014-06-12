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
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiagorizuti
 */
@ManagedBean(name = "evaluationController")
@RequestScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int HOURS_LIMIT = 24;

    private String email;

    private Evaluation evaluation;


    public EvaluationController() {
        try {
            this.daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            String message = "Ocorreu um erro inesperado.";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public Evaluation getEvaluation() {
        if (this.evaluation == null) {

            GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
            gc.add(GregorianCalendar.HOUR, EvaluationController.HOURS_LIMIT);

            User user = new User();
            user.setEmail(this.email);

            if (!this.email.trim().isEmpty() || this.email != null) {

                try {

                    List<Evaluation> evaluations = this.getDaoBase().list("user", user, this.getEntityManager());
                    for (Evaluation e : evaluations) {
                        if (gc.after(e.getDate())) {
                            this.evaluation = e;
                        }
                        if (this.evaluation == null) {
                            this.evaluation = new Evaluation();
                            this.evaluation.setUser(user);
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

    public String intro() {
        if (this.getDrink() == 1) {
            return "quanto-voce-bebe-sim-beber-pesado.xhtml";
        } else {
            return "quanto-voce-bebe-abstemio.xhtml";
        }
    }

    public String heavyDrinking() {
        if (this.getEvaluation().getHeavySum() <= 3) {
            if (this.getEvaluation().getUser().getGender() == 'M' && this.getEvaluation().getUser().getAge() <= 65) {
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml";
            } else {
                return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml";
            }
        } else {
            return "quanto-voce-bebe-sim-beber-dependencia.xhtml";
        }
    }
    
    public void sendEmail(){
        
    }

    public int getDay() {
        if (this.evaluation == null) {
            getEvaluation();
        }
        return this.evaluation.getDate().get(Calendar.DAY_OF_MONTH);
    }

    public void setDay(int day) {
        if (this.evaluation == null) {
            getEvaluation();
        }
        this.evaluation.getDate().set(Calendar.DAY_OF_MONTH, day);
    }
    
    public int getMonth() {
        if (this.evaluation == null) {
            getEvaluation();
        }
        return this.evaluation.getDate().get(Calendar.MONTH);
    }

    public void setMonth(int month) {
        if (this.evaluation == null) {
            getEvaluation();
        }
        this.evaluation.getDate().set(Calendar.MONTH, month);
    }
    
      public int getYear() {
        if (this.evaluation == null) {
            getEvaluation();
        }
        return this.evaluation.getDate().get(Calendar.YEAR);
    }

    public void setYear(int year) {
        if (this.evaluation == null) {
            getEvaluation();
        }
        this.evaluation.getDate().set(Calendar.YEAR, year);
    }
    
    public int getDrink(){
        if(this.evaluation == null){
            getEvaluation();
        }
        if(this.evaluation.isDrink()){
            return 1;
        }else{
            return 0;
        }
    }
    
    public void setDrink(int drink){
        if(this.evaluation == null){
            getEvaluation();
        }
        if(drink == 1){
            this.evaluation.setDrink(true);
        }else{
            this.evaluation.setDrink(false);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    

    

}
