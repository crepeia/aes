package aes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
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
import javax.xml.bind.annotation.XmlRootElement;
 
/**
 *
 * @author luansb
 */
@Entity
@Table(name = "tb_ready_text_interaction")
@XmlRootElement
public class ReadyTextInteraction implements Serializable {
    private static final long serialVersionUID = 1L;
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultor_id", nullable = false)
    private User consultor;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_consultor_id", nullable = false)
    private Message messageConsultor;
 
    // Tag da folha de tradução usada (ex.: "readyText.saudacoes.1")
    @Column(name = "ready_text_tag", nullable = false)
    private String readyTextTag;
 
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_used", nullable = false)
    private Date dateUsed;
 
    public ReadyTextInteraction() {
    }
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public User getConsultor() {
        return consultor;
    }
 
    public void setConsultor(User consultor) {
        this.consultor = consultor;
    }
 
    public Message getMessageConsultor() {
        return messageConsultor;
    }
 
    public void setMessageConsultor(Message messageConsultor) {
        this.messageConsultor = messageConsultor;
    }
 
    public String getReadyTextTag() {
        return readyTextTag;
    }
 
    public void setReadyTextTag(String readyTextTag) {
        this.readyTextTag = readyTextTag;
    }
 
    public Date getDateUsed() {
        return dateUsed;
    }
 
    public void setDateUsed(Date dateUsed) {
        this.dateUsed = dateUsed;
    }
}