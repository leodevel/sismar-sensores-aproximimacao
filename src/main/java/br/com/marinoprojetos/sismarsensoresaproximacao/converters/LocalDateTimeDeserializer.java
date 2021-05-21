package br.com.marinoprojetos.sismarsensoresaproximacao.converters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    
	private static final long serialVersionUID = 1L;
    
    public LocalDateTimeDeserializer(){
        this(null);
    }
    
    public LocalDateTimeDeserializer(Class<?> c){
        super(c);
    }
    
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) throws IOException, JsonProcessingException {
        if (jsonParser.getCurrentTokenId() == JsonTokenId.ID_STRING) {
            try {
            	String date = jsonParser.getText();
            	if (date == null) {
            		return null;
            	}
            	return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
            } catch (Exception e) {
            	throw new JsonParseException(e);
            }
        }
        return null;
    }
    
}