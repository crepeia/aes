/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.Contact;
import aes.model.User;
import aes.persistence.ContactDAO;
import aes.persistence.UserDAO;
import aes.utility.EMailSSL;
import aes.utility.Secured;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



/**
 *
 * @author Leonorico
 */
@Secured
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("contact")
public class ContactFacadeREST extends AbstractFacade<Contact> {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    private ContactDAO contactDao;
    private EMailSSL eMailSSL;

    public ContactFacadeREST() {
        super(Contact.class);
        try {
            contactDao = new ContactDAO();
            eMailSSL = new EMailSSL();
        } catch (NamingException ex) {
            Logger.getLogger(ContactFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @Path("sendAnnualScreeningEmail")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sendAnnualScreeningEmail(User user) {
        try {
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setSender(eMailSSL.replaceEmail("alcoolesaude@gmail.com"));
            contact.setRecipient(user.getEmail());
            contact.setSubject("annualscreening_subj");
            contact.setContent("annualscreening");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            contact.setDateScheduled(cal.getTime());
            contactDao.insertOrUpdate(contact, em);
            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | RuntimeException e) {
            Logger.getLogger(ContactFacadeREST.class.getName()).log(Level.SEVERE, "Error type: ", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
