package com.plugway.etl.service.transform;

import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para TransformService.
 */
@DisplayName("TransformService Tests")
class TransformServiceTest {
    
    private TransformService transformService;
    
    @BeforeEach
    void setUp() {
        transformService = new TransformService();
    }
    
    @Test
    @DisplayName("TransformService deve ser criado corretamente")
    void testTransformServiceCreation() {
        assertNotNull(transformService);
        assertTrue(transformService.getTransformers().isEmpty());
    }
    
    @Test
    @DisplayName("TransformService deve adicionar transformers")
    void testAddTransformer() {
        MessageTransformer transformer = new TestTransformer("TestTransformer");
        
        transformService.addTransformer(transformer);
        
        assertEquals(1, transformService.getTransformers().size());
        assertTrue(transformService.getTransformers().contains(transformer));
    }
    
    @Test
    @DisplayName("TransformService deve remover transformers")
    void testRemoveTransformer() {
        MessageTransformer transformer = new TestTransformer("TestTransformer");
        
        transformService.addTransformer(transformer);
        assertEquals(1, transformService.getTransformers().size());
        
        transformService.removeTransformer(transformer);
        assertEquals(0, transformService.getTransformers().size());
    }
    
    @Test
    @DisplayName("TransformService deve aplicar transformações em sequência")
    void testTransformSequence() throws Exception {
        TestTransformer transformer1 = new TestTransformer("Transformer1");
        TestTransformer transformer2 = new TestTransformer("Transformer2");
        
        transformService.addTransformer(transformer1);
        transformService.addTransformer(transformer2);
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, createTestPayload());
        EtlMessage result = transformService.transform(message);
        
        assertNotNull(result);
        assertEquals(1, transformer1.getCallCount());
        assertEquals(1, transformer2.getCallCount());
    }
    
    @Test
    @DisplayName("TransformService deve lançar exceção para mensagem null")
    void testTransformNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            transformService.transform(null);
        });
    }
    
    @Test
    @DisplayName("TransformService deve criar instância padrão")
    void testCreateDefault() {
        TransformService defaultService = TransformService.createDefault();
        
        assertNotNull(defaultService);
        assertFalse(defaultService.getTransformers().isEmpty());
    }
    
    @Test
    @DisplayName("TransformService deve criar instância com pretty JSON")
    void testCreateWithPrettyJson() {
        TransformService prettyService = TransformService.createWithPrettyJson();
        
        assertNotNull(prettyService);
        assertFalse(prettyService.getTransformers().isEmpty());
    }
    
    @Test
    @DisplayName("TransformService deve limpar transformers")
    void testClearTransformers() {
        transformService.addTransformer(new TestTransformer("Transformer1"));
        transformService.addTransformer(new TestTransformer("Transformer2"));
        
        assertEquals(2, transformService.getTransformers().size());
        
        transformService.clearTransformers();
        
        assertEquals(0, transformService.getTransformers().size());
    }
    
    @Test
    @DisplayName("TransformService deve ignorar transformer null")
    void testAddNullTransformer() {
        transformService.addTransformer(null);
        assertEquals(0, transformService.getTransformers().size());
    }
    
    /**
     * Cria payload de teste.
     */
    private List<Map<String, Object>> createTestPayload() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("name", "Test");
        record.put("value", 100.5);
        data.add(record);
        return data;
    }
    
    /**
     * Transformer de teste.
     */
    private static class TestTransformer implements MessageTransformer {
        private final String name;
        private int callCount = 0;
        
        public TestTransformer(String name) {
            this.name = name;
        }
        
        @Override
        public EtlMessage transform(EtlMessage message) throws Exception {
            callCount++;
            message.addHeader("transformedBy", name);
            return message;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        public int getCallCount() {
            return callCount;
        }
    }
}

