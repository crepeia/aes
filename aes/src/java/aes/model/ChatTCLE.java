package aes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author luansb
 */
@Entity
@Table(name = "tb_chat_tcle")
@XmlRootElement
public class ChatTCLE implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "agreed", nullable = false)
    private Boolean agreed;
    
    @Column(name = "tcle_version", nullable = false, length = 10)
    private String tcleVersion;

    @Column(name = "decision_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date decisionDate;
    
    public ChatTCLE() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getAgreed() {
        return agreed;
    }

    public void setAgreed(Boolean agreed) {
        this.agreed = agreed;
    }

    public String getTcleVersion() {
        return tcleVersion;
    }

    public void setTcleVersion(String tcleVersion) {
        this.tcleVersion = tcleVersion;
    }

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
    }
}
