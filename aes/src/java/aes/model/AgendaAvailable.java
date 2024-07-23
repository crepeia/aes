//Classe que armazena os dados de um horário livre de um usuário na agenda.

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
@Table(name = "tb_agenda_available")
@XmlRootElement
public class AgendaAvailable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //TODO: Melhoria de performance
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private User user;
    @Column(name = "available_date")
    private LocalDateTime availableDate;

    public AgendaAvailable() {
    }

    public AgendaAvailable(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        this.availableDate = availableDate;
    }

    //Desvincula as entidades para possibilitar a remoção do available do banco de dados (não usado atualmente)
    public void removeAvailable() {
        this.getUser().getAvailablesUser().remove(this);
        this.setUser(null);
    }
    
    
}
