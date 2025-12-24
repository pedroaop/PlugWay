package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;

/**
 * Interface que representa um Message Translator no padrão EIP.
 * Um Message Translator transforma uma mensagem de um formato para outro.
 * 
 * Padrão EIP: Message Translator
 * - Transforma mensagens entre diferentes formatos
 * - Pode ser usado em pipelines (Pipes-and-Filters)
 */
public interface MessageTransformer {
    
    /**
     * Transforma uma mensagem ETL.
     * 
     * @param message A mensagem a ser transformada
     * @return A mensagem transformada
     * @throws Exception Se ocorrer erro durante a transformação
     */
    EtlMessage transform(EtlMessage message) throws Exception;
    
    /**
     * Retorna o nome identificador deste transformer.
     * 
     * @return O nome do transformer
     */
    String getName();
}

