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
import wati.model.User;
import wati.utility.Encrypter;

/**
 *
 * @author Luiz Afonso
 */

@ManagedBean(name = "inter_01Controller")
@SessionScoped
public class Inter_01Controller{
    
    private int radio_01;
    private int radio_02;
    
    public Inter_01Controller() {
       
    }

        /**
	 * @return o valor escolhido no radio_01
	 */
	public int getRadio_01() {
		return radio_01;
	}

	/**
	 * @param  radio01 o valor escolhido no radio_01 to set
	 */
	public void setRadio_01(int radio_01) {
		this.radio_01 = radio_01;
	}

        /**
	 * @return o valor do segundo radio
	 */
	public int getRadio_02() {
		return radio_02;
	}

	/**
	 * @param radio_02 o valor escolhido no radio_02 to set
	 */
	public void setRadio_02(int radio_02) {
		this.radio_02 = radio_02;
	}
    
    
    
}
