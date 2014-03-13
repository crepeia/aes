/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;
import wati.model.Acompanhamento;
import wati.model.ProntoParaParar;
import wati.model.User;
import wati.persistence.GenericDAO;
import wati.utility.EMailSSL;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "prontoParaPararController")
@SessionScoped
public class ProntoParaPararController extends BaseController<ProntoParaParar> {

	private ProntoParaParar prontoParaParar = null;
	private String[] vencendoAFissuraMarcados = null;
	private static final int VENCENDO_FISSURA_BEBER_AGUA = 0;
	private static final int VENCENDO_FISSURA_COMER = 1;
	private static final int VENCENDO_FISSURA_RELAXAMENTO = 2;
	private static final int VENCENDO_FISSURA_LER_RAZOES = 3;
	private Map<String, String> anos = new LinkedHashMap<String, String>();
	private GregorianCalendar gregorianCalendar = null;

	public ProntoParaPararController() {
		//super(ProntoParaParar.class);
		try {
			this.daoBase = new GenericDAO<ProntoParaParar>( ProntoParaParar.class );
		} catch (NamingException ex) {
			String message = "Ocorreu um erro inesperado.";
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}

		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		int firstYear = gc.get(GregorianCalendar.YEAR);
		for (int i = firstYear; i < firstYear + 5; i++) {
			anos.put(String.valueOf(i), String.valueOf(i));
		}

	}

