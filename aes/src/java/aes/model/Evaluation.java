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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private User user;
    
       @Override
    public String toString() {
        return this.id + ", " + this.user.getId() + ", " + 
                new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.getTime()) + ", " + this.drink;
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
    
    public int getWeekTotal(){
        return sunday + monday + tuesday + wednesday + thursday + friday + saturday;
    }
    
    public int getAudit3Sum(){
        return audit1 + audit2 + audit3;
    } 
    
    public int getAuditFullSum(){
        return audit1 + audit2 + audit3 + audit4 + audit5 + audit6 + audit7 + audit8 + audit9 + audit10;
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
    
    

}
