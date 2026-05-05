package aes.service;

import aes.model.AnonymousKey;
import aes.model.AuthenticationToken;
import aes.model.Chat;
import aes.model.User;
import aes.model.Message;
import aes.persistence.AuthenticationTokenDAO;
import aes.persistence.ChatDAO;
import aes.persistence.GenericDAO;
import aes.persistence.MessageDAO;
import aes.persistence.UserDAO;
import aes.utility.MessageDecoder;
import aes.utility.MessageEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
import javax.websocket.server.ServerEndpoint;


/**
 *
 * @author bruno
 */
@ServerEndpoint(
        value="/chat/{userId}",
        configurator=ChatConfigurator.class,
        decoders = MessageDecoder.class, 
        encoders = MessageEncoder.class)
public class ChatEndpoint {
    
    class UserInfo {
        public String name;
        public String email;
        public Long chat;
        public String status;
        
        public Long idRelatedConsultant;
        public Long lastSentDate;
        public UserInfo(){};
        public UserInfo(String name, String email, Long chat, String status, Session session){
            this.name = name;
            this.email = email;
            this.chat = chat;
            this.status = status;
            this.lastSentDate = null;
        }
        
        public void setLastSentDate(Date date) {
            if(date != null) {
                this.lastSentDate = date.getTime();
            } else {
                this.lastSentDate = null;
            }
        }
    }
    
    @PersistenceContext(unitName = "aesPU")
    private EntityManager em;
    
    @EJB
    private ChatMessageService chatMessageService;
    
    private GenericDAO<Chat> daoBase;
    private UserDAO daoUser;
    private ChatDAO daoChat;
    private MessageDAO messageDAO;
    private AuthenticationTokenDAO authTokenDAO;
    private ChatFacadeREST chatFacade;
    
    private Boolean isWaiting;
    
    // <UserId, Session>
    private static Map<Long, Session> consultants = new ConcurrentHashMap<>();
    // <ChatId, Session>
    private static Map<Long, Session> users = new ConcurrentHashMap<>();
    // <Session, ChatId>
    private static Map<Session, Long> openChats = new ConcurrentHashMap<>();

    private static Map<Session, UserInfo> onlineUsers = new ConcurrentHashMap<>();
    
    private static Set<String> processedClientIds = ConcurrentHashMap.newKeySet();
    
    private static Map<Long, ScheduledExecutorService> reconnectTimers = new ConcurrentHashMap<>();
    
    // -----------------------------------------------
    // PODE TIRAR DEPOIS
    private static final ScheduledExecutorService instabilityScheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean instabilityStarted = false;
    
