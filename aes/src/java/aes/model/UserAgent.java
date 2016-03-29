package aes.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_user_agent")
public class UserAgent implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column(name = "description")
    private String description;
    
    @OneToMany(fetch = FetchType.LAZY)
    private List<PageNavigation> pageNavigation;
    

    public UserAgent() {
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PageNavigation> getPageNavigation() {
        return pageNavigation;
    }

    public void setPageNavigation(List<PageNavigation> pageNavigation) {
        this.pageNavigation = pageNavigation;
    }
    
    
}