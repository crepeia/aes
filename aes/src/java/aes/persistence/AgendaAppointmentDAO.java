/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.persistence;

import aes.model.AgendaAppointment;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

/**
 *
 * @author Leonorico
 */
public class AgendaAppointmentDAO extends GenericDAO<AgendaAppointment> {
    
    UserDAO userDao;
    
    public AgendaAppointmentDAO() throws NamingException {
        super(AgendaAppointment.class);
        userDao = new UserDAO();
    }
    
    //TOVERIFY
    @Override
    public void insert(AgendaAppointment appointment, EntityManager em) throws SQLException {
        userDao.find(appointment.getUser(), em).setAppointmentUser(appointment);
        userDao.find(appointment.getConsultant(), em).setAppointmentConsultant(appointment);
        super.insert(appointment, em);
    }
    
    //TOVERIFY
    public void remove(long id, EntityManager em) {
        em.getTransaction().begin();
        find(id, em).removeAppointment();
        em.getTransaction().commit();
        em.remove(find(id, em));
        em.getTransaction().commit();
    }
    
    //TOVERIFY
    public void update(long id, AgendaAppointment appointment, EntityManager em) {
        em.getTransaction().begin();
        AgendaAppointment app = find(id, em);
        app.setUser(appointment.getUser());
        app.setConsultant(appointment.getConsultant());
        app.setAppointmentDate(appointment.getAppointmentDate());
        em.getTransaction().commit();
    }
    
    public List<AgendaAppointment> findAll(EntityManager em) throws SQLException {
        //as linhas abaixo são uma solução próxima do que seria ao retorno de uma consulta envolvendo apenas os 4 campos e evitando
        //dos de User e Consultant na classe AgendaAppointment fetchType_Lazy serem comentados. Não funciona, porém, por falta de 
        //conhecimento para criar a JPQL correta
        
        //Ao que tudo indica, deveria fazer uma Query para fazer essa consulta a partir apenas dos dados que eu quero do appointment
        //sem precisar de fazer esses JOINS, puxando apenas os dados a partir do appointment, id do app, date, id do user e id do consultant
        
        
//        String jpql = "SELECT app.id, app.appointmentDate, u.id, c.id FROM AgendaAppointment app JOIN app.user u JOIN app.consultant c";
//        return em.createQuery(jpql, AgendaAppointment.class).getResultList();

        return super.list(em);
    }

    public AgendaAppointment find(long id, EntityManager em) {
        return super.find(id, em);
    }
}
