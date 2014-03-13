/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.model;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author hedersb
 */
@Entity
@Table(name = "tb_acompanhamento")
public class Acompanhamento {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne
	private User usuario;
	@Column(name = "recaida")
	private boolean recaida; //se verdadeiro então recaida, caso contrário, lapso
	@Column(name = "data_inserido")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date dataInserido;
	@Column(name = "motivo_recaida_situacao")
	private String motivoRecaidaSituacao;
	@Column(name = "motivo_recaida_fazendo")
	private String motivoRecaidaFazendo;
	@Column(name = "motivo_recaida_depois")
	private String motivoRecaidaDepois;
	@Column(name = "motivo_recaida_cigarros_fuma")
	private int motivoRecaidaCigarrosFuma;
	@Column(name = "recaida_situacao_1")
	private String recaidaSituacao1;
	@Column(name = "recaida_situacao_2")
	private String recaidaSituacao2;
	@Column(name = "recaida_situacao_3")
	private String recaidaSituacao3;
	@Column(name = "recaida_lidar_1")
	private String recaidaLidar1;
	@Column(name = "recaida_lidar_2")
	private String recaidaLidar2;
	@Column(name = "recaida_lidar_3") 
	private String recaidaLidar3;

	public Acompanhamento() {
		this.dataInserido = ((GregorianCalendar) GregorianCalendar.getInstance()).getTime();
	}

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
	 * @return the usuario
	 */
	public User getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario the usuario to set
	 */
	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the recaida
	 */
	public boolean isRecaida() {
		return recaida;
	}

	/**
	 * @param recaida the recaida to set
	 */
	public void setRecaida(boolean recaida) {
		this.recaida = recaida;
	}

	/**
	 * @return the dataInserido
	 */
	public Date getDataInserido() {
		return dataInserido;
	}

	/**
	 * @param dataInserido the dataInserido to set
	 */
	public void setDataInserido(Date dataInserido) {
		this.dataInserido = dataInserido;
	}

	/**
	 * @return the motivoRecaidaSituacao
	 */
	public String getMotivoRecaidaSituacao() {
		return motivoRecaidaSituacao;
	}

	/**
	 * @param motivoRecaidaSituacao the motivoRecaidaSituacao to set
	 */
	public void setMotivoRecaidaSituacao(String motivoRecaidaSituacao) {
		this.motivoRecaidaSituacao = motivoRecaidaSituacao;
	}

	/**
	 * @return the motivoRecaidaFazendo
	 */
	public String getMotivoRecaidaFazendo() {
		return motivoRecaidaFazendo;
	}

	/**
	 * @param motivoRecaidaFazendo the motivoRecaidaFazendo to set
	 */
	public void setMotivoRecaidaFazendo(String motivoRecaidaFazendo) {
		this.motivoRecaidaFazendo = motivoRecaidaFazendo;
	}

	/**
	 * @return the motivoRecaidaDepois
	 */
	public String getMotivoRecaidaDepois() {
		return motivoRecaidaDepois;
	}

	/**
	 * @param motivoRecaidaDepois the motivoRecaidaDepois to set
	 */
	public void setMotivoRecaidaDepois(String motivoRecaidaDepois) {
		this.motivoRecaidaDepois = motivoRecaidaDepois;
	}

	/**
	 * @return the motivoRecaidaCigarrosFuma
	 */
	public int getMotivoRecaidaCigarrosFuma() {
		return motivoRecaidaCigarrosFuma;
	}

	/**
	 * @param motivoRecaidaCigarrosFuma the motivoRecaidaCigarrosFuma to set
	 */
	public void setMotivoRecaidaCigarrosFuma(int motivoRecaidaCigarrosFuma) {
		this.motivoRecaidaCigarrosFuma = motivoRecaidaCigarrosFuma;
	}

	/**
	 * @return the recaidaSituacao1
	 */
	public String getRecaidaSituacao1() {
		return recaidaSituacao1;
	}

	/**
	 * @param recaidaSituacao1 the recaidaSituacao1 to set
	 */
	public void setRecaidaSituacao1(String recaidaSituacao1) {
		this.recaidaSituacao1 = recaidaSituacao1;
	}

	/**
	 * @return the recaidaSituacao2
	 */
	public String getRecaidaSituacao2() {
		return recaidaSituacao2;
	}

	/**
	 * @param recaidaSituacao2 the recaidaSituacao2 to set
	 */
	public void setRecaidaSituacao2(String recaidaSituacao2) {
		this.recaidaSituacao2 = recaidaSituacao2;
	}

	/**
	 * @return the recaidaSituacao3
	 */
	public String getRecaidaSituacao3() {
		return recaidaSituacao3;
	}

	/**
	 * @param recaidaSituacao3 the recaidaSituacao3 to set
	 */
	public void setRecaidaSituacao3(String recaidaSituacao3) {
		this.recaidaSituacao3 = recaidaSituacao3;
	}

	/**
	 * @return the recaidaLidar1
	 */
	public String getRecaidaLidar1() {
		return recaidaLidar1;
	}

	/**
	 * @param recaidaLidar1 the recaidaLidar1 to set
	 */
	public void setRecaidaLidar1(String recaidaLidar1) {
		this.recaidaLidar1 = recaidaLidar1;
	}

	/**
	 * @return the recaidaLidar2
	 */
	public String getRecaidaLidar2() {
		return recaidaLidar2;
	}

	/**
	 * @param recaidaLidar2 the recaidaLidar2 to set
	 */
	public void setRecaidaLidar2(String recaidaLidar2) {
		this.recaidaLidar2 = recaidaLidar2;
	}

	/**
	 * @return the recaidaLidar3
	 */
	public String getRecaidaLidar3() {
		return recaidaLidar3;
	}

	/**
	 * @param recaidaLidar3 the recaidaLidar3 to set
	 */
	public void setRecaidaLidar3(String recaidaLidar3) {
		this.recaidaLidar3 = recaidaLidar3;
	}
}
