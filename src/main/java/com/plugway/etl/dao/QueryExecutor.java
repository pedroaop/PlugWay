package com.plugway.etl.dao;

import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor de queries SQL.
 * Converte ResultSet em List<Map<String, Object>> para facilitar transformação.
 */
public class QueryExecutor {
    
    private static final Logger logger = LoggerUtil.getLogger(QueryExecutor.class);
    private final ConnectionManager connectionManager;
    
    public QueryExecutor() {
        this.connectionManager = ConnectionManager.getInstance();
    }
    
    public QueryExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Executa uma query SELECT e retorna os resultados como lista de mapas.
     * 
     * @param config Configuração do banco de dados
     * @param sqlQuery Query SQL a ser executada
     * @return Lista de mapas, onde cada mapa representa uma linha
     * @throws SQLException Se ocorrer erro na execução
     */
    public List<Map<String, Object>> executeQuery(DatabaseConfig config, String sqlQuery) throws SQLException {
        return executeQuery(config, sqlQuery, Collections.emptyList());
    }
    
    /**
     * Executa uma query SELECT parametrizada e retorna os resultados.
     * 
     * @param config Configuração do banco de dados
     * @param sqlQuery Query SQL com parâmetros (?)
     * @param parameters Lista de parâmetros
     * @return Lista de mapas, onde cada mapa representa uma linha
     * @throws SQLException Se ocorrer erro na execução
     */
    public List<Map<String, Object>> executeQuery(DatabaseConfig config, String sqlQuery, List<Object> parameters) throws SQLException {
        logger.debug("Executando query em: {} | Query: {}", config.getName(), sqlQuery);
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection connection = connectionManager.getConnection(config);
             PreparedStatement stmt = connection.prepareStatement(sqlQuery)) {
            
            // Define parâmetros
            setParameters(stmt, parameters);
            
            // Executa query
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Processa cada linha
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = getValue(rs, i, metaData.getColumnType(i));
                        row.put(columnName, value);
                    }
                    
                    results.add(row);
                }
            }
            
            logger.debug("Query executada com sucesso. {} registros retornados.", results.size());
            
        } catch (SQLException e) {
            logger.error("Erro ao executar query em: {}", config.getName(), e);
            throw e;
        }
        
        return results;
    }
    
    /**
     * Define os parâmetros no PreparedStatement.
     */
    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object param = parameters.get(i);
            int index = i + 1;
            
            if (param == null) {
                stmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(index, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(index, (Long) param);
            } else if (param instanceof Double) {
                stmt.setDouble(index, (Double) param);
            } else if (param instanceof BigDecimal) {
                stmt.setBigDecimal(index, (BigDecimal) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof java.sql.Date) {
                stmt.setDate(index, (java.sql.Date) param);
            } else if (param instanceof Timestamp) {
                stmt.setTimestamp(index, (Timestamp) param);
            } else if (param instanceof LocalDate) {
                stmt.setDate(index, java.sql.Date.valueOf((LocalDate) param));
            } else if (param instanceof LocalDateTime) {
                stmt.setTimestamp(index, Timestamp.valueOf((LocalDateTime) param));
            } else if (param instanceof LocalTime) {
                stmt.setTime(index, Time.valueOf((LocalTime) param));
            } else {
                stmt.setObject(index, param);
            }
        }
    }
    
    /**
     * Extrai o valor do ResultSet tratando diferentes tipos de dados.
     */
    private Object getValue(ResultSet rs, int columnIndex, int sqlType) throws SQLException {
        Object value = rs.getObject(columnIndex);
        
        if (value == null) {
            return null;
        }
        
        // Tratamento específico para alguns tipos
        switch (sqlType) {
            case Types.DATE:
                java.sql.Date date = rs.getDate(columnIndex);
                return date != null ? date.toLocalDate() : null;
                
            case Types.TIME:
                Time time = rs.getTime(columnIndex);
                return time != null ? time.toLocalTime() : null;
                
            case Types.TIMESTAMP:
                Timestamp timestamp = rs.getTimestamp(columnIndex);
                return timestamp != null ? timestamp.toLocalDateTime() : null;
                
            case Types.DECIMAL:
            case Types.NUMERIC:
                BigDecimal decimal = rs.getBigDecimal(columnIndex);
                return decimal;
                
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                Blob blob = rs.getBlob(columnIndex);
                if (blob != null) {
                    return blob.getBytes(1, (int) blob.length());
                }
                return null;
                
            case Types.CLOB:
                Clob clob = rs.getClob(columnIndex);
                if (clob != null) {
                    return clob.getSubString(1, (int) clob.length());
                }
                return null;
                
            default:
                return value;
        }
    }
    
    /**
     * Executa uma query e retorna apenas o primeiro resultado.
     */
    public Map<String, Object> executeQuerySingle(DatabaseConfig config, String sqlQuery) throws SQLException {
        List<Map<String, Object>> results = executeQuery(config, sqlQuery);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Executa uma query e retorna apenas o primeiro resultado com parâmetros.
     */
    public Map<String, Object> executeQuerySingle(DatabaseConfig config, String sqlQuery, List<Object> parameters) throws SQLException {
        List<Map<String, Object>> results = executeQuery(config, sqlQuery, parameters);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Executa uma query e retorna o número de linhas afetadas (para INSERT/UPDATE/DELETE).
     */
    public int executeUpdate(DatabaseConfig config, String sqlQuery) throws SQLException {
        return executeUpdate(config, sqlQuery, Collections.emptyList());
    }
    
    /**
     * Executa uma query de atualização parametrizada.
     */
    public int executeUpdate(DatabaseConfig config, String sqlQuery, List<Object> parameters) throws SQLException {
        logger.debug("Executando update em: {} | Query: {}", config.getName(), sqlQuery);
        
        try (Connection connection = connectionManager.getConnection(config);
             PreparedStatement stmt = connection.prepareStatement(sqlQuery)) {
            
            setParameters(stmt, parameters);
            int rowsAffected = stmt.executeUpdate();
            
            logger.debug("Update executado. {} linhas afetadas.", rowsAffected);
            
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Erro ao executar update em: {}", config.getName(), e);
            throw e;
        }
    }
}

