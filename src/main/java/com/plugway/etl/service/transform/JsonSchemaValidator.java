package com.plugway.etl.service.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador básico de estrutura JSON.
 * Valida se o JSON tem a estrutura esperada.
 */
public class JsonSchemaValidator {
    
    private static final Logger logger = LoggerUtil.getLogger(JsonSchemaValidator.class);
    private final ObjectMapper objectMapper;
    
    public JsonSchemaValidator() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Valida se uma string JSON é válida.
     * 
     * @param jsonString String JSON a ser validada
     * @return true se o JSON é válido, false caso contrário
     */
    public boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            logger.debug("JSON inválido: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida se o JSON é um array.
     * 
     * @param jsonString String JSON a ser validada
     * @return true se é um array, false caso contrário
     */
    public boolean isArray(String jsonString) {
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            return node.isArray();
        } catch (Exception e) {
            logger.debug("Erro ao validar se é array: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida se o JSON é um objeto.
     * 
     * @param jsonString String JSON a ser validada
     * @return true se é um objeto, false caso contrário
     */
    public boolean isObject(String jsonString) {
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            return node.isObject();
        } catch (Exception e) {
            logger.debug("Erro ao validar se é objeto: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida se o JSON contém campos obrigatórios.
     * 
     * @param jsonString String JSON a ser validada
     * @param requiredFields Lista de campos obrigatórios
     * @return Lista de campos faltantes (vazia se todos estão presentes)
     */
    public List<String> validateRequiredFields(String jsonString, List<String> requiredFields) {
        List<String> missingFields = new ArrayList<>();
        
        if (requiredFields == null || requiredFields.isEmpty()) {
            return missingFields;
        }
        
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            
            if (!node.isObject()) {
                logger.warn("JSON não é um objeto, não é possível validar campos");
                return requiredFields; // Todos os campos estão faltando
            }
            
            for (String field : requiredFields) {
                if (!node.has(field) || node.get(field).isNull()) {
                    missingFields.add(field);
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao validar campos obrigatórios", e);
            return requiredFields; // Em caso de erro, considera todos faltantes
        }
        
        return missingFields;
    }
    
    /**
     * Valida se o JSON tem o número esperado de elementos (se for array).
     * 
     * @param jsonString String JSON a ser validada
     * @param minElements Número mínimo de elementos
     * @param maxElements Número máximo de elementos (null para sem limite)
     * @return true se está dentro do range, false caso contrário
     */
    public boolean validateArraySize(String jsonString, int minElements, Integer maxElements) {
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            
            if (!node.isArray()) {
                return false;
            }
            
            int size = node.size();
            
            if (size < minElements) {
                return false;
            }
            
            if (maxElements != null && size > maxElements) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao validar tamanho do array", e);
            return false;
        }
    }
    
    /**
     * Retorna informações sobre a estrutura do JSON.
     */
    public JsonStructureInfo analyzeStructure(String jsonString) {
        JsonStructureInfo info = new JsonStructureInfo();
        
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            
            info.setValid(true);
            info.setType(node.isArray() ? "array" : node.isObject() ? "object" : "value");
            
            if (node.isArray()) {
                info.setElementCount(node.size());
                if (node.size() > 0 && node.get(0).isObject()) {
                    info.setFields(new ArrayList<>());
                    node.get(0).fieldNames().forEachRemaining(info.getFields()::add);
                }
            } else if (node.isObject()) {
                info.setFields(new ArrayList<>());
                node.fieldNames().forEachRemaining(info.getFields()::add);
            }
            
        } catch (Exception e) {
            info.setValid(false);
            info.setErrorMessage(e.getMessage());
        }
        
        return info;
    }
    
    /**
     * Classe para armazenar informações sobre a estrutura do JSON.
     */
    public static class JsonStructureInfo {
        private boolean valid;
        private String type;
        private Integer elementCount;
        private List<String> fields;
        private String errorMessage;
        
        public JsonStructureInfo() {
            this.fields = new ArrayList<>();
        }
        
        // Getters e Setters
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Integer getElementCount() {
            return elementCount;
        }
        
        public void setElementCount(Integer elementCount) {
            this.elementCount = elementCount;
        }
        
        public List<String> getFields() {
            return fields;
        }
        
        public void setFields(List<String> fields) {
            this.fields = fields;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

