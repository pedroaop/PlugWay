package com.plugway.etl.service.transform;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseToJsonTranslator Tests")
class DatabaseToJsonTranslatorTest {
    
    private DatabaseToJsonTranslator translator;
    
    @BeforeEach
    void setUp() {
        translator = new DatabaseToJsonTranslator(false);
    }
    
    @Test
    @DisplayName("Deve converter lista de mapas para JSON")
    void testTransformListToJson() throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record1 = new HashMap<>();
        record1.put("id", 1);
        record1.put("name", "Test");
        data.add(record1);
        
        EtlMessage message = new EtlMessage(data);
        EtlMessage transformed = translator.transform(message);
        
        assertNotNull(transformed);
        assertTrue(transformed.getPayload() instanceof String);
        String json = (String) transformed.getPayload();
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Test\""));
    }
    
    @Test
    @DisplayName("Deve manter headers da mensagem original")
    void testPreserveHeaders() throws Exception {
        EtlMessage message = new EtlMessage(Collections.emptyList());
        message.addHeader("test", "value");
        
        EtlMessage transformed = translator.transform(message);
        
        assertEquals("value", transformed.getHeader("test"));
        assertEquals("application/json", transformed.getHeader("contentType"));
    }
    
    @Test
    @DisplayName("Deve lançar exceção para mensagem null")
    void testNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            translator.transform(null);
        });
    }
    
    @Test
    @DisplayName("Deve criar JSON vazio para payload null")
    void testNullPayload() throws Exception {
        EtlMessage message = new EtlMessage(null);
        EtlMessage transformed = translator.transform(message);
        
        assertNotNull(transformed);
        assertNotNull(transformed.getPayload());
    }
}

