/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author hedersb
 */
@Entity
@Table(name = "tb_user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "name", length = 100)
    private String name;
    @Column(name = "email", length = 50)
    private String email;
    @Column(name = "password", length = 16)
    private byte[] password;
    @Column(name = "birth")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar birth;
    @Column(name = "gender")
    private char gender;
    @Column(name = "receive_emails") 
    private boolean receiveEmails;
    @Column(name = "authorize_data")
    private boolean authorizeData;
    @Column(name = "pregnant")
    private Boolean pregnant;
    @Column(name = "language")
    private String language;
    
    @Column(name = "recover_code")
    private Integer recoverCode;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy="user", cascade=CascadeType.ALL)
    private List<Evaluation> evaluations;
    
    @OneToOne(cascade=CascadeType.ALL, mappedBy = "user")
    private KeepResults keepResults;
      
    @Override
    public String toString() {
        return this.id + ", " + this.name + ", " + this.email;
    }
    
    public int getAge() {
        Calendar today = Calendar.getInstance(); 
        
        if (today.get(Calendar.MONTH) < this.getBirth().get(Calendar.MONTH)) {
            return (today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR) - 1);
            
        } else if (today.get(Calendar.MONTH) == this.getBirth().get(Calendar.MONTH) && 
                today.get(Calendar.DAY_OF_MONTH) < this.getBirth().get(Calendar.DAY_OF_MONTH)) {
            return (today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR) - 1);
            
        }else 
            return today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR);

    }
   
    public void setBirth(int year,int month,int day){     
        birth = Calendar.getInstance();
        birth.set(year,month,day);
    }
    
    public boolean isUnderage(){
        return getAge() < 18;
          
    }
    
    public boolean isFemale(){
        return gender == 'F';
    }
    
    public boolean isMale(){
        return gender == 'M';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public Calendar getBirth() {
        return birth;
    }

    public void setBirth(Calendar birth) {
        this.birth = birth;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public boolean isReceiveEmails() {
        return receiveEmails;
    }

    public void setReceiveEmails(boolean receiveEmails) {
        this.receiveEmails = receiveEmails;
    }

    public boolean isAuthorizeData() {
        return authorizeData;
    }

    public void setAuthorizeData(boolean authorizeData) {
        this.authorizeData = authorizeData;
    }

    public Boolean getPregnant() {
        if(pregnant == null){
            return false;
        }
        return pregnant;
    }

    public void setPregnant(Boolean pregnant) {
        this.pregnant = pregnant;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public KeepResults getKeepResults() {
        return keepResults;
    }

    public void setKeepResults(KeepResults keepResults) {
        this.keepResults = keepResults;
    }

    public Integer getRecoverCode() {
        return recoverCode;
    }

    public void setRecoverCode(Integer recoverCode) {
        this.recoverCode = recoverCode;
    }

    
    
}