package aes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "tb_sensor_data")
@XmlRootElement
public class SensorData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sensor_type", length = 50)
    private String sensorType;

    // Latitude (GPS) ou Eixo X (Acelerómetro)
    @Column(name = "val_x")
    private Double valX;

    // Longitude (GPS) ou Eixo Y (Acelerómetro)
    @Column(name = "val_y")
    private Double valY;

    // Precisão/Raio (GPS) ou Eixo Z (Acelerómetro)
    @Column(name = "val_z")
    private Double valZ;

    /**
     * Mesmo padrão do EmaAnswer: hora local (America/Sao_Paulo), sem offset no wire.
     * O frontend envia "yyyy-MM-dd'T'HH:mm:ss.SSS" gerado com getLocalTimestamp().
     */
    @Column(name = "timestamp")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(
        shape   = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS",  // sem X — não espera offset/Z
        timezone = "America/Sao_Paulo"
    )
    private Date timestamp;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public Double getValX() { return valX; }
    public void setValX(Double valX) { this.valX = valX; }

    public Double getValY() { return valY; }
    public void setValY(Double valY) { this.valY = valY; }

    public Double getValZ() { return valZ; }
    public void setValZ(Double valZ) { this.valZ = valZ; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}