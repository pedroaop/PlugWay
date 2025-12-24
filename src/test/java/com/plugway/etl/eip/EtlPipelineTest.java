package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para EtlPipeline.
 */
@DisplayName("EtlPipeline Tests")
class EtlPipelineTest {
    
    private EtlPipeline pipeline;
    
    @BeforeEach
    void setUp() {
        pipeline = new EtlPipeline("TestPipeline");
    }
    
    @Test
    @DisplayName("Pipeline deve ser criado corretamente")
    void testPipelineCreation() {
        assertNotNull(pipeline);
        assertEquals("TestPipeline", pipeline.getName());
        assertEquals(0, pipeline.getFilterCount());
    }
    
    @Test
    @DisplayName("Pipeline deve adicionar filtros")
    void testAddFilter() {
        MessageTransformer transformer = new TestTransformer("TestTransformer");
        
        pipeline.addFilter(transformer);
        
        assertEquals(1, pipeline.getFilterCount());
        assertTrue(pipeline.getFilters().contains(transformer));
    }
    
    @Test
    @DisplayName("Pipeline deve remover filtros")
    void testRemoveFilter() {
        MessageTransformer transformer = new TestTransformer("TestTransformer");
        
        pipeline.addFilter(transformer);
        assertEquals(1, pipeline.getFilterCount());
        
        pipeline.removeFilter(transformer);
        assertEquals(0, pipeline.getFilterCount());
    }
    
    @Test
    @DisplayName("Pipeline deve processar mensagem através dos filtros")
    void testProcessMessage() throws Exception {
        TestTransformer transformer1 = new TestTransformer("Transformer1");
        TestTransformer transformer2 = new TestTransformer("Transformer2");
        
        pipeline.addFilter(transformer1);
        pipeline.addFilter(transformer2);
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test payload");
        EtlMessage result = pipeline.process(message);
        
        assertNotNull(result);
        assertEquals(1, transformer1.getCallCount());
        assertEquals(1, transformer2.getCallCount());
    }
    
    @Test
    @DisplayName("Pipeline deve lançar exceção para mensagem null")
    void testProcessNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.process(null);
        });
    }
    
    @Test
    @DisplayName("Pipeline deve propagar exceções dos transformers")
    void testPropagateException() {
        MessageTransformer failingTransformer = new MessageTransformer() {
            @Override
            public EtlMessage transform(EtlMessage message) throws Exception {
                throw new RuntimeException("Test exception");
            }
            
            @Override
            public String getName() {
                return "FailingTransformer";
            }
        };
        
        pipeline.addFilter(failingTransformer);
        
        EtlMessage message = new EtlMessage(MessageType.DOCUMENT, "test");
        
        assertThrows(Exception.class, () -> {
            pipeline.process(message);
        });
    }
    
    @Test
    @DisplayName("Pipeline deve limpar todos os filtros")
    void testClearFilters() {
        pipeline.addFilter(new TestTransformer("Transformer1"));
        pipeline.addFilter(new TestTransformer("Transformer2"));
        
        assertEquals(2, pipeline.getFilterCount());
        
        pipeline.clearFilters();
        
        assertEquals(0, pipeline.getFilterCount());
    }
    
    @Test
    @DisplayName("Pipeline deve ignorar filtros null")
    void testAddNullFilter() {
        pipeline.addFilter(null);
        assertEquals(0, pipeline.getFilterCount());
    }
    
    @Test
    @DisplayName("Pipeline deve ter Wire Tap configurado")
    void testWireTapConfiguration() {
        assertNotNull(pipeline.getWireTap());
        
        WireTap customWireTap = new WireTap(false);
        pipeline.setWireTap(customWireTap);
        
        assertSame(customWireTap, pipeline.getWireTap());
    }
    
    /**
     * Transformer de teste para verificar chamadas.
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
            // Simula transformação adicionando um header
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

