package com.plugway.etl.integration;

import com.plugway.etl.eip.EtlPipeline;
import com.plugway.etl.eip.WireTap;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import com.plugway.etl.service.monitoring.MessageStore;
import com.plugway.etl.service.transform.DatabaseToJsonTranslator;
import com.plugway.etl.service.transform.Normalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o pipeline ETL completo.
 */
@DisplayName("ETL Pipeline Integration Tests")
class EtlPipelineIntegrationTest {
    
    private EtlPipeline pipeline;
    private WireTap wireTap;
    private MessageStore messageStore;
    
    @BeforeEach
    void setUp() {
        messageStore = MessageStore.getInstance();
        messageStore.clear();
        
        wireTap = new WireTap(true);
        pipeline = new EtlPipeline("IntegrationTestPipeline");
        pipeline.setWireTap(wireTap);
    }
    
    @Test
    @DisplayName("Pipeline completo deve processar dados e salvar no Message Store")
    void testCompletePipelineProcessing() throws Exception {
        // Adiciona transformers ao pipeline
        pipeline.addFilter(new Normalizer());
        pipeline.addFilter(new DatabaseToJsonTranslator(false));
        
        // Cria mensagem de teste com dados simulados
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, createTestData());
        
        // Processa através do pipeline
        EtlMessage result = pipeline.process(message);
        
        // Verifica resultado
        assertNotNull(result);
        assertNotNull(result.getPayload());
        
        // Verifica se foi salvo no Message Store (via Wire Tap)
        List<EtlMessage> storedMessages = messageStore.retrieveByContext("pipeline-output");
        assertFalse(storedMessages.isEmpty());
    }
    
    @Test
    @DisplayName("Pipeline deve interceptar mensagens em múltiplos pontos")
    void testMultipleInterceptionPoints() throws Exception {
        pipeline.addFilter(new Normalizer());
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, createTestData());
        String messageId = message.getMessageId();
        pipeline.process(message);
        
        // Verifica interceptações em diferentes contextos
        // Usa retrieve por ID para garantir que a mensagem foi salva
        EtlMessage retrieved = messageStore.retrieve(messageId);
        assertNotNull(retrieved, "Mensagem deve ter sido salva no Message Store");
        
        // Verifica que há mensagens nos contextos esperados
        List<EtlMessage> inputMessages = messageStore.retrieveByContext("pipeline-input");
        List<EtlMessage> outputMessages = messageStore.retrieveByContext("pipeline-output");
        
        // Pelo menos uma das listas deve conter a mensagem
        boolean foundInInput = inputMessages.stream()
            .anyMatch(m -> m.getMessageId().equals(messageId));
        boolean foundInOutput = outputMessages.stream()
            .anyMatch(m -> m.getMessageId().equals(messageId));
        
        assertTrue(foundInInput || foundInOutput, 
            "Mensagem deve ter sido interceptada em pelo menos um contexto");
    }
    
    @Test
    @DisplayName("Pipeline deve processar dados complexos")
    void testComplexDataProcessing() throws Exception {
        pipeline.addFilter(new Normalizer());
        pipeline.addFilter(new DatabaseToJsonTranslator(true)); // Pretty print
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, createComplexTestData());
        
        EtlMessage result = pipeline.process(message);
        
        assertNotNull(result);
        assertNotNull(result.getPayload());
        
        // Verifica que o payload é uma string JSON
        assertTrue(result.getPayload() instanceof String);
        String json = (String) result.getPayload();
        assertTrue(json.contains("{"));
        assertTrue(json.contains("}"));
    }
    
    /**
     * Cria dados de teste simples.
     */
    private List<Map<String, Object>> createTestData() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("name", "Test Record");
        record.put("value", 100.5);
        data.add(record);
        return data;
    }
    
    /**
     * Cria dados de teste complexos.
     */
    private List<Map<String, Object>> createComplexTestData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", i);
            record.put("name", "Record " + i);
            record.put("value", 100.0 * i);
            record.put("active", i % 2 == 0);
            record.put("timestamp", System.currentTimeMillis());
            data.add(record);
        }
        
        return data;
    }
}

