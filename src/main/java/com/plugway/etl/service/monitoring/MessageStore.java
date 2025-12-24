package com.plugway.etl.service.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Message Store para persistir histórico de mensagens ETL.
 * Implementa o padrão Message Store (EIP).
 * 
 * Padrão EIP: Message Store
 * - Persiste mensagens para auditoria
 * - Mantém histórico de execuções
 * - Permite consulta e reprocessamento
 * - Armazena em memória e opcionalmente em arquivo
 */
public class MessageStore {
    
    private static final Logger logger = LoggerUtil.getLogger(MessageStore.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private static MessageStore instance;
    private static final Object instanceLock = new Object();
    
    private final Map<String, StoredMessage> messages;
    private final ObjectMapper objectMapper;
    private final Path storeDirectory;
    private final boolean persistToFile;
    private final ReadWriteLock lock;
    private final int maxInMemoryMessages;
    
    private MessageStore() {
        this(true, 1000);
    }
    
    private MessageStore(boolean persistToFile, int maxInMemoryMessages) {
        this.messages = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.persistToFile = persistToFile;
        this.maxInMemoryMessages = maxInMemoryMessages;
        this.lock = new ReentrantReadWriteLock();
        this.storeDirectory = Paths.get("data", "message-store");
        
        if (persistToFile) {
            try {
                Files.createDirectories(storeDirectory);
                logger.info("Message Store inicializado. Diretório: {}", storeDirectory);
            } catch (IOException e) {
                logger.error("Erro ao criar diretório do Message Store", e);
            }
        }
    }
    
    /**
     * Obtém instância singleton do Message Store.
     */
    public static MessageStore getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new MessageStore();
                }
            }
        }
        return instance;
    }
    
    /**
     * Salva uma mensagem no store.
     */
    public void save(EtlMessage message, String context) {
        if (message == null) {
            return;
        }
        
        this.lock.writeLock().lock();
        try {
            StoredMessage stored = new StoredMessage(message, context, Instant.now());
            messages.put(message.getMessageId(), stored);
            
            // Limita tamanho em memória
            if (messages.size() > maxInMemoryMessages) {
                removeOldestMessages();
            }
            
            // Persiste em arquivo se configurado
            if (persistToFile) {
                persistToFile(stored);
            }
            
            logger.debug("Mensagem salva no Message Store: {} | Contexto: {}", 
                        message.getMessageId(), context);
            
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /**
     * Recupera uma mensagem pelo ID.
     */
    public EtlMessage retrieve(String messageId) {
        this.lock.readLock().lock();
        try {
            StoredMessage stored = messages.get(messageId);
            return stored != null ? stored.getMessage() : null;
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /**
     * Recupera todas as mensagens de um contexto específico.
     */
    public List<EtlMessage> retrieveByContext(String context) {
        this.lock.readLock().lock();
        try {
            return messages.values().stream()
                .filter(stored -> context.equals(stored.getContext()))
                .map(StoredMessage::getMessage)
                .collect(Collectors.toList());
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /**
     * Recupera mensagens em um intervalo de tempo.
     */
    public List<EtlMessage> retrieveByTimeRange(Instant start, Instant end) {
        this.lock.readLock().lock();
        try {
            return messages.values().stream()
                .filter(stored -> {
                    Instant timestamp = stored.getTimestamp();
                    return !timestamp.isBefore(start) && !timestamp.isAfter(end);
                })
                .map(StoredMessage::getMessage)
                .collect(Collectors.toList());
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /**
     * Retorna todas as mensagens armazenadas.
     */
    public List<EtlMessage> retrieveAll() {
        this.lock.readLock().lock();
        try {
            return messages.values().stream()
                .map(StoredMessage::getMessage)
                .collect(Collectors.toList());
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /**
     * Remove uma mensagem do store.
     */
    public void remove(String messageId) {
        this.lock.writeLock().lock();
        try {
            messages.remove(messageId);
            logger.debug("Mensagem removida do Message Store: {}", messageId);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /**
     * Limpa todas as mensagens do store.
     */
    public void clear() {
        this.lock.writeLock().lock();
        try {
            messages.clear();
            logger.info("Message Store limpo");
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    /**
     * Retorna o número de mensagens armazenadas.
     */
    public int size() {
        this.lock.readLock().lock();
        try {
            return messages.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    /**
     * Persiste uma mensagem em arquivo.
     */
    private void persistToFile(StoredMessage stored) {
        try {
            String filename = String.format("message-%s-%s.json",
                stored.getMessage().getMessageId(),
                DATE_FORMATTER.format(LocalDateTime.now()));
            Path filePath = storeDirectory.resolve(filename);
            
            Map<String, Object> data = new HashMap<>();
            data.put("messageId", stored.getMessage().getMessageId());
            data.put("correlationId", stored.getMessage().getCorrelationId());
            data.put("context", stored.getContext());
            data.put("timestamp", stored.getTimestamp().toString());
            data.put("messageType", stored.getMessage().getType().toString());
            data.put("payload", stored.getMessage().getPayload());
            data.put("headers", stored.getMessage().getHeaders());
            
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.write(filePath, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
        } catch (IOException e) {
            logger.error("Erro ao persistir mensagem em arquivo", e);
        }
    }
    
    /**
     * Remove as mensagens mais antigas quando o limite é excedido.
     */
    private void removeOldestMessages() {
        List<Map.Entry<String, StoredMessage>> sorted = new ArrayList<>(messages.entrySet());
        sorted.sort(Comparator.comparing(entry -> entry.getValue().getTimestamp()));
        
        int toRemove = messages.size() - maxInMemoryMessages;
        for (int i = 0; i < toRemove; i++) {
            messages.remove(sorted.get(i).getKey());
        }
        
        logger.debug("Removidas {} mensagens antigas do Message Store", toRemove);
    }
    
    /**
     * Classe interna para armazenar mensagem com metadados.
     */
    private static class StoredMessage {
        private final EtlMessage message;
        private final String context;
        private final Instant timestamp;
        
        public StoredMessage(EtlMessage message, String context, Instant timestamp) {
            this.message = message;
            this.context = context;
            this.timestamp = timestamp;
        }
        
        public EtlMessage getMessage() {
            return message;
        }
        
        public String getContext() {
            return context;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
    }
}

