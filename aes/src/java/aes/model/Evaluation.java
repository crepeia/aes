/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
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
    @Column (name = "year_email_date")
    private Calendar yearEmailDate;

    @Column(name = "audit_1")
    private Integer audit1;
    @Column(name = "audit_2")
    private Integer audit2;
    @Column(name = "audit_3")
    private Integer audit3;
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
    
    @Column(name = "monday")
    private int monday;
    @Column(name = "tuesday")
    private int tuesday;
    @Column(name = "wednesday")
    private int wednesday;
    @Column(name = "thursday")
    private int thursday;
    @Column(name = "friday")
    private int friday;
    @Column(name = "saturday")
    private int saturday;
    @Column(name = "sunday")
    private int sunday;
       
    @Column(name = "preparado")
    private Integer preparado;
    
    @Column(name = "back_plan")
    private Integer backPlan;
    
    @Column(name = "dependencia")
    private Integer dependencia;
    
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
    
    @ManyToOne(cascade = {CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    private User user;

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

    public int getMonday() {
        return monday;
    }

    public void setMonday(int monday) {
        this.monday = monday;
    }

    public int getTuesday() {
        return tuesday;
    }

    public void setTuesday(int tuesday) {
        this.tuesday = tuesday;
    }

    public int getWednesday() {
        return wednesday;
    }

    public void setWednesday(int wednesday) {
        this.wednesday = wednesday;
    }

    public int getThursday() {
        return thursday;
    }

    public void setThursday(int thursday) {
        this.thursday = thursday;
    }

    public int getFriday() {
        return friday;
    }

    public void setFriday(int friday) {
        this.friday = friday;
    }

    public int getSaturday() {
        return saturday;
    }

    public void setSaturday(int saturday) {
        this.saturday = saturday;
    }

    public int getSunday() {
        return sunday;
    }

    public void setSunday(int sunday) {
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

    public Integer getPreparado() {
        return preparado;
    }

    public void setPreparado(Integer preparado) {
        this.preparado = preparado;
    }

    public Integer getBackPlan() {
        return backPlan;
    }

    public void setBackPlan(Integer backPlan) {
        this.backPlan = backPlan;
    }

    public Integer getDependencia() {
        return dependencia;
    }

    public void setDependencia(Integer dependencia) {
        this.dependencia = dependencia;
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
       
    
 
}

