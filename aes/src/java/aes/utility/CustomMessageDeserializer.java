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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        JsonNode node = jp.getCodec().readTree(jp);
        
        //m.setId(node.get("id").asLong());
        m.setIdFrom(node.get("idFrom").asText());
        m.setNameFrom(node.get("nameFrom").asText());

        m.setContent(node.get("content").asText());
        
        try {
            m.setSentDate(format.parse(node.get("sentDate").asText()));
        } catch (ParseException ex) {
            Logger.getLogger(CustomMessageDeserializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        c.setId(node.get("chat").asLong());
        m.setChat(c);
        
        return m;
    }
    
}
