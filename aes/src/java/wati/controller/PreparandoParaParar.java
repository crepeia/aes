/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wati.controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import wati.model.User;

/**
 *
 * @author hedersb
 */
@ManagedBean(name = "preparandoParaParar")
@SessionScoped
public class PreparandoParaParar {

    public PreparandoParaParar() {
    }
	
	public String prontoParaPararDeFumar() {
		
		Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedUser");
		if ( obj != null ) {
			User user = (User) obj;
			if ( user.getId() > 0 ) {
				return "pronto-para-parar-de-fumar-introducao.xhtml";
			}
		}
		
		return "cadastrar-nova-conta.xhtml";
		
	}

    
}
