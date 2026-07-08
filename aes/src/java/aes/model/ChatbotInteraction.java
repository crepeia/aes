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
    @Column(name = "resposta_1", columnDefinition = "TEXT")
    private String resposta1;

    @Lob
    @Column(name = "resposta_2", columnDefinition = "TEXT")
    private String resposta2;

    @Lob
    @Column(name = "resposta_3", columnDefinition = "TEXT")
    private String resposta3;

    @Column(name = "consultor_clicou_resposta_1", nullable = false)
    private Boolean consultorClicouResposta1 = false;

    @Column(name = "consultor_clicou_resposta_2", nullable = false)
    private Boolean consultorClicouResposta2 = false;

    @Column(name = "consultor_clicou_resposta_3", nullable = false)
    private Boolean consultorClicouResposta3 = false;

    @Column(name = "consultor_editou_resposta_1", nullable = false)
    private Boolean consultorEditouResposta1 = false;

    @Column(name = "consultor_editou_resposta_2", nullable = false)
    private Boolean consultorEditouResposta2 = false;

    @Column(name = "consultor_editou_resposta_3", nullable = false)
    private Boolean consultorEditouResposta3 = false;

    // Opcional: qual das 3 serviu de base para o envio (1, 2, 3 ou null)
    @Column(name = "resposta_escolhida")
    private Integer respostaEscolhida;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "data_geracao", nullable = false)
    private Date dataGeracao;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "data_envio")
    private Date dataEnvio;

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

    public String getResposta1() {
        return resposta1;
    }

    public void setResposta1(String resposta1) {
        this.resposta1 = resposta1;
    }

    public String getResposta2() {
        return resposta2;
    }

    public void setResposta2(String resposta2) {
        this.resposta2 = resposta2;
    }

    public String getResposta3() {
        return resposta3;
    }

    public void setResposta3(String resposta3) {
        this.resposta3 = resposta3;
    }

    public Boolean getConsultorClicouResposta1() {
        return consultorClicouResposta1;
    }

    public void setConsultorClicouResposta1(Boolean v) {
        this.consultorClicouResposta1 = v;
    }

    public Boolean getConsultorClicouResposta2() {
        return consultorClicouResposta2;
    }

    public void setConsultorClicouResposta2(Boolean v) {
        this.consultorClicouResposta2 = v;
    }

    public Boolean getConsultorClicouResposta3() {
        return consultorClicouResposta3;
    }

    public void setConsultorClicouResposta3(Boolean v) {
        this.consultorClicouResposta3 = v;
    }

    public Boolean getConsultorEditouResposta1() {
        return consultorEditouResposta1;
    }

    public void setConsultorEditouResposta1(Boolean v) {
        this.consultorEditouResposta1 = v;
    }

    public Boolean getConsultorEditouResposta2() {
        return consultorEditouResposta2;
    }

    public void setConsultorEditouResposta2(Boolean v) {
        this.consultorEditouResposta2 = v;
    }

    public Boolean getConsultorEditouResposta3() {
        return consultorEditouResposta3;
    }

    public void setConsultorEditouResposta3(Boolean v) {
        this.consultorEditouResposta3 = v;
    }

    public Integer getRespostaEscolhida() {
        return respostaEscolhida;
    }

    public void setRespostaEscolhida(Integer respostaEscolhida) {
        this.respostaEscolhida = respostaEscolhida;
    }

    public Date getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Date dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
