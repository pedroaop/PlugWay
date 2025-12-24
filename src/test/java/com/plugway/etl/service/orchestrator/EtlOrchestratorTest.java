package com.plugway.etl.service.orchestrator;

import com.plugway.etl.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para EtlOrchestrator.
 */
@DisplayName("EtlOrchestrator Tests")
class EtlOrchestratorTest {
    
    private EtlOrchestrator orchestrator;
    private EtlJob testJob;
    
    @BeforeEach
    void setUp() {
        orchestrator = new EtlOrchestrator();
        
        // Cria um job de teste básico
        testJob = createTestJob();
    }
    
    @Test
    @DisplayName("Orchestrator deve ser criado corretamente")
    void testOrchestratorCreation() {
        assertNotNull(orchestrator);
    }
    
    @Test
    @DisplayName("Orchestrator deve rejeitar job null")
    void testExecuteNullJob() {
        assertThrows(IllegalArgumentException.class, () -> {
            orchestrator.execute(null);
        });
    }
    
    @Test
    @DisplayName("Orchestrator deve rejeitar job inválido")
    void testExecuteInvalidJob() {
        EtlJob invalidJob = new EtlJob();
        invalidJob.setId("invalid-job");
        invalidJob.setName("Invalid Job");
        
        assertThrows(IllegalArgumentException.class, () -> {
            orchestrator.execute(invalidJob);
        });
    }
    
    @Test
    @DisplayName("Orchestrator deve cancelar job desabilitado")
    void testExecuteDisabledJob() {
        testJob.setEnabled(false);
        
        JobExecutionInfo info = orchestrator.execute(testJob);
        
        assertNotNull(info);
        assertEquals(JobStatus.CANCELLED, info.getStatus());
    }
    
    @Test
    @DisplayName("Orchestrator deve criar JobExecutionInfo")
    void testJobExecutionInfoCreation() {
        JobExecutionInfo info = orchestrator.execute(testJob);
        
        assertNotNull(info);
        assertEquals(testJob.getId(), info.getJobId());
        assertNotNull(info.getStartTime());
    }
    
    /**
     * Cria um job de teste básico.
     * Nota: Este job não será executável sem um banco de dados real,
     * mas serve para testar a estrutura do orquestrador.
     */
    private EtlJob createTestJob() {
        EtlJob job = new EtlJob();
        job.setId("test-job-1");
        job.setName("Test Job");
        job.setDescription("Job de teste");
        job.setEnabled(true);
        
        // Configuração de banco de dados
        DatabaseConfig sourceConfig = new DatabaseConfig();
        sourceConfig.setName("test-db");
        sourceConfig.setType(DatabaseType.MYSQL);
        sourceConfig.setHost("localhost");
        sourceConfig.setPort(3306);
        sourceConfig.setDatabase("test");
        sourceConfig.setUsername("test");
        sourceConfig.setPassword("test");
        job.setSourceConfig(sourceConfig);
        
        // Query SQL
        job.setSqlQuery("SELECT * FROM test_table LIMIT 10");
        
        // Configuração de API
        ApiConfig targetConfig = new ApiConfig();
        targetConfig.setName("test-api");
        targetConfig.setBaseUrl("http://localhost:8080");
        targetConfig.setEndpoint("/api/data");
        targetConfig.setMethod("POST");
        targetConfig.setAuthType(AuthType.NONE);
        job.setTargetConfig(targetConfig);
        
        // Transformações padrão
        Map<String, Object> transformations = new HashMap<>();
        transformations.put("normalizeDates", true);
        transformations.put("normalizeDecimals", true);
        transformations.put("addMetadata", true);
        transformations.put("prettyPrint", false);
        job.setTransformations(transformations);
        
        return job;
    }
}

