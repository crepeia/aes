/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.service;

import java.util.List;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 *
 * @author bruno
 */
public class ChatConfigurator extends ServerEndpointConfig.Configurator {
    
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, 
                                HandshakeRequest request, 
                                HandshakeResponse response) {
        
        Map<String,List<String>> headers = request.getHeaders();
        
        sec.getUserProperties().put("cookie", headers.get("cookie"));
    }
}
