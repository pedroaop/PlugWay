package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline ETL usando padrão Pipes-and-Filters.
 * Processa mensagens através de uma cadeia de transformers.
 * 
 * Padrão EIP: Pipes-and-Filters
 * - Cada transformer é um filtro
 * - Mensagens fluem através dos filtros sequencialmente
 * - Permite composição flexível de transformações
 */
public class EtlPipeline {
    
    private static final Logger logger = LoggerUtil.getLogger(EtlPipeline.class);
    
    private final List<MessageTransformer> filters;
    private final String name;
    private WireTap wireTap;
    
    public EtlPipeline() {
        this("DefaultPipeline");
    }
    
    public EtlPipeline(String name) {
        this.name = name;
        this.filters = new ArrayList<>();
        this.wireTap = new WireTap(true);
    }
    
    /**
     * Define o Wire Tap para interceptação de mensagens.
     */
    public void setWireTap(WireTap wireTap) {
        this.wireTap = wireTap;
    }
    
    /**
     * Obtém o Wire Tap configurado.
     */
    public WireTap getWireTap() {
        return wireTap;
    }
    
    /**
     * Adiciona um filtro ao pipeline.
     */
    public void addFilter(MessageTransformer filter) {
        if (filter != null) {
            filters.add(filter);
            logger.debug("Filtro adicionado ao pipeline {}: {}", name, filter.getName());
        }
    }
    
    /**
     * Remove um filtro do pipeline.
     */
    public void removeFilter(MessageTransformer filter) {
        filters.remove(filter);
    }
    
    /**
     * Limpa todos os filtros do pipeline.
     */
    public void clearFilters() {
        filters.clear();
    }
    
    /**
     * Processa uma mensagem através de todos os filtros do pipeline.
     * 
     * @param message Mensagem a ser processada
     * @return Mensagem processada
     * @throws Exception Se ocorrer erro durante o processamento
     */
    public EtlMessage process(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        logger.debug("Processando mensagem através do pipeline {} | Filtros: {}", name, filters.size());
        
        // Wire Tap: intercepta mensagem de entrada
        if (wireTap != null) {
            wireTap.intercept(message, "pipeline-input");
        }
        
        EtlMessage currentMessage = message;
        
        for (int i = 0; i < filters.size(); i++) {
            MessageTransformer filter = filters.get(i);
            logger.debug("Aplicando filtro {}/{}: {}", i + 1, filters.size(), filter.getName());
            
            // Wire Tap: intercepta antes de cada filtro
            if (wireTap != null) {
                wireTap.intercept(currentMessage, "pipeline-filter-" + filter.getName());
            }
            
            try {
                currentMessage = filter.transform(currentMessage);
            } catch (Exception e) {
                logger.error("Erro ao aplicar filtro {} no pipeline {}", filter.getName(), name, e);
                throw new Exception("Erro no pipeline ao aplicar filtro: " + filter.getName(), e);
            }
        }
        
        // Wire Tap: intercepta mensagem de saída
        if (wireTap != null) {
            wireTap.intercept(currentMessage, "pipeline-output");
        }
        
        logger.debug("Pipeline {} concluído. Mensagem processada: {}", name, currentMessage.getMessageId());
        
        return currentMessage;
    }
    
    /**
     * Retorna a lista de filtros do pipeline.
     */
    public List<MessageTransformer> getFilters() {
        return new ArrayList<>(filters);
    }
    
    /**
     * Retorna o número de filtros no pipeline.
     */
    public int getFilterCount() {
        return filters.size();
    }
    
    public String getName() {
        return name;
    }
}

