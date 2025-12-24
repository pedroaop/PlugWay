package com.plugway.etl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitário para criação de loggers SLF4J.
 * Centraliza a criação de loggers para facilitar futuras mudanças.
 */
public class LoggerUtil {
    
    /**
     * Retorna um logger para a classe especificada.
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Retorna um logger com o nome especificado.
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
}

