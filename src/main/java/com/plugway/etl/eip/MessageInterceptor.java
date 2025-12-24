package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;

/**
 * Interface para interceptação de mensagens.
 * Usado pelo padrão Wire Tap para interceptar mensagens sem modificá-las.
 * 
 * Padrão EIP: Wire Tap
 * - Intercepta mensagens que passam por um canal
 * - Não modifica a mensagem original
 * - Permite logging, auditoria, monitoramento
 */
public interface MessageInterceptor {
    
    /**
     * Intercepta uma mensagem.
     * A mensagem não deve ser modificada, apenas observada.
     * 
     * @param message Mensagem a ser interceptada
     * @param context Contexto da interceptação (ex: "extract", "transform", "load")
     */
    void intercept(EtlMessage message, String context);
    
    /**
     * Nome do interceptor para identificação.
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}

