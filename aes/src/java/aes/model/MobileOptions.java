/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bruno
 */
@Entity
@Table(name = "tb_mobile_options")
@XmlRootElement
public class MobileOptions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @OneToOne
    private User user;

    @Column(name = "allow_drink_notifications")
    boolean allowDrinkNotifications;
    
    @Column(name = "allow_tip_notifications")
    boolean allowTipNotifications;
    
    @Temporal(javax.persistence.TemporalType.TIME)
    @Column(name = "drink_notification_time")
    Date drinkNotificationTime;
    
    @Temporal(javax.persistence.TemporalType.TIME)
    @Column(name = "tip_notification_time")
    Date tipNotificationTime;
    
    @Column(name = "notification_token")
    String notificationToken;
    
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

    public boolean isAllowDrinkNotifications() {
        return allowDrinkNotifications;
    }

    public void setAllowDrinkNotifications(boolean allowDrinkNotifications) {
        this.allowDrinkNotifications = allowDrinkNotifications;
    }

    public boolean isAllowTipNotifications() {
        return allowTipNotifications;
    }

    public void setAllowTipNotifications(boolean allowTipNotifications) {
        this.allowTipNotifications = allowTipNotifications;
    }

    public Date getDrinkNotificationTime() {
        return drinkNotificationTime;
    }

    public void setDrinkNotificationTime(Date drinkNotificationTime) {
        this.drinkNotificationTime = drinkNotificationTime;
    }

    public Date getTipNotificationTime() {
        return tipNotificationTime;
    }

    public void setTipNotificationTime(Date tipNotificationTime) {
        this.tipNotificationTime = tipNotificationTime;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }
    
}
