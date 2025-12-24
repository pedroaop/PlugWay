package com.plugway.etl.service.load;

import com.plugway.etl.eip.MessageEndpoint;
import com.plugway.etl.model.ApiConfig;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.http.HttpResponse;

/**
 * Implementação de MessageEndpoint para APIs REST.
 * Permite enviar dados para APIs REST usando o padrão EIP Message Endpoint.
 * 
 * Padrão EIP: Message Endpoint
 * - Encapsula a lógica de comunicação com API REST
 * - Abstrai detalhes de HTTP e autenticação
 */
public class RestApiEndpoint implements MessageEndpoint {
    
    private static final Logger logger = LoggerUtil.getLogger(RestApiEndpoint.class);
    
    private final ApiConfig config;
    private final RestApiClient client;
    private final RetryHandler retryHandler;
    private final DeadLetterChannel deadLetterChannel;
    private boolean connected;
    
    public RestApiEndpoint(ApiConfig config) {
        this.config = config;
        this.client = new RestApiClient(config);
        this.retryHandler = new RetryHandler(config);
        this.deadLetterChannel = new DeadLetterChannel();
        this.connected = false;
    }
    
    @Override
    public void connect() throws Exception {
        if (isAvailable()) {
            logger.debug("Endpoint já está conectado: {}", getName());
            return;
        }
        
        try {
            if (client.testConnection()) {
                connected = true;
                logger.info("Conectado à API REST: {}", getName());
            } else {
                throw new Exception("Falha ao conectar à API REST: " + getName());
            }
        } catch (Exception e) {
            logger.error("Erro ao conectar à API REST: {}", getName(), e);
            connected = false;
            throw e;
        }
    }
    
    @Override
    public void disconnect() throws Exception {
        connected = false;
        logger.info("Desconectado da API REST: {}", getName());
    }
    
    @Override
    public boolean isAvailable() {
        if (!connected) {
            return false;
        }
        
        try {
            return client.testConnection();
        } catch (Exception e) {
            logger.debug("Erro ao verificar disponibilidade: {}", getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean send(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        if (!isAvailable()) {
            try {
                connect();
            } catch (Exception e) {
                throw new Exception("Não foi possível conectar à API REST", e);
            }
        }
        
        logger.debug("Enviando mensagem para API REST: {} | MessageId: {}", getName(), message.getMessageId());
        
        // Extrai JSON do payload
        Object payload = message.getPayload();
        if (!(payload instanceof String)) {
            throw new IllegalArgumentException("Payload deve ser uma string JSON");
        }
        
        String jsonData = (String) payload;
        
        // Tenta enviar com retry
        try {
            HttpResponse<String> response = retryHandler.executeWithRetry(() -> {
                try {
                    return client.sendJson(jsonData);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Erro ao enviar requisição HTTP", e);
                }
            });
            
            // Verifica se foi bem-sucedido
            int statusCode = response.statusCode();
            boolean success = statusCode >= 200 && statusCode < 300;
            
            if (success) {
                logger.info("Mensagem enviada com sucesso para: {} | Status: {}", getName(), statusCode);
                return true;
            } else {
                logger.warn("API retornou status de erro: {} | Status: {} | Response: {}", 
                           getName(), statusCode, response.body());
                
                // Para alguns status codes, não tenta novamente
                if (statusCode >= 400 && statusCode < 500) {
                    // Erro do cliente (não deve tentar novamente)
                    deadLetterChannel.send(message, "HTTP " + statusCode + ": " + response.body());
                    return false;
                } else {
                    // Erro do servidor (pode tentar novamente, mas já tentou com retry)
                    deadLetterChannel.send(message, "HTTP " + statusCode + ": " + response.body());
                    return false;
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para API REST: {}", getName(), e);
            
            // Envia para Dead Letter Channel
            deadLetterChannel.send(message, "Exception: " + e.getMessage());
            
            throw e;
        }
    }
    
    @Override
    public EtlMessage receive() throws Exception {
        // RestApiEndpoint é apenas para envio, não suporta recebimento
        throw new UnsupportedOperationException("RestApiEndpoint não suporta recebimento de mensagens");
    }
    
    /**
     * Testa a conexão com a API REST.
     */
    public boolean testConnection() {
        return client.testConnection();
    }
    
    @Override
    public String getName() {
        return config != null ? config.getName() : "Unknown";
    }
    
    public ApiConfig getConfig() {
        return config;
    }
    
    public DeadLetterChannel getDeadLetterChannel() {
        return deadLetterChannel;
    }
}