	public String vencendoAFissura() {
		this.prontoParaParar.limparVencendoFissura();

		for (String string : vencendoAFissuraMarcados) {

			switch (Integer.valueOf(string)) {
				case VENCENDO_FISSURA_BEBER_AGUA:
					this.prontoParaParar.setEnfrentarFissuraBeberAgua(true);
					break;
				case VENCENDO_FISSURA_COMER:
					this.prontoParaParar.setEnfrentarFissuraComer(true);
					break;
				case VENCENDO_FISSURA_LER_RAZOES:
					this.prontoParaParar.setEnfrentarFissuraLerRazoes(true);
					break;
				case VENCENDO_FISSURA_RELAXAMENTO:
					this.prontoParaParar.setEnfrentarFissuraRelaxamento(true);
					break;
			}

		}
		try {
			this.getDaoBase().insertOrUpdate(prontoParaParar, this.getEntityManager());
			return "pronto-para-parar-de-fumar-medicamentos.xhtml";
		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public String fumarMetodosDeParar() {

		return "pronto-para-parar-de-fumar-metodos-de-parar.xhtml";

	}

	public String dataParaParar() {

//		if ( this.ano.equals("Atual") ) {
//			this.ano = String.valueOf( new GregorianCalendar().get( GregorianCalendar.YEAR ) );
//		}
//		
//		if ( this.mes.equals("Atual") ) {
//			this.mes = String.valueOf( new GregorianCalendar().get( GregorianCalendar.MONTH ) );
//		}
//		
//		if ( this.dia.equals("Atual") ) {
//			this.dia = String.valueOf( new GregorianCalendar().get( GregorianCalendar.DAY_OF_MONTH ) );
//		}
		this.prontoParaParar.setDataParar(this.gregorianCalendar.getTime());

		try {
			this.getDaoBase().insertOrUpdate(prontoParaParar, this.getEntityManager());
			return "pronto-para-parar-de-fumar-como-evitar-recaidas.xhtml";
		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar a data de parar de fumar.", null));
		}
		return null;
	}

	public String recaidaTentouParar() {

		try {
			this.getDaoBase().insertOrUpdate(this.prontoParaParar, this.getEntityManager());
			if (this.prontoParaParar.isTentouParar()) {
				return "pronto-para-parar-de-fumar-como-evitar-recaidas-sim.xhtml";
			} else {
				return "pronto-para-parar-de-fumar-como-evitar-recaidas-completo.xhtml";
				//return "pronto-para-parar-de-fumar-ganho-de-peso.xhtml";
			}
		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar a data de parar de fumar.", null));
		}
		return null;
	}

	public String recaidasAjudar() {

		try {
			this.getDaoBase().insertOrUpdate(this.prontoParaParar, this.getEntityManager());
			return "pronto-para-parar-de-fumar-como-evitar-recaidas-completo.xhtml";
			//return "pronto-para-parar-de-fumar-ganho-de-peso.xhtml";
		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar a data de parar de fumar.", null));
		}
		return null;
	}

	public String recaidasEvitar() {

		try {
			this.getDaoBase().insertOrUpdate(this.prontoParaParar, this.getEntityManager());
			return "pronto-para-parar-de-fumar-ganho-de-peso.xhtml";
		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar a data de parar de fumar.", null));
		}
		return null;
	}

	/**
	 * @return the vencendoAFissuraMarcados
	 */
	public String[] getVencendoAFissuraMarcados() {

		if (this.vencendoAFissuraMarcados == null) {

			ProntoParaParar ppp = this.getProntoParaParar();

			int count = 0;
			if (ppp.isEnfrentarFissuraBeberAgua()) {
				count++;
			}
			if (ppp.isEnfrentarFissuraComer()) {
				count++;
			}
			if (ppp.isEnfrentarFissuraLerRazoes()) {
				count++;
			}
			if (ppp.isEnfrentarFissuraRelaxamento()) {
				count++;
			}
			this.vencendoAFissuraMarcados = new String[count];
			count = 0;
			if (ppp.isEnfrentarFissuraBeberAgua()) {
				this.vencendoAFissuraMarcados[ count] = String.valueOf(ProntoParaPararController.VENCENDO_FISSURA_BEBER_AGUA);
				count++;
			}
			if (ppp.isEnfrentarFissuraComer()) {
				this.vencendoAFissuraMarcados[ count] = String.valueOf(ProntoParaPararController.VENCENDO_FISSURA_COMER);
				count++;
			}
			if (ppp.isEnfrentarFissuraLerRazoes()) {
				this.vencendoAFissuraMarcados[ count] = String.valueOf(ProntoParaPararController.VENCENDO_FISSURA_LER_RAZOES);
				count++;
			}
			if (ppp.isEnfrentarFissuraRelaxamento()) {
				this.vencendoAFissuraMarcados[ count] = String.valueOf(ProntoParaPararController.VENCENDO_FISSURA_RELAXAMENTO);
				count++;
			}
		}

		return vencendoAFissuraMarcados;
	}

	/**
	 * @param vencendoAFissuraMarcados the vencendoAFissuraMarcados to set
	 */
	public void setVencendoAFissuraMarcados(String[] vencendoAFissuraMarcados) {
		this.vencendoAFissuraMarcados = vencendoAFissuraMarcados;
	}

	public void save(ActionEvent actionEvent) {
	}

	/**
	 * @return the dia
	 */
	public int getDia() {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		return this.gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);
	}

	/**
	 * @param dia the dia to set
	 */
	public void setDia(int dia) {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		this.gregorianCalendar.set(GregorianCalendar.DAY_OF_MONTH, dia);
	}

	/**
	 * @return the mes
	 */
	public int getMes() {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		return this.gregorianCalendar.get(GregorianCalendar.MONTH);
	}

	/**
	 * @param mes the mes to set
	 */
	public void setMes(int mes) {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		this.gregorianCalendar.set(GregorianCalendar.MONTH, mes);
	}

	/**
	 * @return the ano
	 */
	public int getAno() {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		return this.gregorianCalendar.get(GregorianCalendar.YEAR);
	}

	/**
	 * @param ano the ano to set
	 */
	public void setAno(int ano) {
		if (this.gregorianCalendar == null) {
			getProntoParaParar();
		}
		this.gregorianCalendar.set(GregorianCalendar.YEAR, ano);
	}

	/**
	 * @return the prontoParaParar
	 */
	public ProntoParaParar getProntoParaParar() {
		if (this.prontoParaParar == null) {
			this.gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
			Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
			if (object != null) {
				try {
					//System.out.println( "inject: " + this.loginController.getUser().toString() );
					//List<ProntoParaParar> ppps = this.getDaoBase().list("usuario.id", ((User)object).getId(), this.getEntityManager());
					List<ProntoParaParar> ppps = this.getDaoBase().list("usuario", object, this.getEntityManager());
					if (ppps.size() > 0) {
						this.prontoParaParar = ppps.get(0);
						this.gregorianCalendar.setTime(this.prontoParaParar.getDataParar());
					} else {
						this.prontoParaParar = new ProntoParaParar();
						this.prontoParaParar.setDataParar(this.gregorianCalendar.getTime());
					}
				} catch (SQLException ex) {
					Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
				}
				this.prontoParaParar.setUsuario((User) object);
			} else {
				Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, "Usuário não logado preenchendo ficha.");
			}
		}

		return prontoParaParar;
	}

