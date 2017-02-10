package aes.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
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
    @Column(name = "birth_date")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date birthDate;
    @Column(name = "gender")
    private char gender;
    @Column(name = "receive_emails")
    private boolean receiveEmails;
    @Column(name = "authorize_data")
    private boolean authorizeData;
    @Column(name = "sign_up_date")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date signUpDate;
    @Column(name = "pregnant")
    private Boolean pregnant;
    @Column(name = "drink")
    private Boolean drink;
    @Column(name = "prefered_language")
    private String preferedLanguage;
    @Column(name = "recover_code")
    private Integer recoverCode;
    @Column(name = "phone")
    private String phone;
    @Column(name = "tips_frequency")
    private Integer tipsFrequency;
    @Column(name = "education")
    private Integer education;  
    @Column(name = "employed")
    private Boolean employed;
    @Column(name = "know_website")
    private Integer knowWebsite;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private List<Evaluation> evaluations;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private Record record;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Contact> contacts;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy ="user")
    private List<Rating> ratings;

     
    @OneToMany(fetch = FetchType.LAZY, mappedBy ="user")
    private List<Satisfaction> satisfactions;
    
     @OneToMany(fetch = FetchType.LAZY, mappedBy ="user")
    private List<FollowUp> followUps;
     
    @Column(name = "dt_cadastro")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dt_cadastro;
    
    @Override
    public String toString() {
        return this.id + ", " + this.name + ", " + this.email;
    }

    public int getAge() {
        Calendar today = Calendar.getInstance();
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH)) {
            return (today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR) - 1);

        } else if (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH)) {
            return (today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR) - 1);

        } else {
            return today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
        }

    }

    public void setBirth(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        birthDate = cal.getTime();
    }
    
    public void setBirth(long time){
        birthDate.setTime(time);
    }
    public boolean isUnderage() {
        return getAge() < 18;

    }

    public boolean isFemale() {
        return gender == 'F';
    }

    public boolean isMale() {
        return gender == 'M';
    }
    
    public String getHashedId(){
        return String.valueOf(id*1357);
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
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
        if (pregnant == null) {
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

    public String getPreferedLanguage() {
        return preferedLanguage;
    }

    public void setPreferedLanguage(String preferedLanguage) {
        this.preferedLanguage = preferedLanguage;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public Integer getRecoverCode() {
        return recoverCode;
    }

    public void setRecoverCode(Integer recoverCode) {
        this.recoverCode = recoverCode;
    }

    public Boolean getDrink() {
        return drink;
    }

    public void setDrink(Boolean drink) {
        this.drink = drink;
    }

    public Date getSignUpDate() {
        return signUpDate;
    }

    public void setSignUpDate(Date signUpDate) {
        this.signUpDate = signUpDate;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Satisfaction> getSatisfactions() {
        return satisfactions;
    }

    public void setSatisfactions(List<Satisfaction> satisfactions) {
        this.satisfactions = satisfactions;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public List<FollowUp> getFollowUps() {
        return followUps;
    }

    public void setFollowUps(List<FollowUp> followUps) {
        this.followUps = followUps;
    }

    public Integer getTipsFrequency() {
        return tipsFrequency;
    }

    public void setTipsFrequency(Integer tipsFrequency) {
        this.tipsFrequency = tipsFrequency;
    }

    public Integer getEducation() {
        return education;
    }

    public void setEducation(Integer education) {
        this.education = education;
    }

    public Boolean getEmployed() {
        return employed;
    }

    public void setEmployed(Boolean employed) {
        this.employed = employed;
    }

    public Integer getKnowWebsite() {
        return knowWebsite;
    }

    public void setKnowWebsite(Integer knowWebsite) {
        this.knowWebsite = knowWebsite;
    }
    
    public Date getDtCadastro() {
        return dt_cadastro;
    }

    /**
     */
    public void setDtCadastro(Date dt) {
        this.dt_cadastro = dt;
    }
    
    
    
    
}
