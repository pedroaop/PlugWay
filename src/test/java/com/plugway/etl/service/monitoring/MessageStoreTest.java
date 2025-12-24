package com.plugway.etl.service.monitoring;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para MessageStore.
 */
@DisplayName("MessageStore Tests")
class MessageStoreTest {
    
    private MessageStore messageStore;
    
    @BeforeEach
    void setUp() {
        messageStore = MessageStore.getInstance();
        messageStore.clear(); // Limpa store antes de cada teste
    }
    
    @Test
    @DisplayName("MessageStore deve ser singleton")
    void testSingleton() {
        MessageStore instance1 = MessageStore.getInstance();
        MessageStore instance2 = MessageStore.getInstance();
        
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("MessageStore deve salvar e recuperar mensagens")
    void testSaveAndRetrieve() {
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        messageStore.save(message, "test-context");
        
        EtlMessage retrieved = messageStore.retrieve(message.getMessageId());
        
        assertNotNull(retrieved);
        assertEquals(message.getMessageId(), retrieved.getMessageId());
        assertEquals(message.getPayload(), retrieved.getPayload());
    }
    
    @Test
    @DisplayName("MessageStore deve recuperar mensagens por contexto")
    void testRetrieveByContext() {
        EtlMessage message1 = new EtlMessage(MessageType.DOCUMENT, "payload1");
        EtlMessage message2 = new EtlMessage(MessageType.DOCUMENT, "payload2");
        EtlMessage message3 = new EtlMessage(MessageType.DOCUMENT, "payload3");
        
        messageStore.save(message1, "context1");
        messageStore.save(message2, "context1");
        messageStore.save(message3, "context2");
        
        List<EtlMessage> context1Messages = messageStore.retrieveByContext("context1");
        
        assertEquals(2, context1Messages.size());
        assertTrue(context1Messages.stream()
            .anyMatch(m -> m.getMessageId().equals(message1.getMessageId())));
        assertTrue(context1Messages.stream()
            .anyMatch(m -> m.getMessageId().equals(message2.getMessageId())));
    }
    
    @Test
    @DisplayName("MessageStore deve recuperar mensagens por intervalo de tempo")
    void testRetrieveByTimeRange() {
        Instant start = Instant.now();
        
        EtlMessage message1 = new EtlMessage(MessageType.DOCUMENT, "payload1");
        messageStore.save(message1, "test-context");
        
        try {
            Thread.sleep(10); // Pequeno delay para garantir timestamps diferentes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Instant end = Instant.now();
        
        EtlMessage message2 = new EtlMessage(MessageType.DOCUMENT, "payload2");
        messageStore.save(message2, "test-context");
        
        List<EtlMessage> messages = messageStore.retrieveByTimeRange(start, end);
        
        assertTrue(messages.size() >= 1);
        assertTrue(messages.stream()
            .anyMatch(m -> m.getMessageId().equals(message1.getMessageId())));
    }
    
    @Test
    @DisplayName("MessageStore deve retornar null para mensagem inexistente")
    void testRetrieveNonExistent() {
        EtlMessage retrieved = messageStore.retrieve("non-existent-id");
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("MessageStore deve remover mensagens")
    void testRemove() {
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        messageStore.save(message, "test-context");
        
        assertNotNull(messageStore.retrieve(message.getMessageId()));
        
        messageStore.remove(message.getMessageId());
        
        assertNull(messageStore.retrieve(message.getMessageId()));
    }
    
    @Test
    @DisplayName("MessageStore deve limpar todas as mensagens")
    void testClear() {
        EtlMessage message1 = new EtlMessage(MessageType.DOCUMENT, "payload1");
        EtlMessage message2 = new EtlMessage(MessageType.DOCUMENT, "payload2");
        
        messageStore.save(message1, "test-context");
        messageStore.save(message2, "test-context");
        
        assertEquals(2, messageStore.size());
        
        messageStore.clear();
        
        assertEquals(0, messageStore.size());
    }
    
    @Test
    @DisplayName("MessageStore deve retornar todas as mensagens")
    void testRetrieveAll() {
        EtlMessage message1 = new EtlMessage(MessageType.DOCUMENT, "payload1");
        EtlMessage message2 = new EtlMessage(MessageType.DOCUMENT, "payload2");
        
        messageStore.save(message1, "test-context");
        messageStore.save(message2, "test-context");
        
        List<EtlMessage> allMessages = messageStore.retrieveAll();
        
        assertEquals(2, allMessages.size());
    }
    
    @Test
    @DisplayName("MessageStore deve ignorar mensagens null")
    void testSaveNullMessage() {
        assertDoesNotThrow(() -> messageStore.save(null, "test-context"));
        assertEquals(0, messageStore.size());
    }
}

