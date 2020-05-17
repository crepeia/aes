/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;
import aes.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 *
 * @author bruno
 */
public class MessageDecoder implements Decoder.Text<Message>{
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleModule module = new SimpleModule("CustomMessageDeserializer");
    
    @Override
    public Message decode(String message) throws DecodeException {
        try {
            module.addDeserializer(Message.class, new CustomMessageDeserializer());
        objectMapper.registerModule(module);
            return objectMapper.readValue(message, Message.class);
        } catch (IOException ex) {
            Logger.getLogger(MessageDecoder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean willDecode(String message) {
        return (message != null);
        //verificar se pode ser decodificado
    }

    @Override
    public void init(EndpointConfig config) {
        module.addDeserializer(Message.class, new CustomMessageDeserializer());
        objectMapper.registerModule(module);
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
