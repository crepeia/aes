/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author bruno
 */
@Entity
@Table(name = "tb_tip")
@XmlRootElement
public class Tip implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description", length = 300)
    private String description;
    
    @OneToMany(mappedBy = "tip", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TipUser> tips;
    
    @OneToMany(mappedBy = "challenge", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChallengeUser> challenges;
    
    @JsonIgnore
    @XmlTransient
    public List<ChallengeUser> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<ChallengeUser> challenges) {
        this.challenges = challenges;
    }
    
    @JsonIgnore
    @XmlTransient
    public List<TipUser> getTips() {
        return tips;
    }

    public void setTips(List<TipUser> tips) {
        this.tips = tips;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
}