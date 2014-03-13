/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.model;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import org.hibernate.annotations.Type;

/**
 *
 * @author hedersb
 */
@Entity
@Table(name = "tb_pronto_para_parar")
public class ProntoParaParar implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(name = "enfrentar_fissura_beber_agua")
	private boolean enfrentarFissuraBeberAgua;
	@Column(name = "enfrentar_fissura_comer")
	private boolean enfrentarFissuraComer;
	@Column(name = "enfrentar_fissura_relaxamento")
	private boolean enfrentarFissuraRelaxamento;
	@Column(name = "enfrentar_fissura_ler_razoes")
	private boolean enfrentarFissuraLerRazoes;
	@Column(name = "data_parar")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date dataParar;
	@Column(name = "tentou_parar")
	private boolean tentouParar;
	@Column(name = "evitar_recaida_1")
	private String evitarRecaida1;
	@Column(name = "evitar_recaida_2")
	private String evitarRecaida2;
	@Column(name = "evitar_recaida_3")
	private String evitarRecaida3;
	@Column(name = "evitar_recaida_fara_1")
	private String evitarRecaidaFara1;
	@Column(name = "evitar_recaida_fara_2")
	private String evitarRecaidaFara2;
	@Column(name = "evitar_recaida_fara_3")
	private String evitarRecaidaFara3; 
	//@Column(name="usuario")
	@OneToOne
	private User usuario;
	@Column(name = "data_inserido")
	@Temporal(javax.persistence.TemporalType.DATE)
	private Date dataInserido; 

	public ProntoParaParar() {
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
	 * @return the enfrentarFissuraBeberAgua
	 */
	public boolean isEnfrentarFissuraBeberAgua() {
		return enfrentarFissuraBeberAgua;
	}

	/**
	 * @param enfrentarFissuraBeberAgua the enfrentarFissuraBeberAgua to set
	 */
	public void setEnfrentarFissuraBeberAgua(boolean enfrentarFissuraBeberAgua) {
		this.enfrentarFissuraBeberAgua = enfrentarFissuraBeberAgua;
	}

	/**
	 * @return the enfrentarFissuraComer
	 */
	public boolean isEnfrentarFissuraComer() {
		return enfrentarFissuraComer;
	}

	/**
	 * @param enfrentarFissuraComer the enfrentarFissuraComer to set
	 */
	public void setEnfrentarFissuraComer(boolean enfrentarFissuraComer) {
		this.enfrentarFissuraComer = enfrentarFissuraComer;
	}

	/**
	 * @return the enfrentarFissuraRelaxamento
	 */
	public boolean isEnfrentarFissuraRelaxamento() {
		return enfrentarFissuraRelaxamento;
	}

	/**
	 * @param enfrentarFissuraRelaxamento the enfrentarFissuraRelaxamento to set
	 */
	public void setEnfrentarFissuraRelaxamento(boolean enfrentarFissuraRelaxamento) {
		this.enfrentarFissuraRelaxamento = enfrentarFissuraRelaxamento;
	}

	/**
	 * @return the enfrentarFissuraLerRazoes
	 */
	public boolean isEnfrentarFissuraLerRazoes() {
		return enfrentarFissuraLerRazoes;
	}

	/**
	 * @param enfrentarFissuraLerRazoes the enfrentarFissuraLerRazoes to set
	 */
	public void setEnfrentarFissuraLerRazoes(boolean enfrentarFissuraLerRazoes) {
		this.enfrentarFissuraLerRazoes = enfrentarFissuraLerRazoes;
	}

	public void limparVencendoFissura() {
		this.setEnfrentarFissuraBeberAgua(false);
		this.setEnfrentarFissuraComer(false);
		this.setEnfrentarFissuraLerRazoes(false);
		this.setEnfrentarFissuraRelaxamento(false);
	}

	/**
	 * @return the dataParar
	 */
	public Date getDataParar() {
		return dataParar;
	}

	/**
	 * @param dataParar the dataParar to set
	 */
	public void setDataParar(Date dataParar) {
		this.dataParar = dataParar;
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
	 * @return the tentouParar
	 */
	public boolean isTentouParar() {
		return tentouParar;
	}

	/**
	 * @param tentouParar the tentouParar to set
	 */
	public void setTentouParar(boolean tentouParar) {
		this.tentouParar = tentouParar;
	}

	/**
	 * @return the evitarRecaida1
	 */
	public String getEvitarRecaida1() {
		return evitarRecaida1;
	}

	/**
	 * @param evitarRecaida1 the evitarRecaida1 to set
	 */
	public void setEvitarRecaida1(String evitarRecaida1) {
		this.evitarRecaida1 = evitarRecaida1;
	}

	/**
	 * @return the evitarRecaida2
	 */
	public String getEvitarRecaida2() {
		return evitarRecaida2;
	}

	/**
	 * @param evitarRecaida2 the evitarRecaida2 to set
	 */
	public void setEvitarRecaida2(String evitarRecida2) {
		this.evitarRecaida2 = evitarRecida2;
	}

	/**
	 * @return the evitarRecaida3
	 */
	public String getEvitarRecaida3() {
		return evitarRecaida3;
	}

	/**
	 * @param evitarRecaida3 the evitarRecaida3 to set
	 */
	public void setEvitarRecaida3(String evitarRecida3) {
		this.evitarRecaida3 = evitarRecida3;
	}

	/**
	 * @return the evitarRecaidaFara1
	 */
	public String getEvitarRecaidaFara1() {
		return evitarRecaidaFara1;
	}

	/**
	 * @param evitarRecaidaFara1 the evitarRecaidaFara1 to set
	 */
	public void setEvitarRecaidaFara1(String evitarRecaidaFara1) {
		this.evitarRecaidaFara1 = evitarRecaidaFara1;
	}

	/**
	 * @return the evitarRecaidaFara2
	 */
	public String getEvitarRecaidaFara2() {
		return evitarRecaidaFara2;
	}

	/**
	 * @param evitarRecaidaFara2 the evitarRecaidaFara2 to set
	 */
	public void setEvitarRecaidaFara2(String evitarRecaidaFara2) {
		this.evitarRecaidaFara2 = evitarRecaidaFara2;
	}

	/**
	 * @return the evitarRecaidaFara3
	 */
	public String getEvitarRecaidaFara3() {
		return evitarRecaidaFara3;
	}

	/**
	 * @param evitarRecaidaFara3 the evitarRecaidaFara3 to set
	 */
	public void setEvitarRecaidaFara3(String evitarRecaidaFara3) {
		this.evitarRecaidaFara3 = evitarRecaidaFara3;
	}

	public String getDataPararStr() {
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		gc.setTime(dataParar);
		return gc.get(GregorianCalendar.DAY_OF_MONTH) + "/" + gc.get(GregorianCalendar.MONTH) + "/" + gc.get(GregorianCalendar.YEAR);
	}

	public String getFissuraStr() {
		StringBuilder s = new StringBuilder();
		if (isEnfrentarFissuraBeberAgua()) {
			s.append("Beber um copo de água pausadamente.\n");
		}
		if (isEnfrentarFissuraComer()) {
			s.append("Comer alimentos com baixa quantidade de calorias como frutas cristalizadas (uva passas), balas dietéticas e chicletes dietéticos.\n");
		}
		if (isEnfrentarFissuraLerRazoes()) {
			s.append("Fazer exercício de relaxamento - em áudio MP3 - link para download no plano de parar.\n");
		}
		if (isEnfrentarFissuraRelaxamento()) {
			s.append("Ler um cartão com suas razões para ter parado de fumar.\n");
		}
		return s.toString();
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
}
