/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aes.model;

import java.util.Calendar;
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
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;
    
    @Column(name = "drink")
    private Boolean drink;
    
    @Column(name = "heavy_options_1")
    private Integer heavyOptions1;
    @Column(name = "heavy_options_2")
    private Integer heavyOptions2;
    @Column(name = "heavy_options_3")
    private Integer heavyOptions3;
    @Column(name = "heavy_question_1")
    private Integer heavyQuestion1;
    @Column(name = "heavy_question_2")
    private Integer heavyQuestion2;
    @Column(name = "heavy_sum")
    private Integer heavySum;
    
    @Column(name = "dep_question_1")
    private Integer depQuestion1;
    @Column(name = "dep_question_2")
    private Integer depQuestion2;
    @Column(name = "dep_question_3")
    private Integer depQuestion3;
    @Column(name = "dep_question_4")
    private Integer depQuestion4;
    @Column(name = "dep_question_5")
    private Integer depQuestion5;
    @Column(name = "dep_question_6")
    private Integer depQuestion6;
    @Column(name = "dep_question_7")
    private Integer depQuestion7;
    @Column(name = "dep_question_8")
    private Integer depQuestion8;
    
    @Column(name = "risk_question_1")
    private Integer riskQuestion1;
    @Column(name = "risk_question_2")
    private Integer riskQuestion2;
    @Column(name = "risk_question_3")
    private Integer riskQuestion3;
    @Column(name = "risk_question_4")
    private Integer riskQuestion4;
    @Column(name = "risk_question_5")
    private Integer riskQuestion5;
    @Column(name = "risk_question_6")
    private Integer riskQuestion6;
    
    @ManyToOne
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean isDrink() {
        return drink;
    }

    public void setDrink(Boolean drink) {
        this.drink = drink;
    }

    public Integer getHeavyOptions1() {
        return heavyOptions1;
    }

    public void setHeavyOptions1(Integer heavyOptions1) {
        this.heavyOptions1 = heavyOptions1;
    }

    public Integer getHeavyOptions2() {
        return heavyOptions2;
    }

    public void setHeavyOptions2(Integer heavyOptions2) {
        this.heavyOptions2 = heavyOptions2;
    }

    public Integer getHeavyOptions3() {
        return heavyOptions3;
    }

    public void setHeavyOptions3(Integer heavyOptions3) {
        this.heavyOptions3 = heavyOptions3;
    }

    public Integer getHeavyQuestion1() {
        return heavyQuestion1;
    }

    public void setHeavyQuestion1(Integer heavyQuestion1) {
        this.heavyQuestion1 = heavyQuestion1;
    }

    public Integer getHeavyQuestion2() {
        return heavyQuestion2;
    }

    public void setHeavyQuestion2(Integer heavyQuestion2) {
        this.heavyQuestion2 = heavyQuestion2;
    }

    public Integer getHeavySum() {
        return this.heavyOptions1+this.heavyOptions2+this.heavyOptions3;
    }

    public void setHeavySum(Integer heavySum) {
        this.heavySum = heavySum;
    }

    public Integer getDepQuestion1() {
        return depQuestion1;
    }

    public void setDepQuestion1(Integer depQuestion1) {
        this.depQuestion1 = depQuestion1;
    }

    public Integer getDepQuestion2() {
        return depQuestion2;
    }

    public void setDepQuestion2(Integer depQuestion2) {
        this.depQuestion2 = depQuestion2;
    }

    public Integer getDepQuestion3() {
        return depQuestion3;
    }

    public void setDepQuestion3(Integer depQuestion3) {
        this.depQuestion3 = depQuestion3;
    }

    public Integer getDepQuestion4() {
        return depQuestion4;
    }

    public void setDepQuestion4(Integer depQuestion4) {
        this.depQuestion4 = depQuestion4;
    }

    public Integer getDepQuestion5() {
        return depQuestion5;
    }

    public void setDepQuestion5(Integer depQuestion5) {
        this.depQuestion5 = depQuestion5;
    }

    public Integer getDepQuestion6() {
        return depQuestion6;
    }

    public void setDepQuestion6(Integer depQuestion6) {
        this.depQuestion6 = depQuestion6;
    }

    public Integer getDepQuestion7() {
        return depQuestion7;
    }

    public void setDepQuestion7(Integer depQuestion7) {
        this.depQuestion7 = depQuestion7;
    }

    public Integer getDepQuestion8() {
        return depQuestion8;
    }

    public void setDepQuestion8(Integer depQuestion8) {
        this.depQuestion8 = depQuestion8;
    }

    public Integer getRiskQuestion1() {
        return riskQuestion1;
    }

    public void setRiskQuestion1(Integer riskQuestion1) {
        this.riskQuestion1 = riskQuestion1;
    }

    public Integer getRiskQuestion2() {
        return riskQuestion2;
    }

    public void setRiskQuestion2(Integer riskQuestion2) {
        this.riskQuestion2 = riskQuestion2;
    }

    public Integer getRiskQuestion3() {
        return riskQuestion3;
    }

    public void setRiskQuestion3(Integer riskQuestion3) {
        this.riskQuestion3 = riskQuestion3;
    }

    public Integer getRiskQuestion4() {
        return riskQuestion4;
    }

    public void setRiskQuestion4(Integer riskQuestion4) {
        this.riskQuestion4 = riskQuestion4;
    }

    public Integer getRiskQuestion5() {
        return riskQuestion5;
    }

    public void setRiskQuestion5(Integer riskQuestion5) {
        this.riskQuestion5 = riskQuestion5;
    }

    public Integer getRiskQuestion6() {
        return riskQuestion6;
    }

    public void setRiskQuestion6(Integer riskQuestion6) {
        this.riskQuestion6 = riskQuestion6;
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
    
    
    
    
    
}