/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;

import aes.model.Evaluation;
import aes.model.KeepResults;
import aes.model.User;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.NamingException;

/**
 *
 * @author thiago
 */
@ManagedBean(name = "keepResultsController")
@SessionScoped
public class KeepResultsController extends BaseController<KeepResults>{
    
    private KeepResults keepResults;
    
    public KeepResultsController() {
        try {
            this.daoBase = new GenericDAO<KeepResults>(KeepResults.class);
        } catch (NamingException ex) {
            Logger.getLogger(EvaluationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public KeepResults getKeepResults() {
        if(keepResults == null && loggedUser()){
            keepResults = getLoggedUser().getKeepResults();
            if(keepResults == null){
                keepResults = new KeepResults();
                keepResults.setUser(getLoggedUser());
            }
        }
        return keepResults;
    }
    
    public String intro(){
        if(getUser().isFemale()){
            return "estrategia-diminuir-registro-eletronico-meta-mulher.xhtml?faces-redirect=true";
        }else{
            return "estrategia-diminuir-registro-eletronico-meta-mulher.xhtml?faces-redirect=true";
        }
    }
    
    public String goals(){ 
        try {
            daoBase.insertOrUpdate(keepResults, getEntityManager());
            return "estrategia-diminuir-registro-eletronico.xhtml?faces-redirect=true";
        } catch (SQLException ex) {
            Logger.getLogger(KeepResultsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
        
    
    public User getUser(){
            return getKeepResults().getUser();
    }
    
    
    
    
     
     
}
