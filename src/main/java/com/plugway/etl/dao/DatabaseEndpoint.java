package com.plugway.etl.dao;

import com.plugway.etl.eip.MessageEndpoint;
import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Implementação de MessageEndpoint para bancos de dados.
 * Permite extrair dados de bancos de dados usando o padrão EIP Message Endpoint.
 * 
 * Padrão EIP: Message Endpoint
 * - Encapsula a lógica de comunicação com banco de dados
 * - Abstrai detalhes de JDBC e conexão
 */
public class DatabaseEndpoint implements MessageEndpoint {
    
    private static final Logger logger = LoggerUtil.getLogger(DatabaseEndpoint.class);
    
    private final DatabaseConfig config;
    private final ConnectionManager connectionManager;
    private final QueryExecutor queryExecutor;
    private boolean connected;
    
    public DatabaseEndpoint(DatabaseConfig config) {
        this.config = config;
        this.connectionManager = ConnectionManager.getInstance();
        this.queryExecutor = new QueryExecutor(connectionManager);
        this.connected = false;
    }
    
    @Override
    public void connect() throws Exception {
        if (isAvailable()) {
            logger.debug("Endpoint já está conectado: {}", getName());
            return;
        }
        
        try {
            // Testa a conexão
            if (connectionManager.testConnection(config)) {
                connected = true;
                logger.info("Conectado ao banco de dados: {}", getName());
            } else {
                throw new SQLException("Falha ao conectar ao banco de dados: " + getName());
            }
        } catch (SQLException e) {
            logger.error("Erro ao conectar ao banco de dados: {}", getName(), e);
            connected = false;
            throw e;
        }
    }
    
    @Override
    public void disconnect() throws Exception {
        if (!connected) {
            return;
        }
        
        try {
            connectionManager.closeDataSource(config.getName());
            connected = false;
            logger.info("Desconectado do banco de dados: {}", getName());
        } catch (Exception e) {
            logger.error("Erro ao desconectar do banco de dados: {}", getName(), e);
            throw e;
        }
    }
    
    @Override
    public boolean isAvailable() {
        if (!connected) {
            return false;
        }
        
        try {
            return connectionManager.testConnection(config);
        } catch (Exception e) {
            logger.debug("Erro ao verificar disponibilidade: {}", getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean send(EtlMessage message) throws Exception {
        // DatabaseEndpoint é principalmente para receber dados (extract)
        // Envio seria para inserção/atualização, que não é o foco principal do ETL
        throw new UnsupportedOperationException("DatabaseEndpoint não suporta envio de mensagens. Use QueryExecutor para operações de escrita.");
    }
    
    @Override
    public EtlMessage receive() throws Exception {
        // Para receber dados, é necessário especificar uma query
        // Este método não é adequado para DatabaseEndpoint
        throw new UnsupportedOperationException("Use executeQuery() para receber dados do banco.");
    }
    
    /**
     * Executa uma query SELECT e retorna os resultados como EtlMessage.
     * Este é o método principal para extrair dados do banco.
     * 
     * @param sqlQuery Query SQL a ser executada
     * @return EtlMessage contendo os dados extraídos
     * @throws SQLException Se ocorrer erro na execução
     */
    public EtlMessage executeQuery(String sqlQuery) throws SQLException {
        return executeQuery(sqlQuery, null);
    }
    
    /**
     * Executa uma query SELECT parametrizada e retorna os resultados.
     * 
     * @param sqlQuery Query SQL com parâmetros
     * @param parameters Lista de parâmetros
     * @return EtlMessage contendo os dados extraídos
     * @throws SQLException Se ocorrer erro na execução
     */
    public EtlMessage executeQuery(String sqlQuery, List<Object> parameters) throws SQLException {
        if (!isAvailable()) {
            try {
                connect();
            } catch (Exception e) {
                throw new SQLException("Não foi possível conectar ao banco de dados", e);
            }
        }
        
        logger.debug("Executando query no endpoint: {} | Query: {}", getName(), sqlQuery);
        
        List<Map<String, Object>> results = queryExecutor.executeQuery(config, sqlQuery, 
            parameters != null ? parameters : java.util.Collections.emptyList());
        
        // Cria mensagem ETL com os dados
        EtlMessage message = new EtlMessage(results);
        message.addHeader("source", "database");
        message.addHeader("database", config.getName());
        message.addHeader("query", sqlQuery);
        message.addHeader("recordCount", String.valueOf(results.size()));
        
        logger.debug("Query executada com sucesso. {} registros extraídos.", results.size());
        
        return message;
    }
    
    /**
     * Testa a conexão com o banco de dados.
     */
    public boolean testConnection() {
        try {
            return connectionManager.testConnection(config);
        } catch (Exception e) {
            logger.error("Erro ao testar conexão: {}", getName(), e);
            return false;
        }
    }
    
    /**
     * Reconecta ao banco de dados em caso de falha.
     */
    public void reconnect() throws Exception {
        logger.info("Tentando reconectar ao banco de dados: {}", getName());
        disconnect();
        Thread.sleep(1000); // Aguarda 1 segundo antes de reconectar
        connect();
    }
    
    @Override
    public String getName() {
        return config != null ? config.getName() : "Unknown";
    }
    
    public DatabaseConfig getConfig() {
        return config;
    }
    
    public QueryExecutor getQueryExecutor() {
        return queryExecutor;
    }
}

