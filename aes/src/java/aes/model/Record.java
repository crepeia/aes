package aes.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "tb_record")
@XmlRootElement
public class Record implements Serializable{
       
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "daily_goal")
    @ColumnDefault("0")
    private float dailyGoal;
    @Column(name = "weekly_goal")
    @ColumnDefault("0")
    private float weeklyGoal;
    
    @JsonBackReference
    @OneToOne
    private User user;
    
    @OneToMany(fetch = FetchType.LAZY)
    private List<DailyLog> dailyLogs;
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(float dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public float getWeeklyGoal() {
        return weeklyGoal;
    }

    public void setWeeklyGoal(float weeklyGoal) {
        this.weeklyGoal = weeklyGoal;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @JsonIgnore
    @XmlTransient
    public List<DailyLog> getDailyLogs() {
        return dailyLogs;
    }

    public void setDailyLogs(List<DailyLog> dailyLogs) {
        this.dailyLogs = dailyLogs;
    }
     
    
}
