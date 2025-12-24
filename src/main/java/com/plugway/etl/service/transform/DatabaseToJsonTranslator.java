package com.plugway.etl.service.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Translator que converte dados de banco de dados para formato JSON.
 * Implementa o padrão Message Translator (EIP).
 * 
 * Padrão EIP: Message Translator
 * - Converte mensagens de um formato (List<Map>) para outro (JSON String)
 */
public class DatabaseToJsonTranslator implements MessageTransformer {
    
    private static final Logger logger = LoggerUtil.getLogger(DatabaseToJsonTranslator.class);
    
    private final ObjectMapper objectMapper;
    private final boolean prettyPrint;
    
    public DatabaseToJsonTranslator() {
        this(false);
    }
    
    public DatabaseToJsonTranslator(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        this.objectMapper = createObjectMapper();
    }
    
    /**
     * Cria e configura o ObjectMapper do Jackson.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registra módulo para suporte a Java 8 Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Configura formatação de datas
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configura pretty print se solicitado
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        
        // Configura tratamento de valores nulos
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return mapper;
    }
    
    @Override
    public EtlMessage transform(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        logger.debug("Transformando mensagem para JSON: {}", message.getMessageId());
        
        Object payload = message.getPayload();
        
        if (payload == null) {
            logger.warn("Payload da mensagem é null. Criando JSON vazio.");
            payload = List.of();
        }
        
        // Converte payload para JSON
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao converter payload para JSON", e);
            throw new Exception("Erro ao converter dados para JSON: " + e.getMessage(), e);
        }
        
        // Cria nova mensagem com JSON como payload
        EtlMessage transformedMessage = new EtlMessage(jsonString);
        transformedMessage.setMessageId(message.getMessageId());
        transformedMessage.setCorrelationId(message.getCorrelationId());
        transformedMessage.setType(message.getType());
        transformedMessage.setTimestamp(message.getTimestamp());
        
        // Copia headers existentes
        transformedMessage.getHeaders().putAll(message.getHeaders());
        
        // Adiciona header indicando formato JSON
        transformedMessage.addHeader("contentType", "application/json");
        transformedMessage.addHeader("format", prettyPrint ? "pretty" : "compact");
        
        logger.debug("Transformação concluída. Tamanho do JSON: {} caracteres", jsonString.length());
        
        return transformedMessage;
    }
    
    /**
     * Converte uma lista de mapas diretamente para JSON.
     */
    public String toJson(List<Map<String, Object>> data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
    
    /**
     * Converte um objeto diretamente para JSON.
     */
    public String toJson(Object data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
    
    /**
     * Converte JSON string para objeto.
     */
    public <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }
    
    @Override
    public String getName() {
        return "DatabaseToJsonTranslator";
    }
    
    public boolean isPrettyPrint() {
        return prettyPrint;
    }
}

