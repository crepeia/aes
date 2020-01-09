/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bruno
 */
@Entity
@Table(name = "tb_challenge")
@XmlRootElement
public class Challenge implements Serializable {
    public enum ChallengeType {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY
      }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description", length = 300)
    private String description;
    
    @Column(name = "base_value")
    private Integer base_value;
    
    @Column(name = "modifier")
    private Float modifier;
    
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    public String getTitle() {
        return title;
    }

    public Integer getBase_value() {
        return base_value;
    }

    public void setBase_value(Integer base_value) {
        this.base_value = base_value;
    }

    public Float getModifier() {
        return modifier;
    }

    public void setModifier(Float modifier) {
        this.modifier = modifier;
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

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
}
