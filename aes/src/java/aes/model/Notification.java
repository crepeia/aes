/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Leonorico
 */
@Entity
@Table(name = "tb_notification")
@XmlRootElement
public class Notification implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User consultant;
    @ManyToOne
    private User user;
    private String message;
    private boolean notificated;

    public Notification() {
    }
    
    public Notification(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public User getConsultant() {
        return consultant;
    }

    public void setConsultant(User consultant) {
        this.consultant = consultant;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNotificated() {
        return notificated;
    }

    public void setNotificated(boolean notificated) {
        this.notificated = notificated;
    }
    
    
}
