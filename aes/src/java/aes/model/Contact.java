package aes.model;

import java.io.ByteArrayOutputStream;
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
import javax.persistence.Transient;

@Entity
@Table(name = "tb_contact")
public class Contact implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_sent")
    private Date dateSent;
    @Column(name = "sender")
    private String sender;
    @Column(name = "recipient")
    private String recipient;
    @Column(name = "subject")
    private String subject;
    @Column(name = "content")
    private String content;
    @Column(name = "attachment")
    private String attachment;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_scheduled")
    private Date dateScheduled;
    @Transient
    ByteArrayOutputStream pdf;

    @ManyToOne
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateScheduled() {
        return dateScheduled;
    }

    public void setDateScheduled(Date dateScheduled) {
        this.dateScheduled = dateScheduled;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public ByteArrayOutputStream getPdf() {
        return pdf;
    }

    public void setPdf(ByteArrayOutputStream pdf) {
        this.pdf = pdf;
    }

}
