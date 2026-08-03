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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author luansb
 */
@Entity
@Table(name = "tb_chatbot_interaction")
@XmlRootElement
public class ChatbotInteraction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Consultor que recebeu as sugestões do chatbot
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultor_id", nullable = false)
    private User consultor;
    
    // Última mensagem do paciente que disparou a geração
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_paciente_id", nullable = false)
    private Message messagePaciente;
    
    // Mensagem efetivamente enviada pelo consultor (null enquanto não enviar / se descartar tudo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_consultor_id")
    private Message messageConsultor;
    
    @Lob
    @Column(name = "response_1", columnDefinition = "TEXT")
    private String response1;

    @Lob
    @Column(name = "response_2", columnDefinition = "TEXT")
    private String response2;

    @Lob
    @Column(name = "response_3", columnDefinition = "TEXT")
    private String response3;

    @Column(name = "consultant_clicked_response_1", nullable = false)
    private Boolean consultantClickedResponse1 = false;

    @Column(name = "consultant_clicked_response_2", nullable = false)
    private Boolean consultantClickedResponse2 = false;

    @Column(name = "consultant_clicked_response_3", nullable = false)
    private Boolean consultantClickedResponse3 = false;
    
    @Column(name = "last_clicked_response_by_consultant")
    private Integer lastClickedResponseByConsultant;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_request", nullable = false)
    private Date date_request;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_response")
    private Date date_response;

    public ChatbotInteraction() {
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

    public Message getMessagePaciente() {
        return messagePaciente;
    }

    public void setMessagePaciente(Message messagePaciente) {
        this.messagePaciente = messagePaciente;
    }

    public Message getMessageConsultor() {
        return messageConsultor;
    }

    public void setMessageConsultor(Message messageConsultor) {
        this.messageConsultor = messageConsultor;
    }

    public String getResponse1() {
        return response1;
    }

    public void setResponse1(String response1) {
        this.response1 = response1;
    }

    public String getResponse2() {
        return response2;
    }

    public void setResponse2(String response2) {
        this.response2 = response2;
    }

    public String getResponse3() {
        return response3;
    }

    public void setResponse3(String response3) {
        this.response3 = response3;
    }

    public Boolean getConsultantClickedResponse1() {
        return consultantClickedResponse1;
    }

    public void setConsultantClickedResponse1(Boolean consultantClickedResponse1) {
        this.consultantClickedResponse1 = consultantClickedResponse1;
    }

    public Boolean getConsultantClickedResponse2() {
        return consultantClickedResponse2;
    }

    public void setConsultantClickedResponse2(Boolean consultantClickedResponse2) {
        this.consultantClickedResponse2 = consultantClickedResponse2;
    }

    public Boolean getConsultantClickedResponse3() {
        return consultantClickedResponse3;
    }

    public void setConsultantClickedResponse3(Boolean consultantClickedResponse3) {
        this.consultantClickedResponse3 = consultantClickedResponse3;
    }
    
    public Integer getLastClickedResponseByConsultant() {
        return lastClickedResponseByConsultant;
    }

    public void setLastClickedResponseByConsultant(Integer lastClickedResponseByConsultant) {
        this.lastClickedResponseByConsultant = lastClickedResponseByConsultant;
    }

    public Date getDate_request() {
        return date_request;
    }

    public void setDate_request(Date date_request) {
        this.date_request = date_request;
    }

    public Date getDate_response() {
        return date_response;
    }

    public void setDate_response(Date date_response) {
        this.date_response = date_response;
    }
}
