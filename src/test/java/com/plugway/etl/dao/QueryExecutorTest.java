package com.plugway.etl.dao;

import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.model.DatabaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para QueryExecutor.
 * Nota: Estes testes requerem um banco de dados real ou mock.
 */
@DisplayName("QueryExecutor Tests")
class QueryExecutorTest {
    
    private QueryExecutor queryExecutor;
    private DatabaseConfig testConfig;
    
    @BeforeEach
    void setUp() {
        queryExecutor = new QueryExecutor();
        
        // Configuração de teste (deve ser ajustada para ambiente real)
        testConfig = new DatabaseConfig();
        testConfig.setName("test-db");
        testConfig.setType(DatabaseType.MYSQL);
        testConfig.setHost("localhost");
        testConfig.setPort(3306);
        testConfig.setDatabase("test");
        testConfig.setUsername("test");
        testConfig.setPassword("test");
    }
    
    @Test
    @DisplayName("QueryExecutor deve ser criado corretamente")
    void testQueryExecutorCreation() {
        assertNotNull(queryExecutor);
    }
    
    @Test
    @DisplayName("DatabaseConfig deve ser válido quando preenchido corretamente")
    void testDatabaseConfigValidation() {
        assertTrue(testConfig.isValid());
    }
    
    @Test
    @DisplayName("DatabaseConfig deve construir URL JDBC corretamente")
    void testJdbcUrlBuilding() {
        String url = testConfig.buildJdbcUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("3306"));
        assertTrue(url.contains("test"));
    }
    
    @Test
    @DisplayName("DatabaseConfig deve ser inválido quando campos obrigatórios estão faltando")
    void testInvalidDatabaseConfig() {
        DatabaseConfig invalidConfig = new DatabaseConfig();
        assertFalse(invalidConfig.isValid());
        
        invalidConfig.setName("test");
        assertFalse(invalidConfig.isValid()); // Ainda falta tipo
        
        invalidConfig.setType(DatabaseType.MYSQL);
        assertFalse(invalidConfig.isValid()); // Ainda falta host
    }
}

