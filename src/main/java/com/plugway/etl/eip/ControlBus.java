package com.plugway.etl.eip;

import com.plugway.etl.model.JobStatus;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Control Bus para controle de execuções de jobs ETL.
 * Implementa o padrão Control Bus (EIP).
 * 
 * Padrão EIP: Control Bus
 * - Permite controle do sistema através de mensagens de comando
 * - Interface para iniciar, parar, pausar jobs
 * - Monitoramento de status
 */
public class ControlBus {
    
    private static final Logger logger = LoggerUtil.getLogger(ControlBus.class);
    private static ControlBus instance;
    
    private final Map<String, JobExecutionControl> runningJobs;
    private final ExecutorService executorService;
    
    private ControlBus() {
        this.runningJobs = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public static synchronized ControlBus getInstance() {
        if (instance == null) {
            instance = new ControlBus();
        }
        return instance;
    }
    
    /**
     * Inicia a execução de um job.
     * 
     * @param jobId ID do job a ser executado
     * @param executionTask Tarefa de execução
     * @return Future que representa a execução
     */
    public Future<?> startJob(String jobId, Runnable executionTask) {
        logger.info("Iniciando job: {}", jobId);
        
        JobExecutionControl control = new JobExecutionControl(jobId);
        runningJobs.put(jobId, control);
        
        Future<?> future = executorService.submit(() -> {
            try {
                control.setStatus(JobStatus.RUNNING);
                executionTask.run();
                control.setStatus(JobStatus.SUCCESS);
            } catch (Exception e) {
                logger.error("Erro ao executar job: {}", jobId, e);
                control.setStatus(JobStatus.FAILED);
                control.setErrorMessage(e.getMessage());
            } finally {
                // Remove após um tempo para permitir consulta do resultado
                new Thread(() -> {
                    try {
                        Thread.sleep(60000); // Mantém por 1 minuto
                        runningJobs.remove(jobId);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
        
        control.setFuture(future);
        return future;
    }
    
    /**
     * Para a execução de um job.
     * 
     * @param jobId ID do job a ser parado
     * @return true se o job foi parado, false se não estava em execução
     */
    public boolean stopJob(String jobId) {
        logger.info("Parando job: {}", jobId);
        
        JobExecutionControl control = runningJobs.get(jobId);
        if (control != null && control.getFuture() != null) {
            boolean cancelled = control.getFuture().cancel(true);
            if (cancelled) {
                control.setStatus(JobStatus.CANCELLED);
            }
            return cancelled;
        }
        
        return false;
    }
    
    /**
     * Pausa a execução de um job (preparado para implementação futura).
     * 
     * @param jobId ID do job a ser pausado
     * @return true se o job foi pausado
     */
    public boolean pauseJob(String jobId) {
        logger.info("Pausando job: {} (não implementado ainda)", jobId);
        // TODO: Implementar pausa quando necessário
        return false;
    }
    
    /**
     * Retoma a execução de um job pausado (preparado para implementação futura).
     * 
     * @param jobId ID do job a ser retomado
     * @return true se o job foi retomado
     */
    public boolean resumeJob(String jobId) {
        logger.info("Retomando job: {} (não implementado ainda)", jobId);
        // TODO: Implementar retomada quando necessário
        return false;
    }
    
    /**
     * Retorna o status de um job.
     * 
     * @param jobId ID do job
     * @return Status do job ou null se não encontrado
     */
    public JobStatus getStatus(String jobId) {
        JobExecutionControl control = runningJobs.get(jobId);
        return control != null ? control.getStatus() : null;
    }
    
    /**
     * Retorna informações de controle de um job.
     * 
     * @param jobId ID do job
     * @return Informações de controle ou null se não encontrado
     */
    public JobExecutionControl getJobControl(String jobId) {
        return runningJobs.get(jobId);
    }
    
    /**
     * Retorna todos os jobs em execução.
     */
    public Map<String, JobExecutionControl> getRunningJobs() {
        return new ConcurrentHashMap<>(runningJobs);
    }
    
    /**
     * Limpa jobs finalizados (mantém apenas os em execução).
     */
    public void cleanupFinishedJobs() {
        runningJobs.entrySet().removeIf(entry -> {
            JobStatus status = entry.getValue().getStatus();
            return status == JobStatus.SUCCESS || 
                   status == JobStatus.FAILED || 
                   status == JobStatus.CANCELLED;
        });
    }
    
    /**
     * Encerra o Control Bus e todos os jobs em execução.
     */
    public void shutdown() {
        logger.info("Encerrando Control Bus...");
        
        // Para todos os jobs
        for (String jobId : runningJobs.keySet()) {
            stopJob(jobId);
        }
        
        executorService.shutdown();
    }
    
    /**
     * Classe interna para controle de execução de um job.
     */
    public static class JobExecutionControl {
        private final String jobId;
        private JobStatus status;
        private Future<?> future;
        private String errorMessage;
        
        public JobExecutionControl(String jobId) {
            this.jobId = jobId;
            this.status = JobStatus.PENDING;
        }
        
        // Getters e Setters
        
        public String getJobId() {
            return jobId;
        }
        
        public JobStatus getStatus() {
            return status;
        }
        
        public void setStatus(JobStatus status) {
            this.status = status;
        }
        
        public Future<?> getFuture() {
            return future;
        }
        
        public void setFuture(Future<?> future) {
            this.future = future;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

