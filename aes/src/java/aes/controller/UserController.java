/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.User;
import aes.persistence.GenericDAO;
import aes.utility.Encrypter;
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
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "userController")
@SessionScoped
public class UserController extends BaseFormController<User> {

	private User user;

	private String password;

	private int day;
	private int month;
	private int year;

	private boolean showErrorMessage;

	private Map<String, String> days = new LinkedHashMap<String, String>();
	private Map<String, String> months = new LinkedHashMap<String, String>();
	private Map<String, String> years = new LinkedHashMap<String, String>();
	private String[] monthsName = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

	@PersistenceContext
	private EntityManager entityManager = null;
        
        private GenericDAO dao = null;
	
	/**
	 * Creates a new instance of UserController
	 */
	public UserController() {

		super(User.class);

		this.showErrorMessage = false;
                
                month = -1;

		for (int i = 1; i <= 31; i++) {
			days.put(String.valueOf(i), String.valueOf(i));
		}

		for (int i = 1; i < this.monthsName.length; i++){
			months.put(this.monthsName[i-1], String.valueOf(i-1));
		}

		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		int lastYear = gc.get(GregorianCalendar.YEAR) - 1;
		for (int i = lastYear; i > lastYear - 100; i--) {
			years.put(String.valueOf(i), String.valueOf(i));
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
		this.user.setBirth(year, month, day);

		try {
                    /*if (!(dao.list("email", user.getEmail(), entityManager).isEmpty())) {
                        String message = "email.cadastrado";
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
                    } else {*/

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
                    }
                catch (InvalidKeyException ex) {
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
                //return "quanto-voce-bebe-sim-beber-uso-audit-3.xhtml";
                System.out.println("teste");

	}

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Map<String, String> getDays() {
        return days;
    }

    public void setDays(Map<String, String> days) {
        this.days = days;
    }

    public Map<String, String> getMonths() {
        return months;
    }

    public void setMonths(Map<String, String> months) {
        this.months = months;
    }

    public Map<String, String> getYears() {
        return years;
    }

    public void setYears(Map<String, String> years) {
        this.years = years;
    }

    public String[] getMonthsName() {
        return monthsName;
    }

    public void setMonthsName(String[] monthsName) {
        this.monthsName = monthsName;
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
		this.year = 0;
		this.day = 0;
		this.month = -1;
		this.password = "";
		this.user = new User();
	}
}
