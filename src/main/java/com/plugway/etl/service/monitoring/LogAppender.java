package com.plugway.etl.service.monitoring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plugway.etl.ui.LogsController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Appender customizado do Logback para capturar logs em tempo real.
 * Permite que a interface gráfica receba logs em tempo real.
 */
public class LogAppender extends AppenderBase<ILoggingEvent> {
    
    private static LogAppender instance;
    private static final Object lock = new Object();
    
    private final List<LogListener> listeners;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public LogAppender() {
        this.listeners = new CopyOnWriteArrayList<>();
        // Registra esta instância como singleton quando criada pelo Logback
        synchronized (lock) {
            if (instance == null) {
                instance = this;
            }
        }
    }
    
    /**
     * Obtém instância singleton do LogAppender.
     */
    public static LogAppender getInstance() {
        synchronized (lock) {
            if (instance == null) {
                // Se não existe, cria uma instância temporária (não será usada pelo Logback)
                instance = new LogAppender();
            }
            return instance;
        }
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        if (listeners.isEmpty()) {
            return;
        }
        
        // Formata timestamp
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(event.getTimeStamp()),
            ZoneId.systemDefault()
        );
        String timestamp = TIMESTAMP_FORMATTER.format(dateTime);
        
        // Formata nível
        String level = event.getLevel().toString();
        
        // Obtém logger name (simplificado)
        String loggerName = event.getLoggerName();
        if (loggerName.length() > 50) {
            loggerName = "..." + loggerName.substring(loggerName.length() - 47);
        }
        
        // Obtém thread
        String thread = event.getThreadName();
        
        // Formata mensagem
        String message = event.getFormattedMessage();
        
        // Obtém exceção se houver
        String exception = null;
        if (event.getThrowableProxy() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(event.getThrowableProxy().getClassName());
            if (event.getThrowableProxy().getMessage() != null) {
                sb.append(": ").append(event.getThrowableProxy().getMessage());
            }
            exception = sb.toString();
        }
        
        // Cria entrada de log
        LogsController.LogEntry entry = new LogsController.LogEntry(
            timestamp, level, loggerName, thread, message
        );
        if (exception != null) {
            entry.setException(exception);
        }
        
        // Notifica todos os listeners
        for (LogListener listener : listeners) {
            try {
                listener.onLog(entry);
            } catch (Exception e) {
                // Ignora erros em listeners
            }
        }
    }
    
    /**
     * Adiciona um listener de logs.
     */
    public void addLogListener(LogListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove um listener de logs.
     */
    public void removeLogListener(LogListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Remove todos os listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }
    
    /**
     * Interface para receber notificações de logs.
     */
    @FunctionalInterface
    public interface LogListener {
        void onLog(LogsController.LogEntry entry);
    }
}

