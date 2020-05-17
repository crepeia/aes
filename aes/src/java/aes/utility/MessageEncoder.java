/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;
import aes.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author bruno
 */
public class MessageEncoder implements Encoder.Text<Message> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleModule module = new SimpleModule("CustomMessageSerializer");
    @Override
    public String encode(Message message) throws EncodeException {
        try {
            module.addSerializer(Message.class, new CustomMessageSerializer());
            objectMapper.registerModule(module);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MessageEncoder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
 
    @Override
    public void init(EndpointConfig endpointConfig) {
        module.addSerializer(Message.class, new CustomMessageSerializer());
        objectMapper.registerModule(module);
    }
 
    @Override
    public void destroy() {
        // Close resources
    }
}
