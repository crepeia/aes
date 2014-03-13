/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
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
@ManagedBean(name = "parouDeFumarController")
@SessionScoped
public class ParouDeFumarController extends BaseController<Acompanhamento> {

	private static final int LIMITE_IGUALDADE_HORAS = 24;
	private String recaida;
	private Acompanhamento acompanhamento;

	/**
	 * Creates a new instance of ParouDeFumarController
	 */
	public ParouDeFumarController() {
		//super(Acompanhamento.class);
		try {
			this.daoBase = new GenericDAO<Acompanhamento>(Acompanhamento.class);
		} catch (NamingException ex) {
			String message = "Ocorreu um erro inesperado.";
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
			Logger.getLogger(ParouDeFumarController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}

	}

	public String recaidaOuLapso() {

		Acompanhamento a = this.getAcompanhamento();
		a.setRecaida(this.recaida.equals("1"));
		try {

			this.getDaoBase().insertOrUpdate(a, this.getEntityManager());

			if (a.isRecaida()) {
				return "parou-de-fumar-acompanhamento-recaidas-identificar-motivos.xhtml";
			} else {
				return "parou-de-fumar-acompanhamento-lapso-identificar-fatores-recaida.xhtml";
			}


		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;

	}

	public String identificarMotivos() {

		Acompanhamento a = this.getAcompanhamento();

		try {

			this.getDaoBase().insertOrUpdate(a, this.getEntityManager());

			return "parou-de-fumar-acompanhamento-lapso-identificar-fatores-recaida.xhtml";

		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;

	}

	public String identificarFatoresRecaida() {

		Acompanhamento a = this.getAcompanhamento();

		try {

			this.getDaoBase().insertOrUpdate(a, this.getEntityManager());

			return "parou-de-fumar-acompanhamento-lapso-plano-evitar-recaida.xhtml";

		} catch (SQLException ex) {
			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;

	}

	public void enviarEmail() {

		Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");

		if (object == null) {

			Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, "Usuário não logado no sistema requerendo plano.");
			//message to the user
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Você deve estar logado no sistema para solitar o envio do e-mail.", null));

		} else {

			User user = (User) object;
			Acompanhamento a = this.getAcompanhamento();

			try {
				String from = "watiufjf@gmail.com";
				String to = user.getEmail();
				String subject = "Plano para evitar recaídas -- Wati";
				String message = "Prezado " + user.getName() + ",\n\n"
						+ "Segue abaixo seu plano para evitar recaídas:\n\n"
						+ "\nEstratégias para lidar com a recaída:\n";
				if (a.getRecaidaLidar1() != null && !a.getRecaidaLidar1().isEmpty()) {
					message += a.getRecaidaLidar1() + "\n";
				}
				if (a.getRecaidaLidar2() != null && !a.getRecaidaLidar2().isEmpty()) {
					message += a.getRecaidaLidar2() + "\n";
				}
				if (a.getRecaidaLidar3() != null && !a.getRecaidaLidar3().isEmpty()) {
					message += a.getRecaidaLidar3() + "\n";
				}
				message += "\n\nAtenciosamente.\n";

				EMailSSL eMailSSL = new EMailSSL();
				eMailSSL.send(from, to, subject, message);

				Logger.getLogger(ParouDeFumarController.class.getName()).log(Level.INFO, "Plano para evitar recaídas enviado para o e-mail " + user.getEmail() + ".");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "E-mail enviado com sucesso.", null));

			} catch (Exception ex) {

				Logger.getLogger(ParouDeFumarController.class.getName()).log(Level.SEVERE, "Problemas ao enviar e-mail para " + user.getEmail() + ".");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao enviar e-mail. Por favor, tente novamente mais tarde.", null));

			}

		}
	}

	/**
	 * @return the recaida
	 */
	public String getRecaida() {

		Acompanhamento a = this.getAcompanhamento();

		if (a.isRecaida()) {
			return "1";
		} else {
			return "0";
		}

	}

	/**
	 * @param recaida the recaida to set
	 */
	public void setRecaida(String recaida) {
		this.recaida = recaida;
	}

	public Acompanhamento getAcompanhamento() {

		if (this.acompanhamento == null) {

			GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
			gc.add(GregorianCalendar.HOUR, ParouDeFumarController.LIMITE_IGUALDADE_HORAS);

			//System.out.println("Dia: " + gc.get( GregorianCalendar.DAY_OF_MONTH ));
			//System.out.println("Hora: " + gc.get( GregorianCalendar.HOUR_OF_DAY ));

			Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
			if (object != null) {
				try {
					//System.out.println( "inject: " + this.loginController.getUser().toString() );
					//List<ProntoParaParar> ppps = this.getDaoBase().list("usuario.id", ((User)object).getId(), this.getEntityManager());
					List<Acompanhamento> as = this.getDaoBase().list("usuario", object, this.getEntityManager());

					for (Acompanhamento a : as) {

						//System.out.println("Verificando acompanhamento: " + a.getId());
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime(a.getDataInserido());
						if (gc.after(calendar)) {
							this.acompanhamento = a;
							//System.out.println("Acompanhamento continua.");
						}

					}

					if (this.acompanhamento == null) {

						this.acompanhamento = new Acompanhamento();
						this.acompanhamento.setUsuario((User) object);

					}



				} catch (SQLException ex) {
					Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, null, ex);
				}

			} else {
				Logger.getLogger(ProntoParaPararController.class.getName()).log(Level.SEVERE, "Usuário não logado sendo acompanhado.");
				this.acompanhamento = new Acompanhamento();
			}

		}

		return this.acompanhamento;

	}
}
