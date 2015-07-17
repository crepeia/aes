/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author hedersb
 */
@Entity
@Table(name = "tb_user")
public class User {

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

    @OneToMany(fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;
    
    public Integer getAge() {
        Calendar today = Calendar.getInstance(); 
        
        if (today.get(Calendar.MONTH) < this.getBirth().get(Calendar.MONTH)) {
            return (today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR) - 1);
            
        } else if (today.get(Calendar.MONTH) == this.getBirth().get(Calendar.MONTH) && 
                today.get(Calendar.DAY_OF_MONTH) < this.getBirth().get(Calendar.DAY_OF_MONTH)) {
            return (today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR) - 1);
            
        }else 
            return today.get(Calendar.YEAR) - this.getBirth().get(Calendar.YEAR);

    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the password
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return this.id + ", " + this.name + ", " + this.email + ", " + new String(this.password);
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the birth
     */
    public Calendar getBirth() {
        return birth;
    }

    /**
     * @param birth the birth to set
     */
    public void setBirth(Calendar birth) {
        this.birth = birth;
    }

    /**
     * @return the gender
     */
    public char getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(char gender) {
        this.gender = gender;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }
    
    public void setBirth(int year,int month,int day){
        if(this.birth == null){
            this.birth = Calendar.getInstance();
        }
        this.birth.set(year,month,day);
    }

    /**
     * @return the receiveEmails
     */
    public boolean isReceiveEmails() {
	return receiveEmails;
    }

    /**
     * @param receiveEmails the receiveEmails to set
     */
    public void setReceiveEmails(boolean receiveEmails) {
	this.receiveEmails = receiveEmails;
    }

    /**
     * @return the authorizeData
     */
    public boolean isAuthorizeData() {
	return authorizeData;
    }

    /**
     * @param authorizeData the authorizeData to set
     */
    public void setAuthorizeData(boolean authorizeData) {
	this.authorizeData = authorizeData;
    }
    



}
