package com.plugway.etl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Configuração de agendamento para jobs ETL.
 */
public class ScheduleConfig {
    
    private boolean enabled;
    private String cronExpression;
    private String timezone;
    private int intervalSeconds; // Para execução periódica simples
    
    public ScheduleConfig() {
        this.enabled = false;
        this.timezone = "America/Sao_Paulo";
        this.intervalSeconds = 0;
    }
    
    /**
     * Valida se a configuração de agendamento está completa.
     */
    @JsonIgnore
    public boolean isValid() {
        if (!enabled) {
            return true; // Se não está habilitado, não precisa validar
        }
        
        // Precisa ter cron expression OU intervalSeconds
        return (cronExpression != null && !cronExpression.trim().isEmpty()) || 
               intervalSeconds > 0;
    }
    
    // Getters e Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public int getIntervalSeconds() {
        return intervalSeconds;
    }
    
    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
}

