package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.service.monitoring.MessageStore;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Wire Tap para interceptação de mensagens ETL.
 * Implementa o padrão Wire Tap (EIP).
 * 
 * Padrão EIP: Wire Tap
 * - Intercepta mensagens que passam por um canal
 * - Permite múltiplos interceptors
 * - Não modifica a mensagem original
 * - Usado para logging, auditoria, monitoramento
 */
public class WireTap implements MessageInterceptor {
    
    private static final Logger logger = LoggerUtil.getLogger(WireTap.class);
    
    private final List<MessageInterceptor> interceptors;
    private final MessageStore messageStore;
    private final boolean enabled;
    
    public WireTap() {
        this(true);
    }
    
    public WireTap(boolean enabled) {
        this.interceptors = new CopyOnWriteArrayList<>();
        this.messageStore = MessageStore.getInstance();
        this.enabled = enabled;
    }
    
    /**
     * Intercepta uma mensagem e notifica todos os interceptors registrados.
     */
    @Override
    public void intercept(EtlMessage message, String context) {
        if (!enabled || message == null) {
            return;
        }
        
        logger.debug("Wire Tap interceptando mensagem: {} | Contexto: {}", 
                    message.getMessageId(), context);
        
        // Salva no Message Store para histórico
        try {
            messageStore.save(message, context);
        } catch (Exception e) {
            logger.error("Erro ao salvar mensagem no Message Store", e);
        }
        
        // Notifica todos os interceptors registrados
        for (MessageInterceptor interceptor : interceptors) {
            try {
                interceptor.intercept(message, context);
            } catch (Exception e) {
                logger.error("Erro ao executar interceptor: {}", interceptor.getName(), e);
            }
        }
    }
    
    /**
     * Registra um interceptor.
     */
    public void addInterceptor(MessageInterceptor interceptor) {
        if (interceptor != null && !interceptors.contains(interceptor)) {
            interceptors.add(interceptor);
            logger.debug("Interceptor registrado: {}", interceptor.getName());
        }
    }
    
    /**
     * Remove um interceptor.
     */
    public void removeInterceptor(MessageInterceptor interceptor) {
        interceptors.remove(interceptor);
    }
    
    /**
     * Remove todos os interceptors.
     */
    public void clearInterceptors() {
        interceptors.clear();
    }
    
    /**
     * Retorna a lista de interceptors.
     */
    public List<MessageInterceptor> getInterceptors() {
        return new ArrayList<>(interceptors);
    }
    
    @Override
    public String getName() {
        return "WireTap";
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        // Note: this.enabled is final, but we can add a mutable field if needed
        logger.info("Wire Tap {} habilitado", enabled ? "habilitado" : "desabilitado");
    }
}

