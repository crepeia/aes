/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;
import aes.model.Chat;
import aes.model.Message;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;
import java.io.IOException;

/**
 *
 * @author bruno
 */
public class CustomMessageDeserializer extends StdDeserializer<Message> {
    
    public CustomMessageDeserializer() {
        this(null);
    }

    public CustomMessageDeserializer(Class<?> vc) {
        super(vc);
    }
    
    @Override
    public Message deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        Message m = new Message();
        Chat c = new Chat();
        
        JsonNode node = jp.getCodec().readTree(jp);
        
        //m.setId(node.get("id").asLong());
        m.setIdFrom(node.get("idFrom").asText());
        m.setContent(node.get("content").asText());
        c.setId(node.get("chat").asLong());
        m.setChat(c);
        
        return m;
    }
    
}
