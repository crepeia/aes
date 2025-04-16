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
import aes.persistence.ChatDAO;
import aes.persistence.GenericDAO;
import aes.persistence.UserDAO;
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
import java.util.Objects;
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
        //public transient Timer timer;
        //public transient ScheduledExecutorService pingExecutorService;
        //public transient Session session;
        public Long idRelatedConsultant;
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
    private UserDAO daoUser;
    private ChatDAO daoChat;
    private ChatFacadeREST chatFacade;
    
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
        OFFLINE,
      }
    
    public ChatEndpoint() {
        try {
            this.daoBase = new GenericDAO<>(Chat.class);
            this.daoBaseMessage = new GenericDAO<>(Message.class);
            this.daoUser = new UserDAO();
            this.daoChat = new ChatDAO();
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

    private boolean isRelatedConsultantOnline(User consultant) {
        if(!Objects.equals(consultant, null)) {
            //true se consultor online, false se consultor não está online
            return consultants.containsKey(consultant.getId());
        }
        //Se usuario não tem consultor
        return true;
    }
    
    //Esse método está responsável pela criação do chat do usuário e o preenchimento das listas.
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
                    Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } else {
            unauthId = (List<String>) config.getUserProperties().get("unauthID");
            
            if(unauthId.isEmpty() || unauthId.get(0).isEmpty()){
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized user."));
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        
        session.setMaxIdleTimeout(5 * 60 * 1000); // Define timeout de 5 minutos para fechar.
        
        UserInfo ui = new UserInfo();
        
        if(currentUser == null){            
            // Enquanto não houver consultores ou não tiver passado 4 minutos programa fica em pausa.
            // 4 minutos = 0,1 segundo * 10 * 60 * 4.
            for(int i = 0; i < 2400 && consultants.isEmpty(); i++) {
                try {
                    Thread.sleep(100); // 0,1 segundo
                } catch (InterruptedException e) {
                    return;
                }
            }
            
            // Se consultants ainda estiver vazia após 4 minutos, manda a mensagem noConsultant e retorna
            if(consultants.isEmpty()) {
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
            ui.idRelatedConsultant = null;
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
                        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                ui.name = currentUser.getName();
                ui.email = currentUser.getEmail();
                ui.chat = currentUser.getChat().getId();
                ui.status = "";
                ui.idRelatedConsultant = null;
                //ui.session = session;
                
                onlineUsers.put(session, ui);
                consultants.put(currentUser.getId(), session);
                
                // Dando tempo para a função do usuário pegar o consultor
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
                
                sendUserStatusList(currentUser.getId());

            } else {//usuário comum
                
                // Enquanto não houver consultores, o consultor do usuário não estiver online ou não tiver passado 4 minutos programa fica em pausa.
                // 4 minutos = 0,1 segundo * 10 * 60 * 4.
                try {
                    for(int i = 0; i < 2400 && (consultants.isEmpty() || !isRelatedConsultantOnline(currentUser.getRelatedConsultant())); i++) {
                        Thread.sleep(100); // 0,1 segundo.
                    }
                } catch (InterruptedException e) {
                    return;
                }
                
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
                        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
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
                //É usuário comum e o idRelatedConsultant pode ser nulo (não tem consultor)
                //ou não (tem consultor associado)
                if(currentUser.getRelatedConsultant() == null) {
                    ui.idRelatedConsultant = null;
                } else {
                    ui.idRelatedConsultant = currentUser.getRelatedConsultant().getId();
                }
                //ui.session = session;


                openChats.put(session, newChat.getId());
                onlineUsers.put(session, ui);

                setStatus(session, realStatus);
                
                // Se consultants ainda estiver vazia ou o consultor do usuário não estiver online após 4 minutos, manda a mensagem noConsultant e retorna
                if(consultants.isEmpty() || !isRelatedConsultantOnline(currentUser.getRelatedConsultant())){
                    sendNoConsultantMessage(session);
                    return;
                }
                
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
        System.out.println("Enviando lista de status para o consultor: " + consultantId);
        
        UserStatusChange usl = new UserStatusChange();
        usl.type = "statusList";

        for(Map.Entry<Session, UserInfo> e: onlineUsers.entrySet()) {
            //Expõe ao consultor o usuário que não for consultor e que estiver
            //diretamente relacionado a ele (que seja seu paciente) ou que não for paciente de ninguém
            if(!consultants.containsValue(e.getKey()) && verifyRelatedUser(e.getValue().idRelatedConsultant, consultantId))
                usl.users.add(e.getValue());
        }
        System.out.println("Usuários online adicionados: " + usl.users.size());
        
        // Buscar chats do banco de dados para identificar usuários offline
        List<Chat> chats = findAllRelatedUserChats(consultantId);
        
        // Para cada chat cria um novo UserInfo se o usuário não estiver online
        for (Chat chat : chats) {
            if (chat.getUser() == null) {
                System.out.println("Chat " + chat.getId() + " ignorado (sem usuário associado)");
                continue;
            }
            
            Long userId = chat.getUser().getId();
            boolean isOnline = onlineUsers.values().stream().anyMatch(u -> u.chat.equals(chat.getId()));
            
            if (!isOnline) {
                String realStatus = statusType.OFFLINE.toString();
                UserInfo offlineUser = new UserInfo(
                    chat.getUser().getName(), chat.getUser().getEmail(), chat.getId(), realStatus, null
                );
                usl.users.add(offlineUser);
                System.out.println("Usuário OFFLINE adicionado: " + offlineUser.name);
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
                //Expõe ao consultor o usuário que não for consultor e que estiver
                //diretamente relacionado a ele (que seja seu paciente) ou que não for paciente de ninguém
                if(verifyRelatedUser(u.idRelatedConsultant, c.getKey()))
                    c.getValue().getBasicRemote().sendObject(json);
            }
        } catch (IOException | EncodeException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
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
            if(consultants.isEmpty()){
                sendNoConsultantMessage(session);
            }
            
            if(messageType.equals("connect")){
                Long consultantId;
                Long userId;
                User user;
                Session userSession;

                Long chatId = node.get("chatId").asLong();
                openChats.put(session, chatId);
                consultantConnectTimeout(openChats.get(session));
                
                //Verifica se não é um usuário anônimo
                if(daoChat.find(chatId, em).getUser() != null) {
                    userId = daoChat.find(chatId, em).getUser().getId();
                    consultantId = getConsultantKeyForSession(session);
                    user = daoUser.find(userId, em);
                    //Ao estabelecer a conexão, o consultor vai ser definido como consultor associado
                    //ao paciente do chat, caso o usuário não seja paciente de nenhum consultor
                    if(user.getRelatedConsultant() == null) {
                        user.setRelatedConsultant(daoUser.find(consultantId, em));
                        try {
                            daoUser.update(user, em);
                        } catch (SQLException ex) {
                            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        userSession = users.get(chatId);
                        onlineUsers.get(userSession).idRelatedConsultant = user.getRelatedConsultant().getId();
                    }
                }
                
                if(users.get(chatId) != null) {
                    setStatus(users.get(chatId), statusType.BUSY.toString());
                } else {
                    System.out.println("Deve ser um consultor acessando um usuário offline no chat: " + chatId);
                }
                
                
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
                
            } else if (messageType.equals("ping")) {
                System.out.println("ping");
            }
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(ChatEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Logger.getLogger(ChatEndpoint.class.getName()).log(Level.INFO, "Session closed ID: {0}", new Object[]{session.getId()});
        System.out.println(reason);
        
        if(users.containsValue(session)){
            Long userKey = getUserKeyForSession(session);
            //consultantConnectTimeout(userKey);
            
            users.remove(userKey);
            deleteUserStatus(session, userKey);

            //onlineUsers.remove(session);
            
        }
        
        
        if(consultants.containsValue(session)) {
            Long userKey = getConsultantKeyForSession(session);
            consultants.remove(userKey);
            deleteUserStatus(session, userKey);

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
