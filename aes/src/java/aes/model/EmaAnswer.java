package aes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "tb_ema_answers")
@XmlRootElement
public class EmaAnswer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Block A: Internal State
    @Column(name = "craving")
    private Integer craving;

    @Column(name = "mood_valence")
    private Integer moodValence;

    @Column(name = "mood_arousal")
    private Integer moodArousal;

    @Column(name = "self_efficacy")
    private Integer selfEfficacy;

    @Column(name = "motivation")
    private Integer motivation;

    // Block B: Social and Physical Context
    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "current_location", length = 100)
    private String currentLocation;

    @Column(name = "someone_drinking")
    private Boolean someoneDrinking;

    @Column(name = "availability")
    private Integer availability;

    // Block C: Consumption
    @Column(name = "consumed_alcohol")
    private Boolean consumedAlcohol;

    @Column(name = "number_of_doses", length = 10)
    private String numberOfDoses;

    @Column(name = "period", length = 20)
    private String period;

    @Column(name = "timestamp")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Date timestamp;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getCraving() { return craving; }
    public void setCraving(Integer craving) { this.craving = craving; }

    public Integer getMoodValence() { return moodValence; }
    public void setMoodValence(Integer moodValence) { this.moodValence = moodValence; }

    public Integer getMoodArousal() { return moodArousal; }
    public void setMoodArousal(Integer moodArousal) { this.moodArousal = moodArousal; }

    public Integer getSelfEfficacy() { return selfEfficacy; }
    public void setSelfEfficacy(Integer selfEfficacy) { this.selfEfficacy = selfEfficacy; }

    public Integer getMotivation() { return motivation; }
    public void setMotivation(Integer motivation) { this.motivation = motivation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public Boolean getSomeoneDrinking() { return someoneDrinking; }
    public void setSomeoneDrinking(Boolean someoneDrinking) { this.someoneDrinking = someoneDrinking; }

    public Integer getAvailability() { return availability; }
    public void setAvailability(Integer availability) { this.availability = availability; }

    public Boolean getConsumedAlcohol() { return consumedAlcohol; }
    public void setConsumedAlcohol(Boolean consumedAlcohol) { this.consumedAlcohol = consumedAlcohol; }

    public String getNumberOfDoses() { return numberOfDoses; }
    public void setNumberOfDoses(String numberOfDoses) { this.numberOfDoses = numberOfDoses; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}