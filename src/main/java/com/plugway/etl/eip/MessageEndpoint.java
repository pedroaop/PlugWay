package com.plugway.etl.eip;

import com.plugway.etl.model.EtlMessage;

/**
 * Interface que representa um Message Endpoint no padrão EIP.
 * Um Message Endpoint encapsula a lógica de comunicação com um sistema externo.
 * 
 * Padrão EIP: Message Endpoint
 * - Encapsula a lógica de comunicação com sistemas externos
 * - Abstrai detalhes de protocolo e formato
 */
public interface MessageEndpoint {
    
    /**
     * Envia uma mensagem através deste endpoint.
     * 
     * @param message A mensagem a ser enviada
     * @return true se o envio foi bem-sucedido, false caso contrário
     * @throws Exception Se ocorrer um erro durante o envio
     */
    boolean send(EtlMessage message) throws Exception;
    
    /**
     * Recebe uma mensagem deste endpoint (para endpoints que suportam recebimento).
     * 
     * @return A mensagem recebida, ou null se não houver mensagens
     * @throws Exception Se ocorrer um erro durante o recebimento
     */
    EtlMessage receive() throws Exception;
    
    /**
     * Verifica se o endpoint está disponível/conectado.
     * 
     * @return true se o endpoint está disponível, false caso contrário
     */
    boolean isAvailable();
    
    /**
     * Conecta ao endpoint (se necessário).
     * 
     * @throws Exception Se ocorrer um erro durante a conexão
     */
    void connect() throws Exception;
    
    /**
     * Desconecta do endpoint (se necessário).
     * 
     * @throws Exception Se ocorrer um erro durante a desconexão
     */
    void disconnect() throws Exception;
    
    /**
     * Retorna o nome identificador deste endpoint.
     * 
     * @return O nome do endpoint
     */
    String getName();
}

