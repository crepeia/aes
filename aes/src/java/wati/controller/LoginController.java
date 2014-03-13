/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import wati.model.User;
import wati.utility.Encrypter;

@ManagedBean(name = "loginController")
@SessionScoped
public class LoginController extends BaseFormController<User> {

	private User user = new User();
	private String password;
	private boolean showName;
	
	private String logout;

	/**
	 * Creates a new instance of LoginController
	 */
	public LoginController() {

		super(User.class);

		this.password = "";

	}

	/**
	 * @return the user
	 */
	public User getUser() {
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
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void loginDialog() {

		try {
			List<User> userList = this.getDaoBase().list("email", this.user.getEmail(), this.getEntityManager());

			if (userList.isEmpty() || !Encrypter.compare(this.password, userList.get(0).getPassword())) {
				//log message
				Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "O usuário com o e-mail '" + this.getUser().getEmail() + "' não conseguiu logar.");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail ou senha inválida.", null));

			} else {

				this.user = userList.get(0);

//                              FacesContext facesContext = FacesContext.getCurrentInstance();
//                              HttpSession session = (HttpSession) facesContext.getExternalContext().getSession( false );
//                              session.setAttribute( "loggedUser" , userList.get( 0 ));
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loggedUser", userList.get(0));

				Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "O usuário '" + this.getUser().getName() + "' logou no sistema.");

			}

		} catch (InvalidKeyException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalBlockSizeException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (BadPaddingException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchPaddingException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SQLException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas no acesso ao banco de dados.", null));
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}

		if (this.user.getId() > 0) {
			this.showName = true;
		} else {
			this.showName = false;
		}

	}

	public void login() {

		try {

			List<User> userList = this.getDaoBase().list("email", this.user.getEmail(), this.getEntityManager());

			if (userList.isEmpty() || !Encrypter.compare(this.password, userList.get(0).getPassword())) {
				//log message
				Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "O usuário com o e-mail '" + this.getUser().getEmail() + "' não conseguiu logar.");
				//message to the user
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail ou senha inválida.", null));

			} else {

				this.user = userList.get(0);

//                              FacesContext facesContext = FacesContext.getCurrentInstance();
//                              HttpSession session = (HttpSession) facesContext.getExternalContext().getSession( false );
//                              session.setAttribute( "loggedUser" , userList.get( 0 ));
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loggedUser", userList.get(0));

				Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "O usuário '" + this.getUser().getName() + "' logou no sistema.");

				if (this.user.getId() > 0) {
					this.showName = true;
				} else {
					this.showName = false;
				}

				Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("url");
				if (object != null) {
					String url = (String) object;
					try {
						Logger.getLogger(LoginController.class.getName()).log(Level.INFO, url);
						FacesContext.getCurrentInstance().getExternalContext().redirect(url);
					} catch (IOException ex) {
						Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
					}
				}

			}

		} catch (InvalidKeyException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalBlockSizeException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (BadPaddingException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchPaddingException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SQLException ex) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problemas no acesso ao banco de dados.", null));
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
		}

	}

	//TODO -- arrumar a funcionalidade de deslogar do sistema
	public String getLogout() {

		Logger.getLogger(LoginController.class.getName()).log(Level.INFO, "O usuário '" + this.getUser().getName() + "' saiu no sistema.");

		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
			
			//this.user = new User();

			//FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loggedUser", this.user);

			//this.showName = false;
		} catch (IOException ex) {
			Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return "";

	}
	
	/**
	 * @return the showName
	 */
	public boolean isShowName() {
		return showName;
	}

	/**
	 * @param showName the showName to set
	 */
	public void setShowName(boolean showName) {
		this.showName = showName;
	}
}
