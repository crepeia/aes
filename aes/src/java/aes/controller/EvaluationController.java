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
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

/**
 *
 * @author thiagorizuti and danielapereira
 */
@ManagedBean(name = "evaluationController")
@SessionScoped
public class EvaluationController extends BaseController<Evaluation> {

    private static final int HOURS_LIMIT = 24 * 7;

    private User user;
    private Evaluation evaluation;

    private Integer gender;
    private Integer drink;
    private Integer day;
    private Integer month;
    private Integer year;
    private Integer pregnant;
    
    private boolean continueEvaluation;
    
    private GenericDAO userDAO;

    public EvaluationController() {
        gender = 3;
        pregnant = 3;
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
            return getLoggedUser();
        }
        return user;
    }
    
    public String intro1(){
        Calendar cal = GregorianCalendar.getInstance();
        int anoAtual = Integer.valueOf(cal.get(Calendar.YEAR));
        int mesAtual = Integer.valueOf(cal.get(Calendar.MONTH));
        int diaAtual = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        //System.out.println(cal.get(Calendar.YEAR)); 
        System.out.println("ano atual: " + anoAtual);
        System.out.println("mes atual: " + mesAtual);
        System.out.println("dia atual: " + diaAtual);
        
        int difAno, difMes, difDia;
        difAno = anoAtual - this.year;
        difMes = this.month - mesAtual;
        difDia = diaAtual - this.day;
        int age;
        if(difMes <= 0){
            if(difDia >= this.day)
                age = difAno;
            else
                age = difAno - 1;
        }else{
            age = difAno - 1;
        }
        
        //int age = anoAtual - this.year;
        System.out.println("idade: " + age);
        if(this.pregnant == 1 && this.drink == 2){
            return "quanto-voce-bebe-nao-gravidez.xhtml?faces-redirect=true";
        }else if(this.pregnant == 1 && this.drink == 1){
            return "quanto-voce-bebe-sim-gravidez.xhtml?faces-redirect=true";
        }else if(age < 18 && drink == 2){
            return "quanto-voce-bebe-nao-adoles.xhtml?faces-redirect=true";
        }else if(age < 18 && drink == 1){
            return "quanto-voce-bebe-sim-adoles.xhtml?faces-redirect=true";
        }else if(drink == 2){
            return "quanto-voce-bebe-abstemio.xhtml?faces-redirect=true";
        }else
            return "quanto-voce-bebe-convite.xhtml?faces-redirect=true";
    }  

    public String intro() {
        if (this.getUser() == null) {
            this.user = new User();
            //this.user.setGender(this.gender.charAt(0));
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
    
    public String audit3(){ 
        Calendar cal = GregorianCalendar.getInstance();
        int anoAtual = Integer.valueOf(cal.get(Calendar.YEAR));
        int mesAtual = Integer.valueOf(cal.get(Calendar.MONTH));
        int diaAtual = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        int difAno, difMes, difDia;
        difAno = anoAtual - this.year;
        difMes = this.month - mesAtual;
        difDia = diaAtual - this.day;
        int age;
        if(difMes <= 0){
            if(difDia >= this.day)
                age = difAno;
            else
                age = difAno - 1;
        }else{
            age = difAno - 1;
        }
        
        int sum1 = evaluation.getAudit1();
        int sum2 = evaluation.getAudit2();
        int sum3 = evaluation.getAudit3();
        
        int sumTotal = sum1 + sum2 + sum3;
        if(sumTotal > 6 && (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation1() > 1) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation1() > 2) || (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation2() > 5) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation2() > 10)){
            return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml";
        }else if(sumTotal <= 6 && (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation1() <= 1) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation1() <= 2) || (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation2() <= 5) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation2() <= 10) ){
            if(this.getUser().getGender() == 'M' && age <= 65)
                return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml";
            else if((this.getUser().getGender() == 'M' && age > 65) || this.getUser().getGender() == 'F' && age > 65)
                return "quanto-voce-bebe-reconmendar-limites-mulheres-e-homens-com-mais-de-65-anos.xhtml";
        }
        return "";  
    }
    
    public String audit7(){
        Calendar cal = GregorianCalendar.getInstance();
        int anoAtual = Integer.valueOf(cal.get(Calendar.YEAR));
        int mesAtual = Integer.valueOf(cal.get(Calendar.MONTH));
        int diaAtual = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        int difAno, difMes, difDia;
        difAno = anoAtual - this.year;
        difMes = this.month - mesAtual;
        difDia = diaAtual - this.day;
        int age;
        if(difMes <= 0){
            if(difDia >= this.day)
                age = difAno;
            else
                age = difAno - 1;
        }else{
            age = difAno - 1;
        }
        
        int sum1 = evaluation.getAudit4();
        int sum2 = evaluation.getAudit5();
        int sum3 = evaluation.getAudit6();
        int sum4 = evaluation.getAudit7();
        int sum5 = evaluation.getAudit8();
        int sum6 = evaluation.getAudit9();
        int sum7 = evaluation.getAudit10();
        
        int sumTotal = sum1 + sum2 + sum3 + sum4 + sum5 + sum6 + sum7;
        
        if(sumTotal <= 17){
             return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-risco.xhtml";
           /* if((this.getUser().getGender()=='F' && evaluation.getWeekEvaluation1() > 1) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation1() > 2) || (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation2() > 5) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation2() > 10))
                return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-baixo-risco.xhtml";
            else if((this.getUser().getGender()=='F' && evaluation.getWeekEvaluation1() < 1) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation1() < 2) || (this.getUser().getGender()=='F' && evaluation.getWeekEvaluation2() < 5) || (this.getUser().getGender()=='M' && evaluation.getWeekEvaluation2() < 10)){
                    if(this.getUser().getGender() == 'M' && age <= 65)
                        return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml";
                    else if((this.getUser().getGender() == 'M' && age > 65) || this.getUser().getGender() == 'F' && age > 65)
                        return "quanto-voce-bebe-reconmendar-limites-mulheres-e-homens-com-mais-de-65-anos.xhtml";*/
        }else if(sumTotal>=18 && sumTotal <= 25){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-risco.xhtml";
        }else if(sumTotal>=26 && sumTotal <= 29){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-uso-nocivo.xhtml";
        }else if(sumTotal >= 30 && sumTotal <= 50){
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-sim-dependencia.xhtml";
        }
        return "";
    }
    
    public String audit31() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());
        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (this.getEvaluation().getSum() <= 3) {
           return this.drinkingLimits();
        } else {
            if (loggedUser()) {
                return "quanto-voce-bebe-sim-beber-uso-audit-7.xhtml?faces-redirect=true";
            }

            return "cadastrar-nova-conta.xhtml?faces-redirect=true";
        }
    }

    public String drinkingLimits() {
        if (this.getEvaluation().getUser().getGender() == 'M' && this.getEvaluation().getUser().getAge() <= 65) {
            return "quanto-voce-bebe-recomendar-limites-homem-ate-65-anos.xhtml?faces-redirect=true";
        } else {
            return "quanto-voce-bebe-recomendar-limites-mulheres-e-homens-com-mais-65-anos.xhtml?faces-redirect=true";
        }
    }

    public void sendEmail() {
        this.evaluation.setYearEmail(true);
        try {
            this.userDAO.update(this.getUser(), this.getEntityManager());
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        String message = "Solicitação enviada com sucesso!";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
        Logger.getLogger(BaseFormController.class.getName()).log(Level.INFO, message);
    }

    public String audit71() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-doenca.xhtml?faces-redirect=true";
    }

    public String screenForAlcoholUseDisorders() {
        try {
            this.daoBase.update(this.getEvaluation(), this.getEntityManager());

        } catch (SQLException ex) {
            Logger.getLogger(EvaluationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (evaluation.screen()) {
            return "quanto-voce-bebe-sim-beber-uso-sintomas-alcool-doenca-sim-cuidado.xhtml?faces-redirect=true";
        } else {
            return this.drinkingLimits();
        }
    }
    
    public String continueEvaluation(){
        if (isContinueEvaluation()){
            return "preparando-pros-cons.xhtml?faces-redirect=true";
        }else{
            return "index.xhtml?faces-redirect=true";
        }
    }

    /*public String getGender() {
    if (loggedUser()) {
    return String.valueOf(this.getUser().getGender());
    }
    return gender;
    }*/
    
    public Integer getGender() {
        return gender;
    }
    

    public void setGender(Integer gender) {
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

    public boolean sameDate(Calendar firstDate, Calendar secondDate) {
        return firstDate.get(Calendar.DAY_OF_MONTH) == secondDate.get(Calendar.DAY_OF_MONTH)
                && firstDate.get(Calendar.MONTH) == secondDate.get(Calendar.MONTH)
                && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR);
    }

    public boolean isContinueEvaluation() {
        return continueEvaluation;
    }

    public void setContinueEvaluation(boolean continueEvaluation) {
        this.continueEvaluation = continueEvaluation;
    }
    
    public boolean isWoman(){
        return gender == 1;
        //return getGender().equals('F');
        //eturn this.getUser().getGender()=='F';
    }

    public Integer getPregnant() {
        return pregnant;
    }

    public void setPregnant(Integer pregnant) {
        this.pregnant = pregnant;
    }
    
    
    
    

}
