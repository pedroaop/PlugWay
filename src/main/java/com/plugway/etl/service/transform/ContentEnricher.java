package com.plugway.etl.service.transform;

import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enricher que adiciona metadados e informações adicionais às mensagens.
 * Implementa o padrão Content Enricher (EIP).
 * 
 * Padrão EIP: Content Enricher
 * - Adiciona informações adicionais a uma mensagem
 * - Enriquece o conteúdo com dados de fontes externas ou calculados
 */
public class ContentEnricher implements MessageTransformer {
    
    private static final Logger logger = LoggerUtil.getLogger(ContentEnricher.class);
    
    private final boolean addMetadata;
    private final boolean addStatistics;
    private final Map<String, String> additionalHeaders;
    
    public ContentEnricher() {
        this(true, true);
    }
    
    public ContentEnricher(boolean addMetadata, boolean addStatistics) {
        this.addMetadata = addMetadata;
        this.addStatistics = addStatistics;
        this.additionalHeaders = new HashMap<>();
    }
    
    @Override
    public EtlMessage transform(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        logger.debug("Enriquecendo mensagem: {}", message.getMessageId());
        
        EtlMessage enrichedMessage = new EtlMessage(message.getPayload());
        enrichedMessage.setMessageId(message.getMessageId());
        enrichedMessage.setCorrelationId(message.getCorrelationId());
        enrichedMessage.setType(message.getType());
        enrichedMessage.setTimestamp(message.getTimestamp());
        
        // Copia headers existentes
        enrichedMessage.getHeaders().putAll(message.getHeaders());
        
        // Adiciona metadados se solicitado
        if (addMetadata) {
            addMetadata(enrichedMessage, message);
        }
        
        // Adiciona estatísticas se solicitado
        if (addStatistics) {
            addStatistics(enrichedMessage, message);
        }
        
        // Adiciona headers adicionais customizados
        enrichedMessage.getHeaders().putAll(additionalHeaders);
        
        logger.debug("Mensagem enriquecida com sucesso");
        
        return enrichedMessage;
    }
    
    /**
     * Adiciona metadados à mensagem.
     */
    private void addMetadata(EtlMessage message, EtlMessage original) {
        message.addHeader("enrichedAt", Instant.now().toString());
        message.addHeader("originalTimestamp", original.getTimestamp().toString());
        
        // Adiciona informações sobre o payload
        Object payload = message.getPayload();
        if (payload != null) {
            if (payload instanceof String) {
                String jsonString = (String) payload;
                message.addHeader("payloadSize", String.valueOf(jsonString.length()));
                message.addHeader("payloadType", "json-string");
            } else if (payload instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<?> list = (java.util.List<?>) payload;
                message.addHeader("recordCount", String.valueOf(list.size()));
                message.addHeader("payloadType", "list");
            } else {
                message.addHeader("payloadType", payload.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Adiciona estatísticas à mensagem.
     */
    private void addStatistics(EtlMessage message, EtlMessage original) {
        Object payload = original.getPayload();
        
        if (payload instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<?> list = (java.util.List<?>) payload;
            
            message.addHeader("statistics.recordCount", String.valueOf(list.size()));
            message.addHeader("statistics.hasData", String.valueOf(!list.isEmpty()));
            
            // Se for lista de mapas, adiciona informações sobre colunas
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> firstRecord = (Map<String, Object>) list.get(0);
                message.addHeader("statistics.columnCount", String.valueOf(firstRecord.size()));
                message.addHeader("statistics.columns", String.join(",", firstRecord.keySet()));
            }
        }
    }
    
    /**
     * Adiciona um header customizado que será incluído em todas as mensagens enriquecidas.
     */
    public void addCustomHeader(String key, String value) {
        additionalHeaders.put(key, value);
    }
    
    /**
     * Remove um header customizado.
     */
    public void removeCustomHeader(String key) {
        additionalHeaders.remove(key);
    }
    
    @Override
    public String getName() {
        return "ContentEnricher";
    }
    
    public boolean isAddMetadata() {
        return addMetadata;
    }
    
    public boolean isAddStatistics() {
        return addStatistics;
    }
}

