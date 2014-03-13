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
@Table(name="tb_inter01")
public class Inter_01 { 
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

        @Column(name="Radio_01", length=1)
    private int radio_01;
	
	@Column(name="Radio_02", length=1)
    private int radio_02;
	

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

	/**
	 * @return o valor escolhido no radio_01
	 */
	public int getRadio_01() {
		return radio_01;
	}

	/**
	 * @param  radio01 o valor escolhido no radio_01 to set
	 */
	public void setRadio_01(int radio_01) {
		this.radio_01 = radio_01;
	}

/**
	 * @return o valor do segundo radio
	 */
	public int getRadio_02() {
		return radio_02;
	}

	/**
	 * @param radio_02 o valor escolhido no radio_02 to set
	 */
	public void setRadio_02(int radio_02) {
		this.radio_02 = radio_02;
	}
        
	
}
