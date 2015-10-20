/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author thiagorizuti
 */
@Entity
@Table(name = "tb_evaluation")
public class Evaluation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;
    @Column(name = "drink")
    private Boolean drink;

    @Column(name = "year_email")
    private Boolean yearEmail;
    @Column(name = "year_email_date")
    private Calendar yearEmailDate;

    @Column(name = "audit_1")
    private Integer audit1;
    @Column(name = "audit_2")
    private Integer audit2;
    @Column(name = "audit_3")
    private Integer audit3;
    @Column(name = "monday")
    private Integer monday;
    @Column(name = "tuesday")
    private Integer tuesday;
    @Column(name = "wednesday")
    private Integer wednesday;
    @Column(name = "thursday")
    private Integer thursday;
    @Column(name = "friday")
    private Integer friday;
    @Column(name = "saturday")
    private Integer saturday;
    @Column(name = "sunday")
    private Integer sunday;

    @Column(name = "audit_4")
    public Integer audit4;
    @Column(name = "audit_5")
    private Integer audit5;
    @Column(name = "audit_6")
    private Integer audit6;
    @Column(name = "audit_7")
    private Integer audit7;
    @Column(name = "audit_8")
    private Integer audit8;
    @Column(name = "audit_9")
    private Integer audit9;
    @Column(name = "audit_10")
    private Integer audit10;

    @Column(name = "pros_reasons_1")
    private Integer prosReasons1;
    @Column(name = "pros_reasons_2")
    private Integer prosReasons2;
    @Column(name = "pros_reasons_3")
    private Integer prosReasons3;
    @Column(name = "pros_reasons_4")
    private Integer prosReasons4;
    @Column(name = "pros_reasons_5")
    private Integer prosReasons5;
    @Column(name = "pros_reasons_6")
    private Integer prosReasons6;
    @Column(name = "pros_reasons_7")
    private Integer prosReasons7;
    @Column(name = "pros_reasons_8")
    private Integer prosReasons8;
    @Column(name = "pros_1")
    private String pros1;
    @Column(name = "pros_2")
    private String pros2;
    @Column(name = "pros_3")
    private String pros3;
    @Column(name = "cons_1")
    private String cons1;
    @Column(name = "cons_2")
    private String cons2;
    @Column(name = "cons_3")
    private String cons3;

    @Column(name = "ready")
    private Boolean ready;
    @Column(name = "back_plan")
    private Boolean backPlan;
    @Column(name = "quit")
    private Boolean quit;

    @Column(name = "dependence")
    private Boolean dependence;

    @Column(name = "data_comecar_plano")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataComecarPlano;
    @Column(name = "razoes_plano")
    private String razoesPlano;
    @Column(name = "estrategias_plano")
    private String estrategiasPlano;
    @Column(name = "pessoas_plano")
    private String pessoasPlano;
    @Column(name = "sinais_sucesso_plano")
    private String sinaisSucessoPlano;
    @Column(name = "possiveis_dificuldades_plano")
    private String possiveisDificuladesPlano;

    @Column(name = "dose_padrao_diaria")
    private Integer dosePadraoDiaria;
    @Column(name = "dose_padrao_semanal")
    private Integer dosePadraoSemanal;

    @Column(name = "starting_day")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startingDay;
    
    @Column(name = "drinks_day1")
    private Integer drinksDay1;
    @Column(name = "contextDay1")
    private String contextDay1;
    @Column (name = "consequencesDay1")
    private String consequencesDay1;
    
    @Column(name = "drinks_day2")
    private Integer drinksDay2;
    @Column(name = "contextDay2")
    private String contextDay2;
    @Column (name = "consequencesDay2")
    private String consequencesDay2;
    
    @Column(name = "drinks_day3")
    private Integer drinksDay3;
    @Column(name = "contextDay3")
    private String contextDay3;
    @Column (name = "consequencesDay3")
    private String consequencesDay3;
    
    @Column(name = "drinks_day4")
    private Integer drinksDay4;
    @Column(name = "contextDay4")
    private String contextDay4;
    @Column (name = "consequencesDay4")
    private String consequencesDay4;
    
    @Column(name = "drinks_day5")
    private Integer drinksDay5;
    @Column(name = "contextDay5")
    private String contextDay5;
    @Column (name = "consequencesDay5")
    private String consequencesDay5;
    
    @Column(name = "drinks_day6")
    private Integer drinksDay6;
    @Column(name = "contextDay6")
    private String contextDay6;
    @Column (name = "consequencesDay6")
    private String consequencesDay6;
    
    @Column(name = "drinks_day7")
    private Integer drinksDay7;
    @Column(name = "contextDay7")
    private String contextDay7;
    @Column (name = "consequencesDay7")
    private String consequencesDay7;
    
   

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private User user;

    @Override
    public String toString() {
        return this.id + ", " + this.user.getId() + ", "
                + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.getTime()) + ", " + this.drink;
    }

    public int getDrinkingDays() {
        int drinkingDays = 0;
        if (sunday != 0) {
            drinkingDays++;
        }
        if (monday != 0) {
            drinkingDays++;
        }
        if (tuesday != 0) {
            drinkingDays++;
        }
        if (wednesday != 0) {
            drinkingDays++;
        }
        if (thursday != 0) {
            drinkingDays++;
        }
        if (friday != 0) {
            drinkingDays++;
        }
        if (saturday != 0) {
            drinkingDays++;
        }

        return drinkingDays;
    }

    public int getWeekTotal() {
        return sunday + monday + tuesday + wednesday + thursday + friday + saturday;
    }

    public int getAudit3Sum() {
        return audit1 + audit2 + audit3;
    }

    public int getAuditFullSum() {
        return audit1 + audit2 + audit3 + audit4 + audit5 + audit6 + audit7 + audit8 + audit9 + audit10;
    }
    
    public int getDrinksSum(){
        return  (drinksDay1 != null ? drinksDay1 : 0) + (drinksDay2 != null ? drinksDay2 : 0) +
                (drinksDay3 != null ? drinksDay3 : 0) + (drinksDay4 != null ? drinksDay4 : 0) +
                (drinksDay5 != null ? drinksDay5 : 0) + (drinksDay6 != null ? drinksDay6 : 0) +
                (drinksDay7 != null ? drinksDay7 : 0);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getDrink() {
        return drink;
    }

    public void setDrink(Boolean drink) {
        this.drink = drink;
    }

    public Integer getAudit1() {
        return audit1;
    }

    public void setAudit1(Integer audit1) {
        this.audit1 = audit1;
    }

    public Integer getAudit2() {
        return audit2;
    }

    public void setAudit2(Integer audit2) {
        this.audit2 = audit2;
    }

    public Integer getAudit3() {
        return audit3;
    }

    public void setAudit3(Integer audit3) {
        this.audit3 = audit3;
    }

    public Integer getMonday() {
        return monday;
    }

    public void setMonday(Integer monday) {
        this.monday = monday;
    }

    public Integer getTuesday() {
        return tuesday;
    }

    public void setTuesday(Integer tuesday) {
        this.tuesday = tuesday;
    }

    public Integer getWednesday() {
        return wednesday;
    }

    public void setWednesday(Integer wednesday) {
        this.wednesday = wednesday;
    }

    public Integer getThursday() {
        return thursday;
    }

    public void setThursday(Integer thursday) {
        this.thursday = thursday;
    }

    public Integer getFriday() {
        return friday;
    }

    public void setFriday(Integer friday) {
        this.friday = friday;
    }

    public Integer getSaturday() {
        return saturday;
    }

    public void setSaturday(Integer saturday) {
        this.saturday = saturday;
    }

    public Integer getSunday() {
        return sunday;
    }

    public void setSunday(Integer sunday) {
        this.sunday = sunday;
    }

    public Boolean getYearEmail() {
        return yearEmail;
    }

    public void setYearEmail(Boolean yearEmail) {
        this.yearEmail = yearEmail;
    }

    public Calendar getYearEmailDate() {
        return yearEmailDate;
    }

    public void setYearEmailDate(Calendar yearEmailDate) {
        this.yearEmailDate = yearEmailDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Integer getAudit4() {
        return audit4;
    }

    public void setAudit4(Integer audit4) {
        this.audit4 = audit4;
    }

    public Integer getAudit5() {
        return audit5;
    }

    public void setAudit5(Integer audit5) {
        this.audit5 = audit5;
    }

    public Integer getAudit6() {
        return audit6;
    }

    public void setAudit6(Integer audit6) {
        this.audit6 = audit6;
    }

    public Integer getAudit7() {
        return audit7;
    }

    public void setAudit7(Integer audit7) {
        this.audit7 = audit7;
    }

    public Integer getAudit8() {
        return audit8;
    }

    public void setAudit8(Integer audit8) {
        this.audit8 = audit8;
    }

    public Integer getAudit9() {
        return audit9;
    }

    public void setAudit9(Integer audit9) {
        this.audit9 = audit9;
    }

    public Integer getAudit10() {
        return audit10;
    }

    public void setAudit10(Integer audit10) {
        this.audit10 = audit10;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public Boolean getBackPlan() {
        return backPlan;
    }

    public void setBackPlan(Boolean backPlan) {
        this.backPlan = backPlan;
    }

    public Boolean getDependence() {
        return dependence;
    }

    public Boolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean quit) {
        this.quit = quit;
    }

    public void setDependence(Boolean dependence) {
        this.dependence = dependence;
    }

    public Date getDataComecarPlano() {
        return dataComecarPlano;
    }

    public void setDataComecarPlano(Date dataComecarPlano) {
        this.dataComecarPlano = dataComecarPlano;
    }

    public String getRazoesPlano() {
        return razoesPlano;
    }

    public void setRazoesPlano(String razoesPlano) {
        this.razoesPlano = razoesPlano;
    }

    public String getEstrategiasPlano() {
        return estrategiasPlano;
    }

    public void setEstrategiasPlano(String estrategiasPlano) {
        this.estrategiasPlano = estrategiasPlano;
    }

    public String getPessoasPlano() {
        return pessoasPlano;
    }

    public void setPessoasPlano(String pessoasPlano) {
        this.pessoasPlano = pessoasPlano;
    }

    public String getSinaisSucessoPlano() {
        return sinaisSucessoPlano;
    }

    public void setSinaisSucessoPlano(String sinaisSucessoPlano) {
        this.sinaisSucessoPlano = sinaisSucessoPlano;
    }

    public String getPossiveisDificuladesPlano() {
        return possiveisDificuladesPlano;
    }

    public void setPossiveisDificuladesPlano(String possiveisDificuladesPlano) {
        this.possiveisDificuladesPlano = possiveisDificuladesPlano;
    }

    public Integer getDosePadraoDiaria() {
        return dosePadraoDiaria;
    }

    public void setDosePadraoDiaria(Integer dosePadraoDiaria) {
        this.dosePadraoDiaria = dosePadraoDiaria;
    }

    public Integer getDosePadraoSemanal() {
        return dosePadraoSemanal;
    }

    public void setDosePadraoSemanal(Integer dosePadraoSemanal) {
        this.dosePadraoSemanal = dosePadraoSemanal;
    }

    public Integer getProsReasons1() {
        return prosReasons1;
    }

    public void setProsReasons1(Integer prosReasons1) {
        this.prosReasons1 = prosReasons1;
    }

    public Integer getProsReasons2() {
        return prosReasons2;
    }

    public void setProsReasons2(Integer prosReasons2) {
        this.prosReasons2 = prosReasons2;
    }

    public Integer getProsReasons3() {
        return prosReasons3;
    }

    public void setProsReasons3(Integer prosReasons3) {
        this.prosReasons3 = prosReasons3;
    }

    public String getPros1() {
        return pros1;
    }

    public void setPros1(String pros1) {
        this.pros1 = pros1;
    }

    public String getPros2() {
        return pros2;
    }

    public void setPros2(String pros2) {
        this.pros2 = pros2;
    }

    public String getPros3() {
        return pros3;
    }

    public void setPros3(String pros3) {
        this.pros3 = pros3;
    }

    public String getCons1() {
        return cons1;
    }

    public void setCons1(String cons1) {
        this.cons1 = cons1;
    }

    public String getCons2() {
        return cons2;
    }

    public void setCons2(String cons2) {
        this.cons2 = cons2;
    }

    public String getCons3() {
        return cons3;
    }

    public void setCons3(String cons3) {
        this.cons3 = cons3;
    }

    public Integer getProsReasons4() {
        return prosReasons4;
    }

    public void setProsReasons4(Integer prosReasons4) {
        this.prosReasons4 = prosReasons4;
    }

    public Integer getProsReasons5() {
        return prosReasons5;
    }

    public void setProsReasons5(Integer prosReasons5) {
        this.prosReasons5 = prosReasons5;
    }

    public Integer getProsReasons6() {
        return prosReasons6;
    }

    public void setProsReasons6(Integer prosReasons6) {
        this.prosReasons6 = prosReasons6;
    }

    public Integer getProsReasons7() {
        return prosReasons7;
    }

    public void setProsReasons7(Integer prosReasons7) {
        this.prosReasons7 = prosReasons7;
    }

    public Integer getProsReasons8() {
        return prosReasons8;
    }

    public void setProsReasons8(Integer prosReasons8) {
        this.prosReasons8 = prosReasons8;
    }

    public Date getStartingDay() {
        return startingDay;
    }

    public void setStartingDay(Date startingDay) {
        this.startingDay = startingDay;
    }

    public Integer getDrinksDay1() {
        return drinksDay1;
    }

    public void setDrinksDay1(Integer drinksDay1) {
        this.drinksDay1 = drinksDay1;
    }

    public String getContextDay1() {
        return contextDay1;
    }

    public void setContextDay1(String contextDay1) {
        this.contextDay1 = contextDay1;
    }

    public String getConsequencesDay1() {
        return consequencesDay1;
    }

    public void setConsequencesDay1(String consequencesDay1) {
        this.consequencesDay1 = consequencesDay1;
    }

    public Integer getDrinksDay2() {
        return drinksDay2;
    }

    public void setDrinksDay2(Integer drinksDay2) {
        this.drinksDay2 = drinksDay2;
    }

    public String getContextDay2() {
        return contextDay2;
    }

    public void setContextDay2(String contextDay2) {
        this.contextDay2 = contextDay2;
    }

    public String getConsequencesDay2() {
        return consequencesDay2;
    }

    public void setConsequencesDay2(String consequencesDay2) {
        this.consequencesDay2 = consequencesDay2;
    }

    public Integer getDrinksDay3() {
        return drinksDay3;
    }

    public void setDrinksDay3(Integer drinksDay3) {
        this.drinksDay3 = drinksDay3;
    }

    public String getContextDay3() {
        return contextDay3;
    }

    public void setContextDay3(String contextDay3) {
        this.contextDay3 = contextDay3;
    }

    public String getConsequencesDay3() {
        return consequencesDay3;
    }

    public void setConsequencesDay3(String consequencesDay3) {
        this.consequencesDay3 = consequencesDay3;
    }

    public Integer getDrinksDay4() {
        return drinksDay4;
    }

    public void setDrinksDay4(Integer drinksDay4) {
        this.drinksDay4 = drinksDay4;
    }

    public String getContextDay4() {
        return contextDay4;
    }

    public void setContextDay4(String contextDay4) {
        this.contextDay4 = contextDay4;
    }

    public String getConsequencesDay4() {
        return consequencesDay4;
    }

    public void setConsequencesDay4(String consequencesDay4) {
        this.consequencesDay4 = consequencesDay4;
    }

    public Integer getDrinksDay5() {
        return drinksDay5;
    }

    public void setDrinksDay5(Integer drinksDay5) {
        this.drinksDay5 = drinksDay5;
    }

    public String getContextDay5() {
        return contextDay5;
    }

    public void setContextDay5(String contextDay5) {
        this.contextDay5 = contextDay5;
    }

    public String getConsequencesDay5() {
        return consequencesDay5;
    }

    public void setConsequencesDay5(String consequencesDay5) {
        this.consequencesDay5 = consequencesDay5;
    }

    public Integer getDrinksDay6() {
        return drinksDay6;
    }

    public void setDrinksDay6(Integer drinksDay6) {
        this.drinksDay6 = drinksDay6;
    }

    public String getContextDay6() {
        return contextDay6;
    }

    public void setContextDay6(String contextDay6) {
        this.contextDay6 = contextDay6;
    }

    public String getConsequencesDay6() {
        return consequencesDay6;
    }

    public void setConsequencesDay6(String consequencesDay6) {
        this.consequencesDay6 = consequencesDay6;
    }

    public Integer getDrinksDay7() {
        return drinksDay7;
    }

    public void setDrinksDay7(Integer drinksDay7) {
        this.drinksDay7 = drinksDay7;
    }

    public String getContextDay7() {
        return contextDay7;
    }

    public void setContextDay7(String contextDay7) {
        this.contextDay7 = contextDay7;
    }

    public String getConsequencesDay7() {
        return consequencesDay7;
    }

    public void setConsequencesDay7(String consequencesDay7) {
        this.consequencesDay7 = consequencesDay7;
    }

 
    
    

}
