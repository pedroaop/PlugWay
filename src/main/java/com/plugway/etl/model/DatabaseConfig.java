package com.plugway.etl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de conexão com banco de dados.
 */
public class DatabaseConfig {
    
    private String name;
    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Map<String, String> properties;
    
    public DatabaseConfig() {
        this.properties = new HashMap<>();
    }
    
    public DatabaseConfig(String name, DatabaseType type, String host, int port, 
                          String database, String username, String password) {
        this();
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Constrói a URL JDBC baseada no tipo de banco.
     */
    public String buildJdbcUrl() {
        if (type == null) {
            throw new IllegalStateException("Database type is not set");
        }
        
        StringBuilder url = new StringBuilder(type.getJdbcPrefix());
        
        switch (type) {
            case FIREBIRD:
                // jdbc:firebirdsql://host:port/database
                url.append(host).append(":").append(port).append("/").append(database);
                break;
                
            case MYSQL:
                // jdbc:mysql://host:port/database
                url.append(host).append(":").append(port).append("/").append(database);
                break;
                
            case POSTGRESQL:
                // jdbc:postgresql://host:port/database
                url.append(host).append(":").append(port).append("/").append(database);
                break;
                
            case SQLSERVER:
                // jdbc:sqlserver://host:port;databaseName=database
                url.append(host).append(":").append(port)
                   .append(";databaseName=").append(database);
                break;
        }
        
        return url.toString();
    }
    
    /**
     * Valida se a configuração está completa.
     */
    @JsonIgnore
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               type != null &&
               host != null && !host.trim().isEmpty() &&
               port > 0 &&
               database != null && !database.trim().isEmpty() &&
               username != null && !username.trim().isEmpty();
    }
    
    // Getters e Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DatabaseType getType() {
        return type;
    }
    
    public void setType(DatabaseType type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties != null ? properties : new HashMap<>();
    }
    
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }
    
    public String getProperty(String key) {
        return this.properties.get(key);
    }
    
    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

