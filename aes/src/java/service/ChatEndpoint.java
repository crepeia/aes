/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import javax.websocket.CloseReason;
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
@ServerEndpoint("/chat/{username}")
public class ChatEndpoint {
    
    
    @OnMessage
    public String onMessage(Session session, String message) {
        return null;
    }
    /*
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        
    }
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {
        //se consultor: mudar status para 'disponível' e retornar conversas anteriores(?)
        //se usuário: carregar mensagens anteriores(?) e retornar mensagem de "estamos te conectando com um consultor"
    }
    
    
    @OnError
    public void onError(Session session) {
        
    }
*/
}