    private void startInstabilitySimulation() {
        if (instabilityStarted) return;
        
        instabilityStarted = true;
        
        instabilityScheduler.scheduleAtFixedRate(() -> {
            try {
//                double probability = 0.99;
                double probability = 0.01;
                double random = Math.random();
                
                if (random > probability) {
                    return;
                }
                
                System.out.println("[TEST] Instability triggered!");
                
//                boolean dropConsultant = Math.random() < 0.5;
                boolean dropConsultant = true;
                
                if (dropConsultant && !consultants.isEmpty()) {
                    Session s = consultants.values().iterator().next();
                    if (s != null && s.isOpen()) {
                        System.out.println("[TEST] Closing CONSULTANT session: " + s.getId());
                        s.close();
                    }
                } else if (!users.isEmpty()) {
                    Session s = users.values().iterator().next();
                    if (s != null && s.isOpen()) {
                        System.out.println("[TEST] Closing USER session: " + s.getId());
                        s.close();
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Instability error", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    // -----------------------------------------------

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
    }
    
    public enum statusType {
        AVAILABLE,
        BUSY,
        IDLE,
        TEMP_OFFLINE,
        OFFLINE,
    }
    
    public ChatEndpoint() {
        try {
            this.daoBase = new GenericDAO<>(Chat.class);
            this.daoUser = new UserDAO();
            this.daoChat = new ChatDAO();
            this.messageDAO = new MessageDAO();
            authTokenDAO = new AuthenticationTokenDAO();
            this.isWaiting = true;
            System.out.println("service.ChatEndpoint.<init>()");
        } catch (NamingException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private User validateTokenAndGetLoggedUser(String token) throws Exception {
        try {
            if (token == null || token.isEmpty()) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] INVALID_TOKEN reason=INVALID_TOKEN_PARAM");
                
                return null;
            }
            
            return (User) daoUser.findUserByToken(token, em);
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return null;
        }
    }

    private boolean isRelatedConsultantOnline(User consultant) {
        try {
            if (consultant == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.INFO,
                        "[INFO] NULL_CONSULTANT reason=NULL_USER_PARAM");
                
                // true if the user does not have a consultant
                return true;
            }
            
            // true if consultant is online, false if consultant is not online
            return consultants.containsKey(consultant.getId());
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
            return false; // fallback
        }
    }
    
    private void addOnlineUser(Session session, UserInfo ui) {
        try {
            if (session == null || ui == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_USERINFO");
                
                return;
            }
            
            // REMOVE DUPLICATES
            // any entry with the same chatId is removed before adding a new user
            onlineUsers.entrySet().removeIf(entry -> entry.getValue().chat.equals(ui.chat));
            
            onlineUsers.put(session, ui);
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private boolean verifyRelatedUser(Long consultantIdRelatedToUser, Long idConsultant) {
        return (Objects.equals(consultantIdRelatedToUser, idConsultant) || Objects.equals(consultantIdRelatedToUser, null));
    }
    
    private List<Chat> findAllRelatedUserChats(Long consultantId) {
        List<Chat> chats = null;
        try {
            chats = daoChat.listAllRelatedUserChats(consultantId, em);
        } catch (SQLException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return chats;
    }
    
    private void sendUserStatusList(Long consultantId){
        Logger.getLogger(ChatEndpoint.class.getName())
            .log(Level.INFO,
                "[INFO] Sending status list to the consultant={0}", consultantId);
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "statusList";

        for (Map.Entry<Session, UserInfo> e: onlineUsers.entrySet()) {
            // Exposes to the consultant the user who is directly related to him or who is not related to anyone
            if(!consultants.containsValue(e.getKey()) && verifyRelatedUser(e.getValue().idRelatedConsultant, consultantId))
                usl.users.add(e.getValue());
        }
        
        List<Chat> chats = findAllRelatedUserChats(consultantId);
        
        for (Chat chat : chats) {
            if (chat.getUser() == null) {
                continue;
            }
            
            boolean hasMessages = false;
            hasMessages = messageDAO.existsMessageInChat(chat.getId(), em);
            if (!hasMessages) {
                continue;
            }
            
            Long userId = chat.getUser().getId();
            boolean isOnline = onlineUsers.values().stream().anyMatch(u -> u.chat.equals(chat.getId()));
            
            if (!isOnline) {
                String realStatus = statusType.OFFLINE.toString();
                String email = chat.getUser().getEmail();
                UserInfo offlineUser = new UserInfo(
                    chat.getUser().getName(), email, chat.getId(), realStatus, null
                );
                
                Date lastSentDate = messageDAO.findLastSentDateByChatId(chat.getId(), em);
                if (lastSentDate != null) {
                    offlineUser.setLastSentDate(lastSentDate);
                }
                
                usl.users.add(offlineUser);
            }
        }
        
        Gson g = new Gson();
        String json = g.toJson(usl);
        System.out.println("JSON final enviado: " + json);
        
        try {
            consultants.get(consultantId).getBasicRemote().sendObject(json);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                // Exposes to the consultant the user who is directly related to him or who is not related to anyone
                if(verifyRelatedUser(u.idRelatedConsultant, c.getKey()))
                    c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendNewUserChatId(Session session, Long chatId){
        GenericMessage gm = new GenericMessage();
        gm.type = "chatid";
        gm.value = String.valueOf(chatId);
        
        Gson g = new Gson();
        String json = g.toJson(gm);
        
        try {
            session.getBasicRemote().sendObject(json);
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void scheduleOfflineCheck(Long chatId) {
        if (chatId == null) return;

        if (reconnectTimers.containsKey(chatId)) {
            reconnectTimers.get(chatId).shutdownNow();
        }
        
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        scheduler.schedule(() -> {
            try {
                Session userSession = users.get(chatId);
                
                if (userSession == null) {
                    System.out.println("[OFFLINE DEFINITIVO] chatId=" + chatId);
                    
                    for (Map.Entry<Session, UserInfo> entry : onlineUsers.entrySet()) {
                        if (entry.getValue().chat.equals(chatId)) {
                            setStatus(entry.getKey(), statusType.OFFLINE.toString());
                            break;
                        }
                    }
                    
                    disconnectConsultantsFromUser(null, chatId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 15, TimeUnit.SECONDS);
        
        reconnectTimers.put(chatId, scheduler);
    }
    
    // This method is responsible for creating the user's chat and populating the lists
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            if (session == null || config == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.SEVERE,
                        "[SECURITY] NULL_PARAM reason=NULL_SESSION_OR_CONFIG");
                
                return;
            }
            
            // -----------------------------------------------
            // PODE TIRAR DEPOIS
            startInstabilitySimulation();
            // -----------------------------------------------
            
            List<String> auth = (List<String>) config.getUserProperties().get("auth");

            if (auth == null || !Boolean.parseBoolean(auth.get(0))) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] UNAUTHORIZED_ACCESS reason=NULL_AUTH_PARAM " +
                            "sessionId={0}", session.getId());
                
                onError(session, new Throwable("NULL_AUTH_PARAM"));
                return;
            }
            
            List<String> token = (List<String>) config.getUserProperties().get("authtoken");
            
            if (token == null || token.isEmpty()) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] UNAUTHORIZED_ACCESS reason=NULL_TOKEN " +
                            "sessionId={0}", session.getId());
                
                onError(session, new Throwable("NULL_TOKEN"));
                return;
            }
            
            User currentUser = validateTokenAndGetLoggedUser(token.get(0));
            
            if (currentUser == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[SECURITY] UNAUTHORIZED_ACCESS reason=NULL_USER_OR_NOT_AUTHENTICATED " +
                            "sessionId={0}", session.getId());
                
                onError(session, new Throwable("NULL_USER_OR_NOT_AUTHENTICATED"));
                return;
            }
            
            session.setMaxIdleTimeout(60 * 60 * 1000); // Set a 60-minute timeout to close
            
            UserInfo ui = new UserInfo();
            Chat newChat;
            
            if (currentUser.isConsultant()) {
                if (consultants.containsKey(currentUser.getId())) {
                    consultants.get(currentUser.getId()).close();
                }
                
                ui.name = currentUser.getName();
                ui.email = currentUser.getEmail();
                ui.chat = currentUser.getChat().getId();
                ui.status = "";
                ui.idRelatedConsultant = null;
                
                addOnlineUser(session, ui);
                consultants.put(currentUser.getId(), session);
                
                // Giving time for the user function to pick up the consultant
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
                
                sendUserStatusList(currentUser.getId());
            } else { // Comum and Anonymous Users
                if (currentUser.getChat() == null || currentUser.getChat().getId() == null) {
                    Logger.getLogger(ChatEndpoint.class.getName())
                        .log(Level.WARNING, "User {0} with no linked chat. Creating a new one.", currentUser.getId());
                    
                    newChat = new Chat();
                    newChat.setUser(currentUser);
                    newChat.setStartDate(new Date());
                    
                    daoBase.insert(newChat, em);
                } else {
                    newChat = currentUser.getChat();
                }
                
                if (users.containsKey(newChat.getId())) {
                    users.get(newChat.getId()).close();
                }
                
                final User _currentUser = currentUser;
                
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    if ((consultants.isEmpty() || !isRelatedConsultantOnline(_currentUser.getRelatedConsultant())) && session.isOpen()) {
                        this.isWaiting = false;
                        sendNoConsultantMessage(session);
                    }
                }, 4, TimeUnit.MINUTES);
                
                users.put(newChat.getId(), session);
                String realStatus = statusType.OFFLINE.toString();
                
                ui.name = currentUser.getName();
                ui.email = currentUser.getEmail();
                ui.chat = newChat.getId();
                ui.status = realStatus;
                
                if (currentUser.getRelatedConsultant() == null) ui.idRelatedConsultant = null;
                else ui.idRelatedConsultant = currentUser.getRelatedConsultant().getId();
                
                Date lastSentDate = messageDAO.findLastSentDateByChatId(newChat.getId(), em);
                if (lastSentDate != null) {
                    ui.setLastSentDate(lastSentDate);
                }
                
                openChats.put(session, newChat.getId());
                addOnlineUser(session, ui);
                setStatus(session, realStatus);
                
                // just anonymous
                if (currentUser.getEmail() == null) sendNewUserChatId(session, newChat.getId());
            }
            
            Logger.getLogger(ChatEndpoint.class.getName())
                .log(Level.INFO, "Session opened for user {0} session ID {1}", new Object[]{currentUser.getId(), session.getId()});
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    private void deleteUserStatus(Session userSession, Long userKey){
        try {
            if (userSession == null || userKey == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_USERKEY");
                
                return;
            }
            
            UserInfo u = onlineUsers.get(userSession);
            
            if (u == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_USER_INFO reason=NULL_USER_INFO_FOR_SESSION userSessionId={0}", userSession.getId());
                
                return;
            }
            
            u.status = statusType.OFFLINE.toString();
            
            onlineUsers.remove(userSession);
            
            UserStatusChange usl = new UserStatusChange();
            usl.type = "statusChange";
            usl.users.add(u);
            
            Gson g = new Gson();
            String json = g.toJson(usl);
            
            for (Map.Entry<Long, Session> c : consultants.entrySet()) {
                c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    void consultantDisconnectTimeout(Long chatId){
        try {
            if (chatId == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_CHAT_ID");
                
                return;
            }
            
            UserStatusChange usl = new UserStatusChange();
            usl.type = "setTimeout";
            
            Gson g = new Gson();
            String json = g.toJson(usl);
            
            for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                if(e.getValue().equals(chatId)){
                    e.getKey().getBasicRemote().sendObject(json);
                }
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    void consultantConnectTimeout(Long chatId){
        try {
            if (chatId == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_CHAT_ID");
                
                return;
            }
            
            UserStatusChange usl = new UserStatusChange();
            usl.type = "unsetTimeout";
            
            Gson g = new Gson();
            String json = g.toJson(usl);
            
            for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                if(e.getValue().equals(chatId)){
                    e.getKey().getBasicRemote().sendObject(json);
                }
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    void disconnectConsultantsFromUser(Session userSession, Long chatId){
        try {
            if (chatId == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_CHATID");
                
                return;
            }
            
            for (Map.Entry<Session, Long> e: openChats.entrySet()) {
                if (e.getValue().equals(chatId) && !e.getKey().equals(userSession)){
                    openChats.remove(e.getKey());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    // When the consultant selects a chat, they send a message to the server notifying them who has connected and change the chat status
    @OnMessage
    public void onMessage(Session session, String message) throws SQLException, EncodeException {
        try {
            if (session == null || message == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_MESSAGE");
                
                return;
            }
            
            Logger.getLogger(ChatEndpoint.class.getName())
                .log(Level.INFO, "Message received from session: {0}, message: {1}",
                    new Object[]{session.getId(), message});
            
            ObjectMapper om = new ObjectMapper();
            ObjectNode node;
            node = om.readValue(message, ObjectNode.class);
            String messageType = node.get("type").asText();
            
            if (!this.isWaiting && consultants.isEmpty()) {
                sendNoConsultantMessage(session);
            }
            
            if (messageType.equals("connect")){
                Long consultantId;
                Long userId;
                User user;
                Session userSession;

                Long chatId = node.get("chatId").asLong();
                openChats.put(session, chatId);
                consultantConnectTimeout(openChats.get(session));
                
                Chat chat = daoChat.find(chatId, em);
                
                if (chat == null) {
                    Logger.getLogger(ChatEndpoint.class.getName())
                        .log(Level.SEVERE, "Chat does not exist for the chatId={0}", chatId);
                    return;
                }
                
                User chatUser = chat.getUser();
                
                if (chatUser == null) {
                    Logger.getLogger(ChatEndpoint.class.getName())
                        .log(Level.SEVERE, "User does not exist for the chatId={0}", chatId);
                    return;
                }
                
                userId = chatUser.getId();
                consultantId = getConsultantKeyForSession(session);
                user = daoUser.find(userId, em);
                
                if (user.getRelatedConsultant() == null) {
                    user.setRelatedConsultant(daoUser.find(consultantId, em));

                    daoUser.update(user, em);
                    
                    userSession = users.get(chatId);
                    
                    // It only updates when the user is online
                    if (userSession != null) {
                        if (onlineUsers.get(userSession) != null) {
                            onlineUsers.get(userSession).idRelatedConsultant = user.getRelatedConsultant().getId();
                        }
                    }
                }
                
                if (users.get(chatId) != null) {
                    setStatus(users.get(chatId), statusType.BUSY.toString());
                } else {
                    Logger.getLogger(ChatEndpoint.class.getName())
                        .log(Level.INFO, "It must be a consultant accessing an offline user on the chatId={0}", chatId);
                }   
            } 
            
            else if (messageType.equals("disconnect")) {
                Long chatId = openChats.get(session);
                
                if (chatId == null) {
                    Logger.getLogger(ChatEndpoint.class.getName())
                        .log(Level.WARNING, "[WARNING] DISCONNECT_WITHOUT_CHAT sessionId={0}", session.getId());
                    return;
                }
                
                consultantDisconnectTimeout(chatId);
                openChats.remove(session);    
            }
            
            else if (messageType.equals("statusAvailable")) {
                Long chatId = node.get("chatId").asLong();
                
                if (reconnectTimers.containsKey(chatId)) {
                    reconnectTimers.get(chatId).shutdownNow();
                    reconnectTimers.remove(chatId);
                }
                
                setStatus(users.get(chatId), statusType.AVAILABLE.toString());
                openChats.put(session, chatId);
            }
            
            else if (messageType.equals("statusOffline")){
                Long chatId = node.get("chatId").asLong();
                
                System.out.println("[TEMP OFFLINE] chatId=" + chatId);
                
                setStatus(users.get(chatId), statusType.OFFLINE.toString());
                
                openChats.remove(session);
                
                scheduleOfflineCheck(chatId);
            } 
            
            else if (messageType.equals("statusIdle")){
                Long chatId = node.get("chatId").asLong();
                setStatus(users.get(chatId), statusType.IDLE.toString()); 
            }
            
            else if(messageType.equals("message")) {
                String clientId = node.get("clientId").asText();
                
                if (processedClientIds.contains(clientId)) {
                    ObjectNode ack = om.createObjectNode();
                    ack.put("type", "ack_message");
                    ack.put("clientId", clientId);

                    session.getBasicRemote().sendText(ack.toString());

                    System.out.println("[ACK] Duplicate clientId, ack resent: " + clientId);
                    return;
                }
                processedClientIds.add(clientId);
                
                Message m = new Message();
                Chat c = new Chat();
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                m.setIdFrom(node.get("idFrom").asText());
                m.setNameFrom(node.get("nameFrom").asText());
                m.setContent(node.get("content").asText());
                m.setSentDate(format.parse(node.get("sentDate").asText()));
                m.setReceived(false);
                
                c.setId(node.get("chat").asLong());
                m.setChat(c);

                messageDAO.insert(m, em);
                
                node.put("id", m.getId());
                
                for(Map.Entry<Session, Long> e: openChats.entrySet()) {
                    if(!e.getKey().getId().equals(session.getId())){
                        if(e.getValue().equals(c.getId())) {
                            e.getKey().getBasicRemote().sendObject(m);
                            System.out.println("service.ChatEndpoint.onMessage()");
                        }
                    }
                }
                
                ObjectNode ack = om.createObjectNode();
                ack.put("type", "ack_message");
                ack.put("clientId", clientId);
                ack.put("serverId", m.getId());

                session.getBasicRemote().sendText(ack.toString());
            }
            
            else if (messageType.equals("ping")) {
                session.getBasicRemote().sendText("{\"type\":\"pong\"}");
                System.out.println("[INFO] ping -> pong");
            }
            
            else if (messageType.equals("ack")) {
                Long messageId = node.get("messageId").asLong();
                chatMessageService.markAsReceived(messageId);
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
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
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        try {
            if (session == null || reason == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_REASON");
                
                return;
            }
            
            Logger.getLogger(ChatEndpoint.class.getName())
                .log(Level.INFO, "Session closed sessionId={0} reason={1}", new Object[]{session.getId(), reason});
            
            if(users.containsValue(session) && getUserKeyForSession(session) != null) {
                Long userKey = getUserKeyForSession(session);
                users.remove(userKey);
                deleteUserStatus(session, userKey);
            }
            
            if(consultants.containsValue(session)) {
                Long userKey = getConsultantKeyForSession(session);
                consultants.remove(userKey);
                deleteUserStatus(session, userKey);
            }
            
            if(openChats.containsKey(session)){
                openChats.remove(session);
            }
        } catch (Exception ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            if (session == null || throwable == null) {
                Logger.getLogger(ChatEndpoint.class.getName())
                    .log(Level.WARNING,
                        "[WARNING] NULL_PARAM reason=NULL_SESSION_OR_THROWABLE");
                
                return;
            }
            
            Logger.getLogger(ChatEndpoint.class.getName())
                .log(Level.WARNING, "Session error. Removing session. sessionId={0} throwable={1}", new Object[]{session.getId(), throwable});
            
            session.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, "Error type: ", ex);
        }
    }
}
