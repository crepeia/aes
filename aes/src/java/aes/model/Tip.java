/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/**
 *
 * @author bruno
 */
@Entity
@Table(name = "tb_tip")
@XmlRootElement
/**
 * @Deprecated
 * This class is no longer in use, because tip is not in the databank anymore.
 * Tip is in .properties file and should be used from there, instead.
**/
@Deprecated
public class Tip implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description_pt", length = 300)
    private String descriptionPT;
    
    @Column(name = "description_en", length = 300)
    private String descriptionEN;
    
    @Column(name = "description_es", length = 300)
    private String descriptionES;
    
    @OneToMany(mappedBy = "tip", fetch = FetchType.LAZY)
    private List<TipUser> tips;

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

    public String getDescriptionPT() {
        return descriptionPT;
    }

    public void setDescriptionPT(String descriptionPT) {
        this.descriptionPT = descriptionPT;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }

    public void setDescriptionEN(String descriptionEN) {
        this.descriptionEN = descriptionEN;
    }

    public String getDescriptionES() {
        return descriptionES;
    }

    public void setDescriptionES(String descriptionES) {
        this.descriptionES = descriptionES;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
