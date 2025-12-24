package com.plugway.etl.service.load;

import com.plugway.etl.model.ApiConfig;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Handler para retry automático com backoff exponencial.
 * Implementa o padrão Guaranteed Delivery (EIP).
 * 
 * Padrão EIP: Guaranteed Delivery
 * - Garante que mensagens sejam entregues mesmo em caso de falha temporária
 * - Retry automático com backoff exponencial
 */
public class RetryHandler {
    
    private static final Logger logger = LoggerUtil.getLogger(RetryHandler.class);
    
    private final ApiConfig config;
    
    public RetryHandler(ApiConfig config) {
        this.config = config;
    }
    
    /**
     * Executa uma operação com retry automático.
     * 
     * @param operation Operação a ser executada
     * @return Resultado da operação
     * @throws Exception Se todas as tentativas falharem
     */
    public <T> T executeWithRetry(Supplier<T> operation) throws Exception {
        int maxRetries = config.getMaxRetries();
        long baseDelay = config.getRetryDelay();
        boolean useExponentialBackoff = config.isUseExponentialBackoff();
        
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelay, useExponentialBackoff);
                    logger.warn("Tentativa {} falhou. Tentando novamente em {}ms... Erro: {}", 
                               attempt + 1, delay, e.getMessage());
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Retry interrompido", ie);
                    }
                } else {
                    logger.error("Todas as {} tentativas falharam", maxRetries + 1);
                }
            }
        }
        
        throw new Exception("Operação falhou após " + (maxRetries + 1) + " tentativas", lastException);
    }
    
    /**
     * Calcula o delay para o próximo retry usando backoff exponencial.
     * 
     * @param attempt Número da tentativa atual (0-based)
     * @param baseDelay Delay base em milissegundos
     * @param useExponentialBackoff Se deve usar backoff exponencial
     * @return Delay em milissegundos
     */
    private long calculateDelay(int attempt, long baseDelay, boolean useExponentialBackoff) {
        if (!useExponentialBackoff) {
            return baseDelay;
        }
        
        // Backoff exponencial: delay = baseDelay * 2^attempt
        // Com limite máximo para evitar delays muito longos
        long delay = baseDelay * (1L << attempt);
        long maxDelay = 30000; // 30 segundos máximo
        
        return Math.min(delay, maxDelay);
    }
    
    /**
     * Executa uma operação sem retorno com retry automático.
     */
    public void executeWithRetry(Runnable operation) throws Exception {
        executeWithRetry(() -> {
            operation.run();
            return null;
        });
    }
}

