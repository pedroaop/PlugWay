package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import com.plugway.etl.service.monitoring.MessageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para WireTap.
 */
@DisplayName("WireTap Tests")
class WireTapTest {
    
    private WireTap wireTap;
    private MessageStore messageStore;
    private TestInterceptor testInterceptor;
    
    @BeforeEach
    void setUp() {
        wireTap = new WireTap(true);
        messageStore = MessageStore.getInstance();
        messageStore.clear(); // Limpa store antes de cada teste
        testInterceptor = new TestInterceptor();
    }
    
    @Test
    @DisplayName("WireTap deve ser criado corretamente")
    void testWireTapCreation() {
        assertNotNull(wireTap);
        assertTrue(wireTap.isEnabled());
    }
    
    @Test
    @DisplayName("WireTap deve interceptar mensagens")
    void testInterceptMessage() {
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        
        wireTap.intercept(message, "test-context");
        
        // Verifica se a mensagem foi salva no Message Store
        EtlMessage retrieved = messageStore.retrieve(message.getMessageId());
        assertNotNull(retrieved);
        assertEquals(message.getMessageId(), retrieved.getMessageId());
    }
    
    @Test
    @DisplayName("WireTap deve notificar interceptors registrados")
    void testInterceptorNotification() {
        wireTap.addInterceptor(testInterceptor);
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        wireTap.intercept(message, "test-context");
        
        assertTrue(testInterceptor.wasCalled());
        assertEquals(message.getMessageId(), testInterceptor.getLastMessageId());
        assertEquals("test-context", testInterceptor.getLastContext());
    }
    
    @Test
    @DisplayName("WireTap deve ignorar mensagens null")
    void testInterceptNullMessage() {
        assertDoesNotThrow(() -> wireTap.intercept(null, "test-context"));
    }
    
    @Test
    @DisplayName("WireTap deve permitir adicionar e remover interceptors")
    void testAddRemoveInterceptors() {
        assertEquals(0, wireTap.getInterceptors().size());
        
        wireTap.addInterceptor(testInterceptor);
        assertEquals(1, wireTap.getInterceptors().size());
        
        wireTap.removeInterceptor(testInterceptor);
        assertEquals(0, wireTap.getInterceptors().size());
    }
    
    @Test
    @DisplayName("WireTap desabilitado não deve interceptar")
    void testDisabledWireTap() {
        WireTap disabledWireTap = new WireTap(false);
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        
        disabledWireTap.intercept(message, "test-context");
        
        // Mensagem não deve ser salva quando desabilitado
        EtlMessage retrieved = messageStore.retrieve(message.getMessageId());
        assertNull(retrieved);
    }
    
    /**
     * Interceptor de teste para verificar chamadas.
     */
    private static class TestInterceptor implements MessageInterceptor {
        private boolean called = false;
        private String lastMessageId;
        private String lastContext;
        
        @Override
        public void intercept(EtlMessage message, String context) {
            this.called = true;
            if (message != null) {
                this.lastMessageId = message.getMessageId();
            }
            this.lastContext = context;
        }
        
        public boolean wasCalled() {
            return called;
        }
        
        public String getLastMessageId() {
            return lastMessageId;
        }
        
        public String getLastContext() {
            return lastContext;
        }
    }
}

