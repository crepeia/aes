/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.controller;
import aes.model.Chat;
import aes.persistence.GenericDAO;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.NamingException;

/**
 *
 * @author bruno
 */
@Named(value = "chatController")
@ApplicationScoped
public class ChatController extends BaseController<Chat>{
    private Chat chat;

   
    
    @PostConstruct
    public void init() {
        try {
            daoBase = new GenericDAO<>(Chat.class);
            daoBase.setEntityManager(getEntityManager());

        } catch (NamingException ex) {
            Logger.getLogger(FollowUpController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ChatController() {
    }
    
    public void save() {
        try {
            daoBase.insert(chat);
            chat = null;
        } catch (SQLException ex) {
            Logger.getLogger(SatisfactionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
