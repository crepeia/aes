package aes.utility;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import aes.model.Contact;
import aes.model.Evaluation;
import aes.persistence.EvaluationDAO;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class Scheduler {

    @PersistenceContext
    private EntityManager entityManager = null;

    public Scheduler() {

    }

    @Schedule(minute = "0", hour = "0", dayOfWeek = "*")
    public void scheduledEmails() {
        try {
            EMailSSL emailSSL = new EMailSSL();
            EvaluationDAO evaluationDAO = new EvaluationDAO();
            GenericDAO contactDAO = new GenericDAO(Contact.class);
            ArrayList<Evaluation> evaluations = (ArrayList<Evaluation>) evaluationDAO.getYearEmailEvaluations(null);
            Contact contact = new Contact();
            for (Evaluation e : evaluations) {
                if (e.getUser().getPregnant() || e.getUser().isUnderage() || e.getDrink() == false) {
                    contact = new Contact();
                    contact.setSender("acoolesaude@gmail.com");
                    contact.setRecipient(e.getUser().getEmail());
                    contact.setSubject("test1");
                    contact.setTextContent("test1");
                    contact.setSentDate(Calendar.getInstance());
                    emailSSL.send(contact);
                    contactDAO.insertOrUpdate(contact, entityManager);
                    e.setYearEmailDate(Calendar.getInstance());
                    evaluationDAO.insertOrUpdate(e, entityManager);
                } else {
                    contact = new Contact();
                    contact.setSender("acoolesaude@gmail.com");
                    contact.setRecipient(e.getUser().getEmail());
                    contact.setSubject("test2");
                    contact.setTextContent("test2");
                    contact.setSentDate(Calendar.getInstance());
                    emailSSL.send(contact);
                    contactDAO.insertOrUpdate(contact, entityManager);
                    e.setYearEmailDate(Calendar.getInstance());
                    evaluationDAO.insertOrUpdate(e, entityManager);
                }

            }
        } catch (NamingException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}