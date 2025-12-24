package com.plugway.etl.dao;

import com.plugway.etl.eip.MessageEndpoint;
import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.model.DatabaseType;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

/**
 * Factory para criar DatabaseEndpoint e configurações de banco de dados.
 * Implementa o padrão Factory para criação de conexões.
 */
public class DatabaseConnectionFactory {
    
    private static final Logger logger = LoggerUtil.getLogger(DatabaseConnectionFactory.class);
    
    /**
     * Cria um DatabaseEndpoint baseado na configuração fornecida.
     * 
     * @param config Configuração do banco de dados
     * @return DatabaseEndpoint configurado
     * @throws IllegalArgumentException Se a configuração for inválida
     */
    public static DatabaseEndpoint createEndpoint(DatabaseConfig config) {
        if (config == null || !config.isValid()) {
            throw new IllegalArgumentException("Database configuration is invalid");
        }
        
        logger.debug("Criando DatabaseEndpoint para: {} ({})", config.getName(), config.getType());
        
        DatabaseEndpoint endpoint = new DatabaseEndpoint(config);
        
        // Tenta conectar automaticamente
        try {
            endpoint.connect();
        } catch (Exception e) {
            logger.warn("Não foi possível conectar automaticamente ao banco: {}", config.getName(), e);
            // Não lança exceção, permite conexão posterior
        }
        
        return endpoint;
    }
    
    /**
     * Cria uma configuração de banco de dados com valores padrão para cada tipo.
     */
    public static DatabaseConfig createConfig(String name, DatabaseType type, String host, 
                                              String database, String username, String password) {
        DatabaseConfig config = new DatabaseConfig();
        config.setName(name);
        config.setType(type);
        config.setHost(host);
        config.setDatabase(database);
        config.setUsername(username);
        config.setPassword(password);
        
        // Define porta padrão baseada no tipo
        config.setPort(getDefaultPort(type));
        
        // Adiciona propriedades padrão
        addDefaultProperties(config, type);
        
        return config;
    }
    
    /**
     * Retorna a porta padrão para cada tipo de banco.
     */
    private static int getDefaultPort(DatabaseType type) {
        switch (type) {
            case FIREBIRD:
                return 3050;
            case MYSQL:
                return 3306;
            case POSTGRESQL:
                return 5432;
            case SQLSERVER:
                return 1433;
            default:
                return 0;
        }
    }
    
    /**
     * Adiciona propriedades padrão baseadas no tipo de banco.
     */
    private static void addDefaultProperties(DatabaseConfig config, DatabaseType type) {
        switch (type) {
            case FIREBIRD:
                config.addProperty("encoding", "UTF8");
                config.addProperty("roleName", "");
                break;
            case MYSQL:
                config.addProperty("useSSL", "false");
                config.addProperty("serverTimezone", "UTC");
                config.addProperty("useUnicode", "true");
                config.addProperty("characterEncoding", "UTF-8");
                break;
            case POSTGRESQL:
                config.addProperty("ssl", "false");
                break;
            case SQLSERVER:
                config.addProperty("encrypt", "false");
                config.addProperty("trustServerCertificate", "true");
                break;
        }
    }
    
    /**
     * Cria um DatabaseEndpoint para Firebird.
     */
    public static DatabaseEndpoint createFirebirdEndpoint(String name, String host, int port,
                                                         String database, String username, String password) {
        DatabaseConfig config = createConfig(name, DatabaseType.FIREBIRD, host, database, username, password);
        config.setPort(port);
        return createEndpoint(config);
    }
    
    /**
     * Cria um DatabaseEndpoint para MySQL.
     */
    public static DatabaseEndpoint createMySqlEndpoint(String name, String host, int port,
                                                      String database, String username, String password) {
        DatabaseConfig config = createConfig(name, DatabaseType.MYSQL, host, database, username, password);
        config.setPort(port);
        return createEndpoint(config);
    }
    
    /**
     * Cria um DatabaseEndpoint para PostgreSQL.
     */
    public static DatabaseEndpoint createPostgreSqlEndpoint(String name, String host, int port,
                                                           String database, String username, String password) {
        DatabaseConfig config = createConfig(name, DatabaseType.POSTGRESQL, host, database, username, password);
        config.setPort(port);
        return createEndpoint(config);
    }
    
    /**
     * Cria um DatabaseEndpoint para SQL Server.
     */
    public static DatabaseEndpoint createSqlServerEndpoint(String name, String host, int port,
                                                         String database, String username, String password) {
        DatabaseConfig config = createConfig(name, DatabaseType.SQLSERVER, host, database, username, password);
        config.setPort(port);
        return createEndpoint(config);
    }
    
    /**
     * Valida se uma configuração pode ser usada para criar um endpoint.
     */
    public static boolean validateConfig(DatabaseConfig config) {
        if (config == null) {
            return false;
        }
        
        if (!config.isValid()) {
            return false;
        }
        
        // Valida se o driver está disponível
        try {
            Class.forName(config.getType().getDriverClass());
            return true;
        } catch (ClassNotFoundException e) {
            logger.warn("Driver JDBC não encontrado: {}", config.getType().getDriverClass());
            return false;
        }
    }
}