	/**
	 * @param prontoParaParar the prontoParaParar to set
	 */
	public void setProntoParaParar(ProntoParaParar prontoParaParar) {
		this.prontoParaParar = prontoParaParar;
	}

	/**
	 * @return the anos
	 */
	public Map<String, String> getAnos() {
		return anos;
	}

	/**
	 * @param anos the anos to set
	 */
	public void setAnos(Map<String, String> anos) {
		this.anos = anos;
	}

	public List<ProntoParaParar> getProntoParaPararList() {
		ArrayList<ProntoParaParar> arrayList = new ArrayList<ProntoParaParar>();
		arrayList.add(prontoParaParar);
		return arrayList;
	}

	public void enviarEmail() {



		Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");

		if (object == null) {

			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, "Usuário não logado no sistema requerendo plano.");
			//message to the user
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Você deve estar logado no sistema para solitar o envio do e-mail.", null));

		} else {



			User user = (User) object;

			try {
				String from = "watiufjf@gmail.com";
				String to = user.getEmail();
				String subject = "Plano Personalizado -- Wati";
				String message = "Prezado " + user.getName() + ",\n\n"
						+ "Segue abaixo seu plano personalizado:\n\n"
						+ "Data de parada:\n"
						+ this.prontoParaParar.getDataPararStr()
						+ "\nTécnicas para fissura:\n"
						+ this.prontoParaParar.getFissuraStr()
						+ "\nEstratégias para resistir ao cigarro:\n";
				if (this.prontoParaParar.getEvitarRecaidaFara1() != null && !this.prontoParaParar.getEvitarRecaidaFara1().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaidaFara1() + "\n";
				}
				if (this.prontoParaParar.getEvitarRecaidaFara2() != null && !this.prontoParaParar.getEvitarRecaidaFara2().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaidaFara2() + "\n";
				}
				if (this.prontoParaParar.getEvitarRecaidaFara3() != null && !this.prontoParaParar.getEvitarRecaidaFara3().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaidaFara3() + "\n";
				}
				message += "O que deu certo da última vez que parei:\n";
				if (this.prontoParaParar.getEvitarRecaida1() != null && !this.prontoParaParar.getEvitarRecaida1().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaida1() + "\n";
				}
				if (this.prontoParaParar.getEvitarRecaida2() != null && !this.prontoParaParar.getEvitarRecaida2().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaida2() + "\n";
				}
				if (this.prontoParaParar.getEvitarRecaida3() != null && !this.prontoParaParar.getEvitarRecaida3().isEmpty()) {
					message += this.prontoParaParar.getEvitarRecaida3() + "\n";
				}
				message += "\n\nAtenciosamente.\n";

				EMailSSL eMailSSL = new EMailSSL();
				eMailSSL.send(from, to, subject, message);

				Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.INFO, "Plano personalizado enviado para o e-mail " + user.getEmail() + ".");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "E-mail enviado com sucesso.", null));

			} catch (Exception ex) {

				Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, "Problemas ao enviar e-mail para " + user.getEmail() + ".");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao enviar e-mail. Por favor, tente novamente mais tarde.", null));

			}

		}





	}
}
