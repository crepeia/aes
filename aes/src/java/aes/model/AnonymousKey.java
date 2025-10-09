package aes.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author luansb
 */

@Entity
@Table(name = "tb_anonymous_key")
@XmlRootElement
public class AnonymousKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "instance_id", nullable = false, unique = true, length = 64)
    private String instanceId;
    
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    
    @Column(name = "client_meta", length = 64)
    private String clientMeta;
    
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated = LocalDateTime.now();
    
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;
    
    public AnonymousKey() {}
    
    public AnonymousKey(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
   public void setInstanceId(String instanceId) {
       this.instanceId = instanceId;
   }
   
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
       this.publicKey = publicKey;
    }

    public String getClientMeta() {
        return clientMeta;
    }
    
    public void setClientMeta(String clientMeta) {
       this.clientMeta = clientMeta;
    }
    
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public boolean getRevoked() {
        return revoked;
    }
    
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
