package com.plugway.etl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa um job ETL completo.
 * Contém todas as configurações necessárias para executar Extract → Transform → Load.
 */
public class EtlJob {
    
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    
    // Configuração de origem (Extract)
    private DatabaseConfig sourceConfig;
    private String sqlQuery;
    private Map<String, Object> queryParameters;
    
    // Configuração de destino (Load)
    private ApiConfig targetConfig;
    
    // Configuração de transformação
    private Map<String, Object> transformations;
    
    // Configuração de agendamento (será implementada na Fase 8)
    private ScheduleConfig schedule;
    
    public EtlJob() {
        this.enabled = true;
        this.queryParameters = new HashMap<>();
        this.transformations = new HashMap<>();
    }
    
    public EtlJob(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }
    
    /**
     * Valida se o job está configurado corretamente.
     */
    @JsonIgnore
    public boolean isValid() {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        if (sourceConfig == null || !sourceConfig.isValid()) {
            return false;
        }
        
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return false;
        }
        
        if (targetConfig == null || !targetConfig.isValid()) {
            return false;
        }
        
        return true;
    }
    
    // Getters e Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public DatabaseConfig getSourceConfig() {
        return sourceConfig;
    }
    
    public void setSourceConfig(DatabaseConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
    
    public String getSqlQuery() {
        return sqlQuery;
    }
    
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
    
    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }
    
    public void setQueryParameters(Map<String, Object> queryParameters) {
        this.queryParameters = queryParameters != null ? queryParameters : new HashMap<>();
    }
    
    public ApiConfig getTargetConfig() {
        return targetConfig;
    }
    
    public void setTargetConfig(ApiConfig targetConfig) {
        this.targetConfig = targetConfig;
    }
    
    public Map<String, Object> getTransformations() {
        return transformations;
    }
    
    public void setTransformations(Map<String, Object> transformations) {
        this.transformations = transformations != null ? transformations : new HashMap<>();
    }
    
    public ScheduleConfig getSchedule() {
        return schedule;
    }
    
    public void setSchedule(ScheduleConfig schedule) {
        this.schedule = schedule;
    }
    
    @Override
    public String toString() {
        return "EtlJob{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}

