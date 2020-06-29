/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.controller.ChatController;
import aes.model.Chat;
import aes.model.User;
import aes.model.Message;
import aes.persistence.GenericDAO;
import aes.utility.MessageDecoder;
import aes.utility.MessageEncoder;
import aes.utility.Secured;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;




/**
 *
 * @author bruno
 */
@ServerEndpoint(
        value="/chat/{userId}",
        decoders = MessageDecoder.class, 
        encoders = MessageEncoder.class)
public class ChatEndpoint {
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private GenericDAO<Chat> daoBase;
    private GenericDAO<Message> daoBaseMessage;

    private static Map<Long, Session> consultants = new ConcurrentHashMap<>();
    private static Map<Long, Session> users = new ConcurrentHashMap<>();
    
    private static Map<Session, Long> idleChats = new ConcurrentHashMap<>();
    private static Map<Session, Long> availableChats = new ConcurrentHashMap<>();
    private static Map<Session, Long> openChats = new ConcurrentHashMap<>();

    class UserStatusMessage{
        public String type;
        public String statusType;
        public Collection<Long> chatsIds;
    }
    
    public ChatEndpoint() {
        try {
            this.daoBase = new GenericDAO<>(Chat.class);
            this.daoBaseMessage = new GenericDAO<>(Message.class);
            System.out.println("service.ChatEndpoint.<init>()");
        } catch (NamingException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //adicionar lista de espera?
    //se não tiver nenhum consultor online o usuário ainda pode enviar mensagens mas
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Chat newChat;
        User currentUser = em.find(User.class, Long.parseLong(userId));

        if(currentUser == null) { //usuário não cadastrado
            
            newChat = new Chat();
            //newChat.setUnauthenticatedId(userId);
            newChat.setUser(null);
            newChat.setStartDate(new Date());
            
            try {
                daoBase.insert(newChat, em);
            } catch (SQLException ex) {
                Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            users.put(newChat.getId(), session);
            
            
            availableChats.put(session, newChat.getId());
            
            //UPDATE CONSULTANTS LIST
            
            
            //openChats.put(session, Long.parseLong("1"));
            
            /*
            if(consultants.size() > 0){
                sendMessageToConsultant(newChat.getId(), userId, (Long) consultants.keySet().toArray()[0]);
            }
            */
            
        } else {//consultor
           
            if(currentUser.isConsultant()) {
                if(!consultants.containsKey(currentUser.getId())){
                    consultants.put(currentUser.getId(), session);
                }
                sendUserStatus(currentUser.getId());

                
            } else {//usuário comum
                
                if( currentUser.getChat() == null ) {
                    
                    newChat = new Chat();
                    //newChat.setUnauthenticatedId("");
                    newChat.setUser(currentUser);
                    newChat.setStartDate(new Date());
                    try {
                        daoBase.insert(newChat, em);
                    } catch (SQLException ex) {
                        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    newChat = currentUser.getChat();
                }
                
                    users.put(newChat.getId(), session);
                    availableChats.put(session, newChat.getId());
                
                    
                /*
                if(consultants.size() > 0){
                    sendMessageToConsultant(newChat.getId(), userId, (Long) consultants.keySet().toArray()[0]);
                    
                }
                */
            }
        }
        
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session opened for user {0} session ID {1}", new Object[]{userId, session.getId()});
        
        //se consultor: mudar status para 'disponível' e retornar conversas anteriores(?)
        //se usuário: carregar mensagens anteriores(?) e retornar mensagem de "estamos te conectando com um consultor"
        
        /*List<Chat> chatList = em.createQuery("SELECT c FROM Chat c JOIN Message m WHERE (m.id_from=:uid OR m.id_to=:uid) ")
                                        .setParameter("uid", currentUser.getId())
                                        .getResultList();*/
        
    }
    
    private void sendUserStatus(Long consultantId){
        UserStatusMessage avb = new UserStatusMessage();
        
        avb.chatsIds = availableChats.values();
        avb.statusType= "available";
        avb.type = "statusList";
        
        ObjectMapper om = new ObjectMapper();
        String json = null;
        try {
            json = om.writeValueAsString(avb);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         try {
            consultants.get(consultantId).getBasicRemote().sendObject(json);
            //consultants.get(consultantId).getBasicRemote().sendObject(availableChats.values());
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void sendMessageToConsultant(Long chatId, String fromUser, Long consultantId) {
        try {
            Message m = new Message();
            //m.setId(Long.parseLong("1"));
            m.setChat(em.getReference(Chat.class, chatId));
            m.setContent("New conversation is open");
            m.setIdFrom(fromUser);
            m.setSentDate(new Date());
            
            openChats.put(consultants.get(consultantId), chatId);

            consultants.get(consultantId).getBasicRemote().sendObject(m);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @OnMessage
    public void onMessage(Session session, Message message) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Message received from session: {0}, messsage: {1}", new Object[]{session.getId(), message});
        try {
            daoBaseMessage.insert(message, em);
        } catch (SQLException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            for(Map.Entry<Long, Session> e: users.entrySet()) {
                if(!e.getValue().getId().equals(session.getId())){
                    e.getValue().getBasicRemote().sendObject(message);
                    System.out.println("service.ChatEndpoint.onMessage()");
                }
            }
            
            //openChats. .get(message.getChat().getId()).getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /*
    //quando o consultor seleciona um chat, manda msg pro servidor avisando quem que conectou e altera o status dos chats
    @OnMessage
    public void onMessageConnect(Session session, String message) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Message received from session: {0}, messsage: {1}", new Object[]{session.getId(), message});

        
        try {
            for(Map.Entry<Long, Session> e: users.entrySet()) {
                if(!e.getValue().getId().equals(session.getId())){
                    e.getValue().getBasicRemote().sendObject(message);
                    System.out.println("service.ChatEndpoint.onMessage()");
                }
            }
            
            //openChats. .get(message.getChat().getId()).getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    */
    /*
    @OnMessage
    public void onMessage(Session session, Message message) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Message received from session: {0}, messsage: {1}", new Object[]{session.getId(), message});
        
        
        try {
            for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                if(e.getValue().equals(message.getChat().getId()) && !e.getKey().getId().equals(session.getId())){
                    e.getKey().getBasicRemote().sendObject(message);
                    System.out.println("service.ChatEndpoint.onMessage()");
                }
            }
            //openChats. .get(message.getChat().getId()).getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        //chatEndpoints.remove(this);
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session closed ID: {0}", new Object[]{session.getId()});
        if(users.containsValue(session)){
            users.remove(getUserKeyForSession(session));
        }
        if(consultants.containsValue(session)) {
            consultants.remove(getConsultantKeyForSession(session));
        }
        
    }
    
    private Long getUserKeyForSession(Session session) {
        Long key = users.keySet().stream()
                .filter(t -> users.get(t).equals(session))
                .findAny().get();
        return key;
    }
    private Long getConsultantKeyForSession(Session session) {
        Long key = consultants.keySet().stream()
                .filter(t -> consultants.get(t).equals(session))
                .findAny().get();
        return key;
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.WARNING, "Session error. Removing session: " + session.getId(), throwable);
        try {
            session.close();
        } catch(IOException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.WARNING, "Error closing session ID: " + session.getId(), ex);
        }
    }

}
