/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aes.utility;

import aes.model.Message;
import aes.model.Chat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author bruno
 */
public class CustomMessageSerializer extends StdSerializer<Message> {
    public CustomMessageSerializer() {
        this(null);
    }
 
    public CustomMessageSerializer(Class<Message> t) {
        super(t);
    }
    @Override
    public void serialize(Message t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        jg.writeStartObject();
       
        jg.writeNumberField("id", t.getId());
        jg.writeStringField("type", "message");
        jg.writeStringField("idFrom", t.getIdFrom());
        jg.writeStringField("nameFrom", t.getNameFrom());
        jg.writeStringField("content", t.getContent());
        jg.writeObjectField("sentDate", t.getSentDate());
        jg.writeNumberField("chat", t.getChat().getId());
        
        jg.writeEndObject();
    }
    
}
