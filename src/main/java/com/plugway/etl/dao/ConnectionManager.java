package com.plugway.etl.dao;

import com.plugway.etl.config.ConfigManager;
import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.util.LoggerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de conexões com pool usando HikariCP.
 * Gerencia múltiplas conexões simultâneas para diferentes bancos de dados.
 */
public class ConnectionManager {
    
    private static final Logger logger = LoggerUtil.getLogger(ConnectionManager.class);
    private static ConnectionManager instance;
    
    private final Map<String, HikariDataSource> dataSources;
    private final ConfigManager configManager;
    
    private ConnectionManager() {
        this.dataSources = new ConcurrentHashMap<>();
        this.configManager = ConfigManager.getInstance();
    }
    
    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
    
    /**
     * Obtém ou cria um DataSource para a configuração especificada.
     */
    public DataSource getDataSource(DatabaseConfig config) throws SQLException {
        if (config == null || !config.isValid()) {
            throw new IllegalArgumentException("Database configuration is invalid");
        }
        
        String key = config.getName();
        
        // Retorna DataSource existente se já estiver criado
        if (dataSources.containsKey(key)) {
            HikariDataSource ds = dataSources.get(key);
            if (ds != null && !ds.isClosed()) {
                return ds;
            }
            // Remove se estiver fechado
            dataSources.remove(key);
        }
        
        // Cria novo DataSource
        HikariDataSource dataSource = createDataSource(config);
        dataSources.put(key, dataSource);
        
        logger.info("DataSource criado para: {}", config.getName());
        
        return dataSource;
    }
    
    /**
     * Cria um novo HikariDataSource baseado na configuração.
     */
    private HikariDataSource createDataSource(DatabaseConfig config) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        
        // Configurações básicas
        hikariConfig.setJdbcUrl(config.buildJdbcUrl());
        hikariConfig.setDriverClassName(config.getType().getDriverClass());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Nome do pool
        hikariConfig.setPoolName("HikariPool-" + config.getName());
        
        // Configurações do pool (com valores padrão do ConfigManager)
        hikariConfig.setMinimumIdle(configManager.getInt("database.pool.minSize", 2));
        hikariConfig.setMaximumPoolSize(configManager.getInt("database.pool.maxSize", 10));
        hikariConfig.setConnectionTimeout(configManager.getInt("database.pool.connectionTimeout", 30000));
        hikariConfig.setIdleTimeout(configManager.getInt("database.pool.idleTimeout", 600000));
        hikariConfig.setMaxLifetime(configManager.getInt("database.pool.maxLifetime", 1800000));
        
        // Configurações específicas do banco
        configureDatabaseSpecific(hikariConfig, config);
        
        // Propriedades adicionais
        if (config.getProperties() != null) {
            config.getProperties().forEach(hikariConfig::addDataSourceProperty);
        }
        
        // Validação de conexão
        String testQuery = getTestQuery(config.getType());
        if (testQuery != null && !testQuery.isEmpty()) {
            hikariConfig.setConnectionTestQuery(testQuery);
        }
        
        try {
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            
            // Testa a conexão
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    logger.debug("Conexão validada com sucesso para: {}", config.getName());
                }
            }
            
            return dataSource;
            
        } catch (SQLException e) {
            logger.error("Erro ao criar DataSource para: {}", config.getName(), e);
            throw e;
        }
    }
    
    /**
     * Configura propriedades específicas de cada tipo de banco.
     */
    private void configureDatabaseSpecific(HikariConfig config, DatabaseConfig dbConfig) {
        switch (dbConfig.getType()) {
            case FIREBIRD:
                config.addDataSourceProperty("encoding", "UTF8");
                break;
            case MYSQL:
                config.addDataSourceProperty("useSSL", "false");
                config.addDataSourceProperty("serverTimezone", "UTC");
                break;
            case POSTGRESQL:
                // Configurações padrão do PostgreSQL são adequadas
                break;
            case SQLSERVER:
                config.addDataSourceProperty("encrypt", "false");
                config.addDataSourceProperty("trustServerCertificate", "true");
                break;
        }
    }
    
    /**
     * Retorna a query de teste apropriada para cada tipo de banco.
     * Retorna null para bancos que não precisam de query de validação (HikariCP usará isValid()).
     */
    private String getTestQuery(com.plugway.etl.model.DatabaseType type) {
        switch (type) {
            case FIREBIRD:
                // Firebird requer FROM RDB$DATABASE ou podemos usar isValid() do HikariCP
                return "SELECT 1 FROM RDB$DATABASE";
            case POSTGRESQL:
                return "SELECT 1";
            case SQLSERVER:
                return "SELECT 1";
            case MYSQL:
                return "SELECT 1";
            default:
                return "SELECT 1";
        }
    }
    
    /**
     * Obtém uma conexão do pool para a configuração especificada.
     */
    public Connection getConnection(DatabaseConfig config) throws SQLException {
        DataSource ds = getDataSource(config);
        return ds.getConnection();
    }
    
    /**
     * Valida se uma conexão está disponível.
     */
    public boolean testConnection(DatabaseConfig config) {
        try {
            try (Connection conn = getConnection(config)) {
                return conn.isValid(5);
            }
        } catch (SQLException e) {
            logger.error("Erro ao testar conexão: {}", config.getName(), e);
            return false;
        }
    }
    
    /**
     * Fecha e remove um DataSource.
     */
    public void closeDataSource(String name) {
        HikariDataSource ds = dataSources.remove(name);
        if (ds != null && !ds.isClosed()) {
            ds.close();
            logger.info("DataSource fechado: {}", name);
        }
    }
    
    /**
     * Fecha todos os DataSources.
     */
    public void closeAll() {
        logger.info("Fechando todos os DataSources...");
        dataSources.forEach((name, ds) -> {
            if (ds != null && !ds.isClosed()) {
                ds.close();
                logger.debug("DataSource fechado: {}", name);
            }
        });
        dataSources.clear();
    }
    
    /**
     * Retorna informações sobre o pool de conexões.
     */
    public String getPoolInfo(String name) {
        HikariDataSource ds = dataSources.get(name);
        if (ds == null || ds.isClosed()) {
            return "Pool não encontrado ou fechado";
        }
        
        return String.format(
            "Pool: %s | Active: %d | Idle: %d | Total: %d | Waiting: %d",
            name,
            ds.getHikariPoolMXBean().getActiveConnections(),
            ds.getHikariPoolMXBean().getIdleConnections(),
            ds.getHikariPoolMXBean().getTotalConnections(),
            ds.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}

