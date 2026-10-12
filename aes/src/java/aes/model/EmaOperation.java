package aes.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/** Durable receipt, committed in the same transaction as the EMA answer. */
@Entity
@Table(name = "tb_ema_operation")
public class EmaOperation implements Serializable {
    @Id
    @Column(name = "operation_key", length = 80, nullable = false)
    private String key;
    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;
    @Lob
    @Column(name = "response_json", nullable = false)
    private String responseJson;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
}
