/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import wati.model.User;
import wati.persistence.GenericDAO;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "userListController")
@ViewScoped
public class UserListController implements Serializable {

	private GenericDAO<User> dao;
	@PersistenceContext
	private EntityManager entityManager = null;
	private List<User> list;
	private List<User> filteredList;
	private User selected;

	/**
	 * Creates a new instance of UserListController
	 */
	public UserListController() {

		try {
			this.dao = new GenericDAO<User>(User.class);
		} catch (NamingException ex) {
			String message = "Um erro inesperado ocorreu.";
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
			Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}

	}

	public List<User> getList() {
		if (this.list == null) {
			try {
				this.list = this.dao.list(this.entityManager);
			} catch (SQLException ex) {
				String message = "Problemas ao acessar o banco de dados.";
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
				Logger.getLogger(UserListController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
				this.list = new ArrayList<User>();
			}
		}
		return this.list;
	}

	/**
	 * @return the filteredList
	 */
	public List<User> getFilteredList() {
		return filteredList;
	}

	/**
	 * @param filteredList the filteredList to set
	 */
	public void setFilteredList(List<User> filteredList) {
		this.filteredList = filteredList;
	}

	/**
	 * @return the selected
	 */
	public User getSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(User selected) {
		this.selected = selected;
	}
}
