package aes.utility;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import aes.controller.BaseController;
import aes.model.Evaluation;
import aes.persistence.GenericDAO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.naming.NamingException;


@Stateless
public class Scheduler extends BaseController<Evaluation>{
    
   
    public Scheduler() {
        try {
            daoBase = new GenericDAO<Evaluation>(Evaluation.class);
        } catch (NamingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(minute = "0", hour = "0", dayOfWeek = "*")
    public void scheduledEmails() {
      
        
    }
    
    
}
