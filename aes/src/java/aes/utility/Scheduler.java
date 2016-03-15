package aes.utility;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import aes.controller.ContactController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class Scheduler {

    @Inject
    private ContactController contactController;
    
    public Scheduler() {
        
    }
    
    @Schedule(second = "*/5", minute = "*", hour = "*", dayOfWeek = "*")
    public void testTask() {
       Logger.getLogger(Scheduler.class.getName()).log(Level.INFO, "Scheduled task running");

    }

}