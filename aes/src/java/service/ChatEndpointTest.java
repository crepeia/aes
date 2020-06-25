/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import aes.controller.ChatController;
import aes.model.Chat;
import aes.model.User;
import aes.model.Message;
import aes.persistence.GenericDAO;
import aes.utility.MessageDecoder;
import aes.utility.MessageEncoder;
import aes.utility.Secured;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        value="/chattest/{userId}",
        decoders = MessageDecoder.class, 
        encoders = MessageEncoder.class)
public class ChatEndpointTest {
    
    class UserInfo {
        public String name;
        public String email;
        public Long chat;
        public String status;
        public UserInfo(){};
        public UserInfo(String name, String email, Long chat, String status){
            this.name = name;
            this.email = email;
            this.chat = chat;
            this.status = status;
        }
    }
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    private GenericDAO<Chat> daoBase;
    private GenericDAO<Message> daoBaseMessage;
    
    // <UserId, Session>
    private static Map<Long, Session> consultants = new ConcurrentHashMap<>();
    // <ChatId, Session>
    private static Map<Long, Session> users = new ConcurrentHashMap<>();
    // <Session, ChatId>
    private static Map<Session, Long> openChats = new ConcurrentHashMap<>();

    
    private static Map<Session, UserInfo> onlineUsers = new ConcurrentHashMap<>();
    //private static Map<Session, String> chatStatus2 = new ConcurrentHashMap<>();

    
    class UserStatusChange{
        public String type;
        public List<UserInfo> users;
        public UserStatusChange(){
            users = new ArrayList<>();
        }
    }
    
    public enum statusType {
        AVAILABLE,
        BUSY,
        IDLE
      }
    
    public ChatEndpointTest() {
        try {
            this.daoBase = new GenericDAO<>(Chat.class);
            this.daoBaseMessage = new GenericDAO<>(Message.class);
            System.out.println("service.ChatEndpoint.<init>()");
        } catch (NamingException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Chat newChat;
        User currentUser = em.find(User.class, Long.parseLong(userId));

        if(currentUser == null){
            /*
            newChat = new Chat();
            newChat.setUnauthenticatedId(userId);
            newChat.setUser(null);
            newChat.setStartDate(new Date());
            
            try {
                daoBase.insert(newChat, em);
            } catch (SQLException ex) {
                Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            users.put(newChat.getId(), session);
            openChats.put(session, newChat.getId());
            
            UserInfo ui = new UserInfo(currentUser.getName(),
                                        currentUser.getEmail(), 
                                        currentUser.getChat().getId(), 
                                        statusType.AVAILABLE.toString());
            onlineUsers.put(session, ui);
            
            setStatus(session, statusType.AVAILABLE.toString());
           */
            //UPDATE CONSULTANTS LIST
            
            
            //openChats.put(session, Long.parseLong("1"));
            
            /*
            if(consultants.size() > 0){
                sendMessageToConsultant(newChat.getId(), userId, (Long) consultants.keySet().toArray()[0]);
            }
            */
        } else {
            if(currentUser.isConsultant()) {
            if(consultants.containsKey(currentUser.getId())){
                consultants.remove(currentUser.getId());
            }
            consultants.put(currentUser.getId(), session);
            sendUserStatusList(currentUser.getId());

        } else {//usuário comum

            if( currentUser.getChat() == null) { //primeira vez conectando

                newChat = new Chat();
                newChat.setUnauthenticatedId("");
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
            openChats.put(session, newChat.getId());
            
            UserInfo ui = new UserInfo(currentUser.getName(),
                                        currentUser.getEmail(), 
                                        currentUser.getChat().getId(), 
                                        statusType.AVAILABLE.toString());
            onlineUsers.put(session, ui);
            
            setStatus(session, statusType.AVAILABLE.toString());
            
        }
        }
        
        
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session opened for user {0} session ID {1}", new Object[]{userId, session.getId()});
        
    }
    
    private void sendUserStatusList(Long consultantId){
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "statusList";

        for(Map.Entry<Session, UserInfo> e: onlineUsers.entrySet()) {
            usl.users.add(e.getValue());
        }
        
        Gson g = new Gson();
        String json = g.toJson(usl);
        
        try {
            consultants.get(consultantId).getBasicRemote().sendObject(json);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setStatus(Session userSession, String status) {
        
        UserInfo u = onlineUsers.get(userSession);
        u.status = status;
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "statusChange";
        usl.users.add(u);
        
        Gson g = new Gson();
        String json = g.toJson(usl);
        
        try {
            for (Map.Entry<Long, Session> c : consultants.entrySet()) {
                c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void deleteUserStatus(Session userSession, Long userKey){
        
        UserInfo u = onlineUsers.get(userSession);
        
        onlineUsers.remove(userSession);
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "userOffline";
        usl.users.add(u);
        
        Gson g = new Gson();
        String json = g.toJson(usl);
        
        try {
            for (Map.Entry<Long, Session> c : consultants.entrySet()) {
                c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //quando o consultor seleciona um chat, manda msg pro servidor avisando quem que conectou e altera o status dos chats
    @OnMessage
    public void onMessage(Session session, String message) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Message received from session: {0}, messsage: {1}", new Object[]{session.getId(), message});
        System.out.println(message);

      
        try {
            
            ObjectMapper om = new ObjectMapper();
            ObjectNode node;
            node = om.readValue(message, ObjectNode.class);
            String messageType = node.get("type").asText();
            
            if(messageType.equals("connect")){
                Long chatId = node.get("chatId").asLong();
                openChats.put(session, chatId);
                
                setStatus(users.get(chatId), statusType.BUSY.toString());
                
            } else if (messageType.equals("disconnect")) {
                openChats.remove(session);
                
            } else if(messageType.equals("message")) {
                Message m = new Message();
                Chat c = new Chat();
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                m.setIdFrom(node.get("idFrom").asText());
                m.setNameFrom(node.get("nameFrom").asText());
                m.setContent(node.get("content").asText());
                m.setSentDate(format.parse(node.get("sentDate").asText()));
                c.setId(node.get("chat").asLong());
                m.setChat(c);
                
                try {
                    daoBaseMessage.insert(m, em);
                } catch (SQLException ex) {
                    Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                node.put("id", m.getId());
                
                try {
                    for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                        if(!e.getKey().getId().equals(session.getId())){
                            if(e.getValue().equals(c.getId())) {
                                e.getKey().getBasicRemote().sendObject(m);
                                System.out.println("service.ChatEndpoint.onMessage()");
                            }
                        }
                    }

                } catch (IOException | EncodeException ex) {
                    Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(ChatEndpointTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session closed ID: {0}", new Object[]{session.getId()});
        
        if(users.containsValue(session)){
            Long userKey = getUserKeyForSession(session);
            users.remove(userKey);
            deleteUserStatus(session, userKey);
        }
        
        
        if(consultants.containsValue(session)) {
            consultants.remove(getConsultantKeyForSession(session));
        }
        
        if(openChats.containsKey(session)){
            openChats.remove(session);
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
