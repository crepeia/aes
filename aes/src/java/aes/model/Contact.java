/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import javax.faces.context.FacesContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.servlet.ServletContext;

/**
 *
 * @author thiago
 */
@Entity
@Table(name = "tb_contact")
public class Contact implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "sent_date")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Calendar sentDate;
    @Column(name = "sender")
    private String sender;
    @Column(name = "recipient")
    private String recipient;
    @Column (name = "subject")
    private String subject;
    @Column(name = "text_content")
    private String textContent;
    @Column (name="html_template")
    String htmlTemplate;
    @Transient
    ByteArrayOutputStream attachment;
    @Column (name="attachment_name")
    String attachmentName; 
    
    @ManyToOne(cascade = {CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    private User user;
    
        
    public void readHTMLTemplate(String relativePath) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        String absolutePath = servletContext.getRealPath(relativePath);
        byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
        htmlTemplate =  new String(encoded, StandardCharsets.UTF_8);
    }
    
    public void fillHTMLTemplate(String title, String subtitle, String body) {
            if(title != null){
                htmlTemplate = htmlTemplate.replace("#title#", title);
            }
            if(subtitle != null){
                htmlTemplate = htmlTemplate.replace("#subtitle#", subtitle);
            }
            if(body != null){
                htmlTemplate = htmlTemplate.replace("#body#", body); 
            }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Calendar getSentDate() {
        return sentDate;
    }

    public void setSentDate(Calendar sentDate) {
        this.sentDate = sentDate;
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

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    public ByteArrayOutputStream getAttachment() {
        return attachment;
    }

    public void setAttachment(ByteArrayOutputStream attachment) {
        this.attachment = attachment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    
}
