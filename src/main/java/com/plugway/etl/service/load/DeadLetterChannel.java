package com.plugway.etl.service.load;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dead Letter Channel para armazenar mensagens que falharam ao ser enviadas.
 * Implementa o padrão Dead Letter Channel (EIP).
 * 
 * Padrão EIP: Dead Letter Channel
 * - Armazena mensagens que não puderam ser entregues
 * - Permite reprocessamento posterior
 * - Evita perda de dados
 */
public class DeadLetterChannel {
    
    private static final Logger logger = LoggerUtil.getLogger(DeadLetterChannel.class);
    
    private final List<FailedMessage> failedMessages;
    private final Path deadLetterDirectory;
    private final boolean persistToFile;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public DeadLetterChannel() {
        this(true);
    }
    
    public DeadLetterChannel(boolean persistToFile) {
        this.failedMessages = new CopyOnWriteArrayList<>();
        this.persistToFile = persistToFile;
        this.deadLetterDirectory = Paths.get("data", "dead-letter");
        
        if (persistToFile) {
            try {
                Files.createDirectories(deadLetterDirectory);
                logger.info("Dead Letter Channel inicializado. Diretório: {}", deadLetterDirectory);
            } catch (IOException e) {
                logger.error("Erro ao criar diretório de Dead Letter Channel", e);
            }
        }
    }
    
    /**
     * Envia uma mensagem para o Dead Letter Channel.
     * 
     * @param message Mensagem que falhou
     * @param reason Motivo da falha
     */
    public void send(EtlMessage message, String reason) {
        if (message == null) {
            logger.warn("Tentativa de enviar mensagem null para Dead Letter Channel");
            return;
        }
        
        FailedMessage failedMessage = new FailedMessage(message, reason, LocalDateTime.now());
        failedMessages.add(failedMessage);
        
        logger.warn("Mensagem enviada para Dead Letter Channel: {} | Motivo: {}", 
                   message.getMessageId(), reason);
        
        // Persiste em arquivo se configurado
        if (persistToFile) {
            persistToFile(failedMessage);
        }
    }
    
    /**
     * Persiste uma mensagem falhada em arquivo.
     */
    private void persistToFile(FailedMessage failedMessage) {
        try {
            String timestamp = failedMessage.getTimestamp().format(DATE_FORMATTER);
            String filename = String.format("failed_%s_%s.json", 
                                           failedMessage.getMessage().getMessageId(), 
                                           timestamp);
            Path filePath = deadLetterDirectory.resolve(filename);
            
            // Cria JSON com informações da mensagem falhada
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"messageId\": \"").append(failedMessage.getMessage().getMessageId()).append("\",\n");
            json.append("  \"correlationId\": \"").append(failedMessage.getMessage().getCorrelationId()).append("\",\n");
            json.append("  \"timestamp\": \"").append(failedMessage.getTimestamp()).append("\",\n");
            json.append("  \"reason\": \"").append(escapeJson(failedMessage.getReason())).append("\",\n");
            json.append("  \"payload\": ").append(failedMessage.getMessage().getPayload()).append("\n");
            json.append("}\n");
            
            Files.writeString(filePath, json.toString(), StandardOpenOption.CREATE, 
                            StandardOpenOption.WRITE);
            
            logger.debug("Mensagem falhada persistida em: {}", filePath);
            
        } catch (IOException e) {
            logger.error("Erro ao persistir mensagem falhada em arquivo", e);
        }
    }
    
    /**
     * Escapa caracteres especiais para JSON.
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Retorna todas as mensagens falhadas.
     */
    public List<FailedMessage> getFailedMessages() {
        return new ArrayList<>(failedMessages);
    }
    
    /**
     * Retorna o número de mensagens falhadas.
     */
    public int getFailedMessageCount() {
        return failedMessages.size();
    }
    
    /**
     * Limpa todas as mensagens falhadas.
     */
    public void clear() {
        failedMessages.clear();
        logger.info("Dead Letter Channel limpo");
    }
    
    /**
     * Classe interna para representar uma mensagem falhada.
     */
    public static class FailedMessage {
        private final EtlMessage message;
        private final String reason;
        private final LocalDateTime timestamp;
        
        public FailedMessage(EtlMessage message, String reason, LocalDateTime timestamp) {
            this.message = message;
            this.reason = reason;
            this.timestamp = timestamp;
        }
        
        public EtlMessage getMessage() {
            return message;
        }
        
        public String getReason() {
            return reason;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}

