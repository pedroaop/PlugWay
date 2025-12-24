package com.plugway.etl.service.load;

import com.plugway.etl.model.ApiConfig;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

/**
 * Serviço de carga de dados para APIs REST.
 * Facilita o uso do RestApiEndpoint para enviar dados.
 */
public class LoadService {
    
    private static final Logger logger = LoggerUtil.getLogger(LoadService.class);
    
    /**
     * Envia dados para uma API REST usando a configuração fornecida.
     * 
     * @param config Configuração da API
     * @param message Mensagem ETL com dados JSON
     * @return true se o envio foi bem-sucedido
     * @throws Exception Se ocorrer erro durante o envio
     */
    public boolean load(ApiConfig config, EtlMessage message) throws Exception {
        logger.info("Iniciando carga de dados para API: {} | MessageId: {}", 
                   config.getName(), message.getMessageId());
        
        RestApiEndpoint endpoint = new RestApiEndpoint(config);
        
        try {
            endpoint.connect();
            boolean success = endpoint.send(message);
            
            if (success) {
                logger.info("Carga concluída com sucesso para: {}", config.getName());
            } else {
                logger.warn("Carga concluída com falha para: {}", config.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Erro ao carregar dados para API: {}", config.getName(), e);
            throw e;
        } finally {
            // Não desconecta para manter conexão HTTP reutilizável
        }
    }
    
    /**
     * Testa a conexão com uma API REST.
     * 
     * @param config Configuração da API
     * @return true se a conexão foi bem-sucedida
     */
    public boolean testConnection(ApiConfig config) {
        logger.info("Testando conexão com API: {}", config.getName());
        
        try {
            RestApiEndpoint endpoint = new RestApiEndpoint(config);
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
    
    /**
     * Retorna informações sobre mensagens falhadas (Dead Letter Channel).
     */
    public int getFailedMessageCount(ApiConfig config) {
        RestApiEndpoint endpoint = new RestApiEndpoint(config);
        return endpoint.getDeadLetterChannel().getFailedMessageCount();
    }
}

