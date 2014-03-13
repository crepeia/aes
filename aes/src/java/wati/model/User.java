/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.model;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author hedersb
 */
@Entity
@Table(name = "tb_user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(name = "name", length = 100)
	private String name;
	@Column(name = "email", length = 50, unique = true)
	private String email;
	@Column(name = "password", length = 16)
	private byte[] password;
	@Column(name = "birth")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date birth;
	@Column(name = "gender")
	private char gender;
	@OneToOne(mappedBy = "usuario")
	private ProntoParaParar prontoParaParar;
	
	@OneToMany(fetch= FetchType.LAZY)
	private List<Acompanhamento> acompanhamentos;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public byte[] getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(byte[] password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return this.id + ", " + this.name + ", " + this.email + ", " + new String(this.password);
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the birth
	 */
	public Date getBirth() {
		return birth;
	}

	/**
	 * @param birth the birth to set
	 */
	public void setBirth(Date birth) {
		this.birth = birth;
	}

	/**
	 * @return the gender
	 */
	public char getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(char gender) {
		this.gender = gender;
	}

	/**
	 * @return the prontoParaParar
	 */
	public ProntoParaParar getProntoParaParar() {
		return prontoParaParar;
	}

	/**
	 * @param prontoParaParar the prontoParaParar to set
	 */
	public void setProntoParaParar(ProntoParaParar prontoParaParar) {
		this.prontoParaParar = prontoParaParar;
	}

	/**
	 * @return the acompanhamentos
	 */
	public List<Acompanhamento> getAcompanhamentos() {
		return acompanhamentos;
	}

	/**
	 * @param acompanhamentos the acompanhamentos to set
	 */
	public void setAcompanhamentos(List<Acompanhamento> acompanhamentos) {
		this.acompanhamentos = acompanhamentos;
	}
}
