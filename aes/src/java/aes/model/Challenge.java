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
/**
 * @Deprecated
 * This class is no longer in use, because challenge is not in the databank anymore.
 * Challenge is in .properties file and should be used from there, instead.
**/
@Deprecated
public class Challenge implements Serializable {
    public enum ChallengeType {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY,
        ACCUMULATIVE
      }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "c_prefix")
    private String prefix;

    @Column(name = "base_value")
    private Integer base_value;

    @Column(name = "modifier")
    private Float modifier;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ChallengeType type;

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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
}
