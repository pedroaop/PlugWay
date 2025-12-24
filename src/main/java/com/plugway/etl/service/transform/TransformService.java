package com.plugway.etl.service.transform;

import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de transformação de dados.
 * Orquestra os transformers para aplicar transformações em sequência.
 */
public class TransformService {
    
    private static final Logger logger = LoggerUtil.getLogger(TransformService.class);
    
    private final List<MessageTransformer> transformers;
    
    public TransformService() {
        this.transformers = new ArrayList<>();
    }
    
    /**
     * Adiciona um transformer à cadeia de transformação.
     */
    public void addTransformer(MessageTransformer transformer) {
        if (transformer != null) {
            transformers.add(transformer);
            logger.debug("Transformer adicionado: {}", transformer.getName());
        }
    }
    
    /**
     * Remove um transformer da cadeia.
     */
    public void removeTransformer(MessageTransformer transformer) {
        transformers.remove(transformer);
    }
    
    /**
     * Limpa todos os transformers.
     */
    public void clearTransformers() {
        transformers.clear();
    }
    
    /**
     * Aplica todas as transformações na mensagem em sequência.
     * 
     * @param message Mensagem a ser transformada
     * @return Mensagem transformada
     * @throws Exception Se ocorrer erro durante a transformação
     */
    public EtlMessage transform(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        logger.info("Iniciando transformação da mensagem: {}", message.getMessageId());
        
        EtlMessage currentMessage = message;
        
        for (MessageTransformer transformer : transformers) {
            logger.debug("Aplicando transformer: {}", transformer.getName());
            currentMessage = transformer.transform(currentMessage);
        }
        
        logger.info("Transformação concluída. Mensagem final: {}", currentMessage.getMessageId());
        
        return currentMessage;
    }
    
    /**
     * Cria um TransformService pré-configurado com transformers padrão.
     */
    public static TransformService createDefault() {
        TransformService service = new TransformService();
        
        // Normalizer primeiro (normaliza dados)
        service.addTransformer(new Normalizer());
        
        // ContentEnricher (adiciona metadados)
        service.addTransformer(new ContentEnricher());
        
        // DatabaseToJsonTranslator (converte para JSON)
        service.addTransformer(new DatabaseToJsonTranslator(false));
        
        return service;
    }
    
    /**
     * Cria um TransformService pré-configurado com JSON formatado (pretty print).
     */
    public static TransformService createWithPrettyJson() {
        TransformService service = new TransformService();
        
        service.addTransformer(new Normalizer());
        service.addTransformer(new ContentEnricher());
        service.addTransformer(new DatabaseToJsonTranslator(true)); // Pretty print
        
        return service;
    }
    
    /**
     * Retorna a lista de transformers configurados.
     */
    public List<MessageTransformer> getTransformers() {
        return new ArrayList<>(transformers);
    }
    
    /**
     * Retorna o número de transformers configurados.
     */
    public int getTransformerCount() {
        return transformers.size();
    }
}

