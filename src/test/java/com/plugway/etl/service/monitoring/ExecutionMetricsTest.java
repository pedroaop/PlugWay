package com.plugway.etl.service.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ExecutionMetrics.
 */
@DisplayName("ExecutionMetrics Tests")
class ExecutionMetricsTest {
    
    private ExecutionMetrics metrics;
    
    @BeforeEach
    void setUp() {
        metrics = new ExecutionMetrics("test-job-1");
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve ser criado corretamente")
    void testMetricsCreation() {
        assertNotNull(metrics);
        assertEquals("test-job-1", metrics.getJobId());
        assertNotNull(metrics.getStartTime());
        assertFalse(metrics.isSuccess());
        assertEquals(0, metrics.getRecordsProcessed());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve rastrear etapas de extração")
    void testExtractMetrics() {
        metrics.startExtract();
        metrics.endExtract(1000, 50);
        
        assertEquals(1000, metrics.getExtractDuration());
        assertEquals(50, metrics.getRecordsExtracted());
        assertEquals(50, metrics.getRecordsProcessed());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve rastrear etapas de transformação")
    void testTransformMetrics() {
        metrics.startTransform();
        metrics.endTransform(500, 50);
        
        assertEquals(500, metrics.getTransformDuration());
        assertEquals(50, metrics.getRecordsTransformed());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve rastrear etapas de carga")
    void testLoadMetrics() {
        metrics.startLoad();
        metrics.endLoad(800, 50);
        
        assertEquals(800, metrics.getLoadDuration());
        assertEquals(50, metrics.getRecordsLoaded());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve marcar sucesso")
    void testMarkSuccess() {
        metrics.markSuccess();
        
        assertTrue(metrics.isSuccess());
        assertNotNull(metrics.getEndTime());
        assertNull(metrics.getErrorMessage());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve marcar falha")
    void testMarkFailure() {
        String errorMessage = "Test error";
        metrics.markFailure(errorMessage);
        
        assertFalse(metrics.isSuccess());
        assertNotNull(metrics.getEndTime());
        assertEquals(errorMessage, metrics.getErrorMessage());
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve calcular duração total")
    void testTotalDuration() throws InterruptedException {
        Thread.sleep(10); // Pequeno delay para garantir duração > 0
        
        long duration = metrics.getTotalDuration();
        
        assertTrue(duration > 0);
        
        metrics.markSuccess();
        long finalDuration = metrics.getTotalDuration();
        
        assertTrue(finalDuration >= duration);
    }
    
    @Test
    @DisplayName("ExecutionMetrics deve rastrear múltiplas etapas")
    void testMultipleStages() {
        metrics.startExtract();
        metrics.endExtract(1000, 100);
        
        metrics.startTransform();
        metrics.endTransform(500, 100);
        
        metrics.startLoad();
        metrics.endLoad(800, 100);
        
        metrics.markSuccess();
        
        assertEquals(1000, metrics.getExtractDuration());
        assertEquals(500, metrics.getTransformDuration());
        assertEquals(800, metrics.getLoadDuration());
        assertEquals(100, metrics.getRecordsProcessed());
        assertTrue(metrics.isSuccess());
    }
}

