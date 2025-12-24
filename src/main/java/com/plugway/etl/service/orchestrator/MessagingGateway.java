package com.plugway.etl.service.orchestrator;

import com.plugway.etl.model.EtlJob;
import com.plugway.etl.model.JobExecutionInfo;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

/**
 * Messaging Gateway que fornece uma interface simplificada para execução de jobs ETL.
 * Implementa o padrão Messaging Gateway (EIP).
 * 
 * Padrão EIP: Messaging Gateway
 * - Encapsula a complexidade do sistema de mensageria
 * - Fornece interface simples e direta
 * - Abstrai detalhes de implementação
 */
public class MessagingGateway {
    
    private static final Logger logger = LoggerUtil.getLogger(MessagingGateway.class);
    
    private final EtlOrchestrator orchestrator;
    
    public MessagingGateway() {
        this.orchestrator = new EtlOrchestrator();
    }
    
    public MessagingGateway(EtlOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }
    
    /**
     * Executa um job ETL e retorna o resultado.
     * Interface simplificada para a GUI.
     * 
     * @param job Job ETL a ser executado
     * @return Resultado da execução
     */
    public ExecutionResult executeEtlJob(EtlJob job) {
        logger.info("Executando job ETL via MessagingGateway: {}", job.getName());
        
        try {
            // Valida o job antes de executar
            if (!job.isValid()) {
                return ExecutionResult.failure("Job ETL inválido. Verifique as configurações.");
            }
            
            if (!job.isEnabled()) {
                return ExecutionResult.cancelled("Job está desabilitado");
            }
            
            // Executa o job
            JobExecutionInfo executionInfo = orchestrator.execute(job);
            
            // Converte para ExecutionResult
            return convertToExecutionResult(executionInfo);
            
        } catch (Exception e) {
            logger.error("Erro ao executar job ETL via MessagingGateway", e);
            return ExecutionResult.failure("Erro inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Converte JobExecutionInfo para ExecutionResult.
     */
    private ExecutionResult convertToExecutionResult(JobExecutionInfo info) {
        ExecutionResult result = new ExecutionResult();
        result.setJobId(info.getJobId());
        result.setStatus(info.getStatus());
        result.setStartTime(info.getStartTime());
        result.setEndTime(info.getEndTime());
        result.setDurationMillis(info.getDurationMillis());
        result.setRecordsProcessed(info.getRecordsProcessed());
        result.setErrorMessage(info.getErrorMessage());
        
        if (info.getStatus() == com.plugway.etl.model.JobStatus.SUCCESS) {
            result.setSuccess(true);
        } else if (info.getStatus() == com.plugway.etl.model.JobStatus.FAILED) {
            result.setSuccess(false);
        } else {
            result.setSuccess(false);
        }
        
        return result;
    }
    
    /**
     * Classe que representa o resultado da execução de um job ETL.
     */
    public static class ExecutionResult {
        private String jobId;
        private com.plugway.etl.model.JobStatus status;
        private boolean success;
        private java.time.Instant startTime;
        private java.time.Instant endTime;
        private long durationMillis;
        private int recordsProcessed;
        private String errorMessage;
        
        public static ExecutionResult success(int recordsProcessed) {
            ExecutionResult result = new ExecutionResult();
            result.setSuccess(true);
            result.setStatus(com.plugway.etl.model.JobStatus.SUCCESS);
            result.setRecordsProcessed(recordsProcessed);
            return result;
        }
        
        public static ExecutionResult failure(String errorMessage) {
            ExecutionResult result = new ExecutionResult();
            result.setSuccess(false);
            result.setStatus(com.plugway.etl.model.JobStatus.FAILED);
            result.setErrorMessage(errorMessage);
            return result;
        }
        
        public static ExecutionResult cancelled(String reason) {
            ExecutionResult result = new ExecutionResult();
            result.setSuccess(false);
            result.setStatus(com.plugway.etl.model.JobStatus.CANCELLED);
            result.setErrorMessage(reason);
            return result;
        }
        
        // Getters e Setters
        
        public String getJobId() {
            return jobId;
        }
        
        public void setJobId(String jobId) {
            this.jobId = jobId;
        }
        
        public com.plugway.etl.model.JobStatus getStatus() {
            return status;
        }
        
        public void setStatus(com.plugway.etl.model.JobStatus status) {
            this.status = status;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public java.time.Instant getStartTime() {
            return startTime;
        }
        
        public void setStartTime(java.time.Instant startTime) {
            this.startTime = startTime;
        }
        
        public java.time.Instant getEndTime() {
            return endTime;
        }
        
        public void setEndTime(java.time.Instant endTime) {
            this.endTime = endTime;
        }
        
        public long getDurationMillis() {
            return durationMillis;
        }
        
        public void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }
        
        public int getRecordsProcessed() {
            return recordsProcessed;
        }
        
        public void setRecordsProcessed(int recordsProcessed) {
            this.recordsProcessed = recordsProcessed;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public String getFormattedDuration() {
            if (durationMillis < 1000) {
                return durationMillis + "ms";
            } else if (durationMillis < 60000) {
                return (durationMillis / 1000) + "s";
            } else {
                long minutes = durationMillis / 60000;
                long seconds = (durationMillis % 60000) / 1000;
                return minutes + "m " + seconds + "s";
            }
        }
    }
}

