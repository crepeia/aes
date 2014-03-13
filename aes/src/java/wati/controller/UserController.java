/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import wati.model.User;
import wati.utility.Encrypter;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "userController")
@SessionScoped
public class UserController extends BaseFormController<User> {

	private User user;

	private String password;

	private int dia;
	private int mes;
	private int ano;

	private boolean showErrorMessage;

	private Map<String, String> dias = new LinkedHashMap<String, String>();
	private Map<String, String> meses = new LinkedHashMap<String, String>();
	private Map<String, String> anos = new LinkedHashMap<String, String>();
	private String[] nomeMeses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

	@PersistenceContext
	private EntityManager entityManager = null;
	
	/**
	 * Creates a new instance of UserController
	 */
	public UserController() {

		super(User.class);

		this.showErrorMessage = false;

		for (int i = 1; i <= 31; i++) {
			dias.put(String.valueOf(i), String.valueOf(i));
		}

		for (int i = 0; i < this.nomeMeses.length; i++) {
			//meses.put(this.nomeMeses[ i], String.valueOf(i + 1));
			meses.put(this.nomeMeses[ i], String.valueOf(i));
		}

		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		int lastYear = gc.get(GregorianCalendar.YEAR) - 1;
		for (int i = lastYear; i > lastYear - 100; i--) {
			anos.put(String.valueOf(i), String.valueOf(i));
		}

	}

	/**
	 * @return the user
	 */
	public User getUser() {
		if (user == null) {
			String id = this.getParameterMap().get("id");
			if (id == null || id.isEmpty()) {
				this.user = new User();
			} else {
				try {
					List<User> list = this.getDaoBase().list("id", Long.parseLong(id), this.entityManager);
					if (list.isEmpty()) {
						this.user = new User();
					} else {
						this.user = list.get(0);
					}
				} catch (SQLException ex) {
					Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
					this.user = new User();
				}
			}

		}
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		if (this.password == null) {
			this.password = this.user == null || this.user.getPassword() == null ? "" : this.user.getPassword().toString();
		}
		return this.password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void save(ActionEvent actionEvent) {

		this.showErrorMessage = true;
		this.user.setBirth(new GregorianCalendar(ano, mes, dia).getTime());

		try {

			if (user.getId() == 0) {

				//incluir criptografia da senha
				this.user.setPassword(Encrypter.encrypt(this.password));

			} else {

				if (!Encrypter.compare(this.password, this.user.getPassword())) {
					//incluir criptografia da senha
					this.user.setPassword(Encrypter.encrypt(this.password));
				}

			}

			super.save(actionEvent, entityManager);
			//FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( FacesMessage.SEVERITY_INFO, "Usuário criado com sucesso.", null ));
			this.clear();

		} catch (InvalidKeyException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar usuário.", null));
			Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalBlockSizeException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar usuário.", null));
			Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (BadPaddingException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar usuário.", null));
			Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar usuário.", null));
			Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchPaddingException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas ao gravar usuário.", null));
			Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	/**
	 * @return the dia
	 */
	public int getDia() {
		return dia;
	}

	/**
	 * @param dia the dia to set
	 */
	public void setDia(int dia) {
		this.dia = dia;
	}

	/**
	 * @return the mes
	 */
	public int getMes() {
		return mes;
	}

	/**
	 * @param mes the mes to set
	 */
	public void setMes(int mes) {
		this.mes = mes;
	}

	/**
	 * @return the ano
	 */
	public int getAno() {
		return ano;
	}

	/**
	 * @param ano the ano to set
	 */
	public void setAno(int ano) {
		this.ano = ano;
	}

	/**
	 * @return the dias
	 */
	public Map<String, String> getDias() {
		return dias;
	}

	/**
	 * @param dias the dias to set
	 */
	public void setDias(Map<String, String> dias) {
		this.dias = dias;
	}

	/**
	 * @return the meses
	 */
	public Map<String, String> getMeses() {
		return meses;
	}

	/**
	 * @param meses the meses to set
	 */
	public void setMeses(Map<String, String> meses) {
		this.meses = meses;
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

	/**
	 * @return the showErrorMessage
	 */
	public boolean isShowErrorMessage() {
		return showErrorMessage;
	}

	/**
	 * @param showErrorMessage the showErrorMessage to set
	 */
	public void setShowErrorMessage(boolean showErrorMessage) {
		this.showErrorMessage = showErrorMessage;
	}

	private void clear() {
		this.ano = 0;
		this.dia = 0;
		this.mes = 0;
		this.password = "";
		this.user = new User();
	}
}
