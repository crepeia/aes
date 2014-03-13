/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Luiz Afonso
 */
@Entity
@Table(name="tb_inter03")
public class Inter_03 {
    
   @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
   
       @Column(name="estrategiaRecaida", length=400)
    private String estrategiaRecaida;
       
       @Column(name="motivoRecaida", length=300)
    private String motivoRecaida;
       
       	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	public String getEstrategiaRecaida() {
		return estrategiaRecaida;
	}

	public void setEstrategiaRecaida(String estrategiaRecaida) {
		this.estrategiaRecaida = estrategiaRecaida;
	}
       
        public String getMotivoRecaida() {
		return motivoRecaida;
	}

	public void setMotivoRecaida(String motivoRecaida) {
		this.motivoRecaida = motivoRecaida;
	}
    
}
