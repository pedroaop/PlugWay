package com.plugway.etl.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma mensagem no sistema ETL.
 * Implementa o padrão Message Construct do EIP.
 */
public class EtlMessage {
    
    private String correlationId;      // Correlation Identifier (EIP)
    private String messageId;          // Unique identifier
    private Instant timestamp;
    private MessageType type;          // DOCUMENT, EVENT, COMMAND
    private Object payload;            // Dados (JSON/Document)
    private Map<String, String> headers;
    
    public EtlMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.headers = new HashMap<>();
        this.type = MessageType.DOCUMENT;
    }
    
    public EtlMessage(Object payload) {
        this();
        this.payload = payload;
    }
    
    public EtlMessage(MessageType type, Object payload) {
        this();
        this.type = type;
        this.payload = payload;
    }
    
    /**
     * Cria uma nova mensagem com correlation ID para rastreamento.
     */
    public static EtlMessage createWithCorrelation(String correlationId, Object payload) {
        EtlMessage message = new EtlMessage(payload);
        message.setCorrelationId(correlationId);
        return message;
    }
    
    // Getters e Setters
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }
    
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public String getHeader(String key) {
        return this.headers.get(key);
    }
    
    @Override
    public String toString() {
        return "EtlMessage{" +
                "messageId='" + messageId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                '}';
    }
}

