package aes.model;

import com.sun.xml.ws.binding.FeatureListUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table(name = "tb_evaluation")
@XmlRootElement
public class Evaluation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created")
    private Date dateCreated;

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
    private Integer audit4;
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
    @Column(name = "go_back")
    private Boolean goBack;
    @Column(name = "dependence_continue")
    private Boolean dependenceContinue;

    @Column(name = "quit")
    private Boolean quit;

    @Temporal(javax.persistence.TemporalType.DATE)
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
    @Column(name = "tips_frequency")
    private Integer tipsFrequency;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public String toString() {
        return this.id + ", " + this.user.getId() + ", "
                + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(dateCreated);
    }
    
    public boolean audit3LimitExceeded(){
        return getAudit3Sum() > 6;
    }
    
    
    public boolean dayLimitExceeded() {
        int limit =  getUser().isFemale() ? 1 : 2;
        if (sunday != null && sunday > limit){
            return true;
        }
        if (monday != null && monday > limit) {
            return true;
        }
        if (tuesday != null && tuesday > limit) {
            return true;
        }
        if (wednesday != null && wednesday > limit) {
            return true;
        }
        if (thursday != null && thursday > limit) {
            return true;
        }
        if (friday != null && friday > limit) {
            return true;
        }
        if (saturday != null && saturday > limit) {
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean weekLimitExceeded(){
        int limit =  getUser().isFemale() ? 5 : 10;
        return getWeekTotal() > limit;
    }
    
    @JsonIgnore
    public int getWeekTotal() {
        return sunday + monday + tuesday + wednesday + thursday + friday + saturday;
    }
    @JsonIgnore
    public int getAudit3Sum() {
        return audit1 + audit2 + audit3;
    }
    @JsonIgnore
    public int getAuditFullSum() {
        return audit1 + audit2 + audit3 + audit4 + audit5 + audit6 + audit7 + audit8 + audit9 + audit10;
    }
    @JsonIgnore
    public String[] getPlanContent() {
        String content[] = new String[6];
        content[0] = new SimpleDateFormat("dd/MM/yyyy").format(dataComecarPlano);
        if(razoesPlano != null)
        content[1] = razoesPlano;
        else
            content[1]= "";
        content[2] = estrategiasPlano;
        content[3] = pessoasPlano;
        content[4] = sinaisSucessoPlano;
        content[5] = possiveisDificuladesPlano;
        return content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public void setDateCreated(long time){
       dateCreated.setTime(time);
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

    public Boolean getGoBack() {
        return goBack;
    }

    public void setGoBack(Boolean goBack) {
        this.goBack = goBack;
    }

    public Boolean getDependenceContinue() {
        return dependenceContinue;
    }

    public void setDependenceContinue(Boolean dependenceContinue) {
        this.dependenceContinue = dependenceContinue;
    }

    public Boolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean quit) {
        this.quit = quit;
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

    public Integer getTipsFrequency() {
        return tipsFrequency;
    }

    public void setTipsFrequency(Integer tipsFrequency) {
        this.tipsFrequency = tipsFrequency;
    }

}
