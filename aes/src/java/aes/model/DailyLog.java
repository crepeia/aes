package aes.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "tb_daily_log")
public class DailyLog implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "logDate")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date logDate;
    @Column(name = "drinks")
    private Integer drinks;
    @Column(name = "context")
    private String context;
    @Column(name = "consequences")
    private String consequences;
    
    @ManyToOne
    private Record record;
    
    @Override
    public String toString(){
        return id + ", " + logDate + ", " + drinks + ", " + context + ", " + consequences;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }
      
    public Integer getDrinks() {
        if(drinks == null){
            return 0;
        }
        return drinks;
    }

    public void setDrinks(Integer drinks) {
        this.drinks = drinks;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

   
}
