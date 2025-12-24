package com.plugway.etl.service.transform;

import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.model.EtlMessage;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Normalizer que normaliza formatos de dados nas mensagens.
 * Implementa o padrão Normalizer (EIP).
 * 
 * Padrão EIP: Normalizer
 * - Normaliza dados de diferentes formatos para um formato padrão
 * - Garante consistência nos dados
 */
public class Normalizer implements MessageTransformer {
    
    private static final Logger logger = LoggerUtil.getLogger(Normalizer.class);
    
    private final boolean normalizeDates;
    private final boolean normalizeDecimals;
    private final boolean normalizeColumnNames;
    private final boolean handleNulls;
    private final String nullHandlingStrategy; // "keep", "exclude", "replace"
    private final String nullReplacement;
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public Normalizer() {
        this(true, true, true, true, "keep", "");
    }
    
    public Normalizer(boolean normalizeDates, boolean normalizeDecimals, 
                     boolean normalizeColumnNames, boolean handleNulls,
                     String nullHandlingStrategy, String nullReplacement) {
        this.normalizeDates = normalizeDates;
        this.normalizeDecimals = normalizeDecimals;
        this.normalizeColumnNames = normalizeColumnNames;
        this.handleNulls = handleNulls;
        this.nullHandlingStrategy = nullHandlingStrategy;
        this.nullReplacement = nullReplacement;
    }
    
    @Override
    public EtlMessage transform(EtlMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        logger.debug("Normalizando mensagem: {}", message.getMessageId());
        
        Object payload = message.getPayload();
        Object normalizedPayload = payload;
        
        // Normaliza o payload se for uma lista de mapas
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) payload;
            normalizedPayload = normalizeList(data);
        } else if (payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload;
            normalizedPayload = normalizeMap(data);
        }
        
        // Cria nova mensagem com payload normalizado
        EtlMessage normalizedMessage = new EtlMessage(normalizedPayload);
        normalizedMessage.setMessageId(message.getMessageId());
        normalizedMessage.setCorrelationId(message.getCorrelationId());
        normalizedMessage.setType(message.getType());
        normalizedMessage.setTimestamp(message.getTimestamp());
        normalizedMessage.getHeaders().putAll(message.getHeaders());
        normalizedMessage.addHeader("normalized", "true");
        
        logger.debug("Normalização concluída");
        
        return normalizedMessage;
    }
    
    /**
     * Normaliza uma lista de mapas.
     */
    private List<Map<String, Object>> normalizeList(List<Map<String, Object>> data) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        
        for (Map<String, Object> record : data) {
            Map<String, Object> normalizedRecord = normalizeMap(record);
            if (normalizedRecord != null) {
                normalized.add(normalizedRecord);
            }
        }
        
        return normalized;
    }
    
    /**
     * Normaliza um mapa (registro).
     */
    private Map<String, Object> normalizeMap(Map<String, Object> record) {
        if (record == null) {
            return null;
        }
        
        Map<String, Object> normalized = new LinkedHashMap<>();
        
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Normaliza nome da coluna
            if (normalizeColumnNames) {
                key = normalizeColumnName(key);
            }
            
            // Normaliza valor
            Object normalizedValue = normalizeValue(value);
            
            // Tratamento de nulos
            if (normalizedValue == null && handleNulls) {
                if ("exclude".equals(nullHandlingStrategy)) {
                    continue; // Não inclui campos nulos
                } else if ("replace".equals(nullHandlingStrategy)) {
                    normalizedValue = nullReplacement.isEmpty() ? "" : nullReplacement;
                }
                // "keep" - mantém null como está
            }
            
            normalized.put(key, normalizedValue);
        }
        
        return normalized;
    }
    
    /**
     * Normaliza o nome de uma coluna.
     */
    private String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return "";
        }
        
        // Remove espaços e caracteres especiais, converte para lowercase
        String normalized = columnName.trim()
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .toLowerCase();
        
        // Remove underscores duplicados
        normalized = normalized.replaceAll("_+", "_");
        
        // Remove underscore no início e fim
        normalized = normalized.replaceAll("^_+|_+$", "");
        
        return normalized.isEmpty() ? "column_" + Math.abs(columnName.hashCode()) : normalized;
    }
    
    /**
     * Normaliza um valor individual.
     */
    private Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // Normaliza datas
        if (normalizeDates) {
            if (value instanceof LocalDate) {
                return ISO_DATE_FORMATTER.format((LocalDate) value);
            } else if (value instanceof LocalDateTime) {
                return ISO_DATETIME_FORMATTER.format((LocalDateTime) value);
            } else if (value instanceof java.sql.Date) {
                return ISO_DATE_FORMATTER.format(((java.sql.Date) value).toLocalDate());
            } else if (value instanceof java.sql.Timestamp) {
                return ISO_DATETIME_FORMATTER.format(((java.sql.Timestamp) value).toLocalDateTime());
            }
        }
        
        // Normaliza decimais
        if (normalizeDecimals && value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            // Remove zeros à direita desnecessários
            return decimal.stripTrailingZeros().toPlainString();
        }
        
        // Mantém outros tipos como estão
        return value;
    }
    
    @Override
    public String getName() {
        return "Normalizer";
    }
    
    // Getters
    
    public boolean isNormalizeDates() {
        return normalizeDates;
    }
    
    public boolean isNormalizeDecimals() {
        return normalizeDecimals;
    }
    
    public boolean isNormalizeColumnNames() {
        return normalizeColumnNames;
    }
    
    public boolean isHandleNulls() {
        return handleNulls;
    }
    
    public String getNullHandlingStrategy() {
        return nullHandlingStrategy;
    }
}

