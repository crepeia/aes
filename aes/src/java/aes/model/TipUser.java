/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bruno
 */
@Entity
@Table(name = "tb_tip_tb_user")
@XmlRootElement
public class TipUser implements Serializable {

    @EmbeddedId
    private TipUserKey id;
    
    @ManyToOne
    @MapsId("tipId")
    @JoinColumn(name = "tip_id")
    private Tip tip;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_envio")
    private Date dataEnvio;
    
    @Column(name = "liked")
    private boolean liked;
    
    public Tip getTip() {
        return tip;
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public TipUserKey getId() {
        return id;
    }

    public void setId(TipUserKey id) {
        this.id = id;
    }
    
    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipUser)) {
            return false;
        }
        TipUser other = (TipUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
}