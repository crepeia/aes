/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import aes.model.AuthenticationToken;
import aes.model.Chat;
import aes.model.User;
import aes.model.Message;
import aes.persistence.GenericDAO;
import aes.utility.MessageDecoder;
import aes.utility.MessageEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;




/**
 *
 * @author bruno
 */
@ServerEndpoint(
        value="/chattest/{userId}",
        configurator=ChatConfigurator.class,
        decoders = MessageDecoder.class, 
        encoders = MessageEncoder.class)
public class ChatEndpointTest {
    
    class UserInfo {
        public String name;
        public String email;
        public Long chat;
        public String status;
        //public transient Timer timer;
        //public transient ScheduledExecutorService pingExecutorService;
        //public transient Session session;
        public UserInfo(){};
        public UserInfo(String name, String email, Long chat, String status, Session session){
            this.name = name;
            this.email = email;
            this.chat = chat;
            this.status = status;
            //this.session = session;
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
    
    class GenericMessage{
        public String type;
        public String value;
        //public Long chatId;
    }
    
    public enum statusType {
        AVAILABLE,
        BUSY,
        IDLE,
        OFFLINE
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
    
    private AuthenticationToken validateToken(String token) throws Exception {
        return (AuthenticationToken) em.createQuery("SELECT a FROM AuthenticationToken a WHERE a.token=:t").setParameter("t", token).getSingleResult();
    }
    
    /*
    private void schedulePingMessages(UserInfo newUserConnection) {
    newUserConnection.pingExecutorService = Executors.newScheduledThreadPool(1); 
    newUserConnection.pingExecutorService.scheduleAtFixedRate(() -> {
        scheduleDiconnection(newUserConnection);
        try {
            String data = "Ping";
            ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
            newUserConnection.session.getBasicRemote().sendPing(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }, 300, 300, TimeUnit.SECONDS);
}
    private void scheduleDiconnection(UserInfo user) {
    user.timer = new Timer();
    user.timer.schedule(new TimerTask() {
        @Override
        public void run() {
            try {
                user.session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Client does not respond"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }, 5000);
    
    
    }
    */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("userId") String userId) {
        List<String> auth = (List<String>) config.getUserProperties().get("auth");
        List<String> unauthId = null;
        
        User currentUser = null;
        AuthenticationToken at;
        Chat newChat;
        
        if(Boolean.parseBoolean(auth.get(0))){
            List<String> token = (List<String>) config.getUserProperties().get("authtoken");
            try {
                
                at = validateToken(token.get(0));
                currentUser = at.getUser();
                if(currentUser.getId() != Long.parseLong(userId)) 
                    throw new Exception();
                
            } catch (Exception ex) {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Error validating user identity."));
                    return;
                } catch (IOException ex1) {
                    Logger.getLogger(ChatEndpointTest.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } else {
            unauthId = (List<String>) config.getUserProperties().get("unauthID");
            
            if(unauthId.isEmpty() || unauthId.get(0).isEmpty()){
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized user."));
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(ChatEndpointTest.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        
        UserInfo ui = new UserInfo();
        
        if(currentUser == null){
            if(consultants.isEmpty()){
                sendNoConsultantMessage(session);
                return;
            }
            newChat = new Chat();
            newChat.setUser(null);
            newChat.setStartDate(new Date());
            newChat.setUnauthenticatedId(unauthId.get(0));
            try {
                daoBase.insert(newChat, em);
            } catch (SQLException ex) {
                Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            users.put(newChat.getId(), session);
            String realStatus = statusType.AVAILABLE.toString();
            
            ui.name = "Anônimo";
            ui.email = "";
            ui.chat = newChat.getId();
            ui.status = realStatus;
            //ui.session = session;
            
            openChats.put(session, newChat.getId());
            onlineUsers.put(session, ui);
            setStatus(session, realStatus);
            sendNewUserChatId(session, newChat.getId());
            
        } else {
            if(currentUser.isConsultant()) {
                if(consultants.containsKey(currentUser.getId())){
                    try {
                        consultants.get(currentUser.getId()).close();
                    } catch (IOException ex) {
                        Logger.getLogger(ChatEndpointTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                ui.name = currentUser.getName();
                ui.email = currentUser.getEmail();
                ui.chat = currentUser.getChat().getId();
                ui.status = "";
                //ui.session = session;
                
                onlineUsers.put(session, ui);
                consultants.put(currentUser.getId(), session);
                sendUserStatusList(currentUser.getId());

            } else {//usuário comum

                if( currentUser.getChat() == null) { //primeira vez conectando

                    newChat = new Chat();
                    newChat.setUser(currentUser);
                    newChat.setStartDate(new Date());
                    newChat.setUnauthenticatedId(null);
                    try {
                        daoBase.insert(newChat, em);
                    } catch (SQLException ex) {
                        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    newChat = currentUser.getChat();
                }
                if(users.containsKey(newChat.getId())){
                    try {
                        users.get(newChat.getId()).close();
                    } catch (IOException ex) {
                        Logger.getLogger(ChatEndpointTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                users.put(newChat.getId(), session);
                //String realStatus = statusType.AVAILABLE.toString();
                String realStatus = statusType.OFFLINE.toString();

                //if(openChats.containsValue(newChat.getId())){ //um consultor estava atendendo o chat
                                                                //e o usuário desconectou/voltou
                //    realStatus = statusType.BUSY.toString();
                //}
                
                ui.name = currentUser.getName();
                ui.email = currentUser.getEmail();
                ui.chat = currentUser.getChat().getId();
                ui.status = realStatus;
                //ui.session = session;
                

                openChats.put(session, newChat.getId());
                onlineUsers.put(session, ui);

                setStatus(session, realStatus);

            }
        }
        
       
        //schedulePingMessages(ui);
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session opened for user {0} session ID {1}", new Object[]{userId, session.getId()});
    }
    
    private void sendNoConsultantMessage(Session session){
        
        GenericMessage gm = new GenericMessage();
        gm.type = "noConsultants";
        gm.value = "";
        
        Gson g = new Gson();
        String json = g.toJson(gm);
        
        try {
            session.getBasicRemote().sendObject(json);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void sendNewUserChatId(Session session, Long chatId){
        
        GenericMessage gm = new GenericMessage();
        gm.type = "chatid";
        gm.value = String.valueOf(chatId);//chatId;
        
        Gson g = new Gson();
        String json = g.toJson(gm);
        
        try {
            session.getBasicRemote().sendObject(json);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendUserStatusList(Long consultantId){
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "statusList";

        for(Map.Entry<Session, UserInfo> e: onlineUsers.entrySet()) {
            if(!consultants.containsValue(e.getKey()))
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
            userSession.getBasicRemote().sendObject(json);
            for (Map.Entry<Long, Session> c : consultants.entrySet()) {
                c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void deleteUserStatus(Session userSession, Long userKey){
        
        UserInfo u = onlineUsers.get(userSession);
        u.status = statusType.OFFLINE.toString();
        
        onlineUsers.remove(userSession);
    
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
    
    
    /*
    @OnMessage
    public void onPong(PongMessage pongMessage, Session session) {
        //String sourceSessionId = session.getId();
        UserInfo user = onlineUsers.get(session);
        user.timer.cancel();
        user.timer.purge();
    }
*/
    
    void consultantDisconnectTimeout(Long chatId){
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "setTimeout";

        
        Gson g = new Gson();
        String json = g.toJson(usl);
            
        try {
            for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                if(e.getValue().equals(chatId)){
                    e.getKey().getBasicRemote().sendObject(json);
                    System.out.println("service.ChatEndpoint.onMessage()");

                }
            }

        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void consultantConnectTimeout(Long chatId){
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "unsetTimeout";

        
        Gson g = new Gson();
        String json = g.toJson(usl);
            
        try {
            for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                if(e.getValue().equals(chatId)){
                    e.getKey().getBasicRemote().sendObject(json);
                    System.out.println("service.ChatEndpoint.onMessage()");

                }
            }

        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void disconnectConsultantsFromUser(Session userSession, Long chatId){

        for(Map.Entry<Session, Long> e: openChats.entrySet()) {
            if(e.getValue().equals(chatId) && !e.getKey().equals(userSession)){
                openChats.remove(e.getKey());
            }
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
                consultantConnectTimeout(openChats.get(session));
                
                
                setStatus(users.get(chatId), statusType.BUSY.toString());
                
            } else if (messageType.equals("disconnect")) {
                consultantDisconnectTimeout(openChats.get(session));
                openChats.remove(session);
                
            } else if (messageType.equals("statusAvailable")) {
                Long chatId = node.get("chatId").asLong();
                setStatus(users.get(chatId), statusType.AVAILABLE.toString());
               
                openChats.put(session, chatId);

            } else if (messageType.equals("statusOffline")){
                Long chatId = node.get("chatId").asLong();
                setStatus(users.get(chatId), statusType.OFFLINE.toString());
                consultantConnectTimeout(chatId);
                openChats.remove(session);
                
                disconnectConsultantsFromUser(session, chatId);
                
            } else if (messageType.equals("statusIdle")){
                Long chatId = node.get("chatId").asLong();
                setStatus(users.get(chatId), statusType.IDLE.toString());
                
                disconnectConsultantsFromUser(session, chatId);
                
            }else if(messageType.equals("message")) {
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
            //consultantConnectTimeout(userKey);
            deleteUserStatus(session, userKey);
            
            users.remove(userKey);
            
            onlineUsers.remove(session);
            
        }
        
        
        if(consultants.containsValue(session)) {
            Long userKey = getConsultantKeyForSession(session);
            deleteUserStatus(session, userKey);

            consultants.remove(userKey);
            onlineUsers.remove(session);
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
