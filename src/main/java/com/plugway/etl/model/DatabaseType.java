package com.plugway.etl.model;

/**
 * Enum que representa os tipos de banco de dados suportados.
 */
public enum DatabaseType {
    FIREBIRD("Firebird", "org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://"),
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    POSTGRESQL("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://"),
    SQLSERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://");
    
    private final String displayName;
    private final String driverClass;
    private final String jdbcPrefix;
    
    DatabaseType(String displayName, String driverClass, String jdbcPrefix) {
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.jdbcPrefix = jdbcPrefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDriverClass() {
        return driverClass;
    }
    
    public String getJdbcPrefix() {
        return jdbcPrefix;
    }
    
    /**
     * Retorna o DatabaseType baseado no nome (case-insensitive).
     */
    public static DatabaseType fromString(String name) {
        if (name == null) {
            return null;
        }
        for (DatabaseType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}

