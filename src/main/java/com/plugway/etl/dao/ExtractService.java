package com.plugway.etl.dao;

import com.plugway.etl.model.DatabaseConfig;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Serviço de extração de dados.
 * Facilita o uso do DatabaseEndpoint para extrair dados de bancos de dados.
 */
public class ExtractService {
    
    private static final Logger logger = LoggerUtil.getLogger(ExtractService.class);
    
    /**
     * Extrai dados de um banco de dados usando uma query SQL.
     * 
     * @param config Configuração do banco de dados
     * @param sqlQuery Query SQL a ser executada
     * @return EtlMessage contendo os dados extraídos
     * @throws SQLException Se ocorrer erro na execução
     */
    public EtlMessage extract(DatabaseConfig config, String sqlQuery) throws SQLException {
        return extract(config, sqlQuery, null);
    }
    
    /**
     * Extrai dados de um banco de dados usando uma query SQL parametrizada.
     * 
     * @param config Configuração do banco de dados
     * @param sqlQuery Query SQL com parâmetros
     * @param parameters Lista de parâmetros
     * @return EtlMessage contendo os dados extraídos
     * @throws SQLException Se ocorrer erro na execução
     */
    public EtlMessage extract(DatabaseConfig config, String sqlQuery, List<Object> parameters) throws SQLException {
        logger.info("Iniciando extração de dados de: {} | Query: {}", config.getName(), sqlQuery);
        
        DatabaseEndpoint endpoint = DatabaseConnectionFactory.createEndpoint(config);
        
        try {
            EtlMessage message = endpoint.executeQuery(sqlQuery, parameters);
            
            @SuppressWarnings("unchecked")
            List<Object> records = (List<Object>) message.getPayload();
            logger.info("Extração concluída. {} registros extraídos de: {}", 
                       records != null ? records.size() : 0, config.getName());
            
            return message;
            
        } catch (SQLException e) {
            logger.error("Erro ao extrair dados de: {}", config.getName(), e);
            
            // Tenta reconectar e executar novamente
            try {
                logger.info("Tentando reconectar e reexecutar query...");
                endpoint.reconnect();
                EtlMessage message = endpoint.executeQuery(sqlQuery, parameters);
                logger.info("Reexecução bem-sucedida após reconexão");
                return message;
            } catch (Exception retryException) {
                logger.error("Falha na tentativa de reconexão", retryException);
                throw e;
            }
        } finally {
            // Não desconecta aqui para manter o pool ativo
            // A conexão será gerenciada pelo ConnectionManager
        }
    }
    
    /**
     * Testa a conexão com um banco de dados.
     * 
     * @param config Configuração do banco de dados
     * @return true se a conexão foi bem-sucedida
     */
    public boolean testConnection(DatabaseConfig config) {
        logger.info("Testando conexão com: {}", config.getName());
        
        try {
            DatabaseEndpoint endpoint = DatabaseConnectionFactory.createEndpoint(config);
            boolean result = endpoint.testConnection();
            
            if (result) {
                logger.info("Conexão testada com sucesso: {}", config.getName());
            } else {
                logger.warn("Falha ao testar conexão: {}", config.getName());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Erro ao testar conexão: {}", config.getName(), e);
            return false;
        }
    }
}

