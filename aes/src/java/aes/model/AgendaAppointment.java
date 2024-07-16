//Classe que armazena os dados de um horário reservado na agenda, envolvendo usuário e consultor.

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
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
@Table(name = "tb_agenda_appointment")
@XmlRootElement
public class AgendaAppointment implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //TODO: Melhoria de performance
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private User user;
    //TODO: Melhoria de performance
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private User consultant;
    @Column(name = "appointment_date")
    private LocalDateTime appointmentDate;

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getConsultant() {
        return consultant;
    }

    public void setConsultant(User consultant) {
        this.consultant = consultant;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    //Desvincula as entidades para possibilitar a remoção do appointment do banco de dados (não usado atualmente)
    public void removeAppointment() {
        this.getUser().getAppointmentsUser().remove(this);
        this.getConsultant().getAppointmentsConsultant().remove(this);
        this.setUser(null);
        this.setConsultant(null);
    }
        
}
