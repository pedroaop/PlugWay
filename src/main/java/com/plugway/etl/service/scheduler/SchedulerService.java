package com.plugway.etl.service.scheduler;

import com.plugway.etl.model.EtlJob;
import com.plugway.etl.model.ScheduleConfig;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Serviço de alto nível para gerenciamento de agendamentos.
 */
public class SchedulerService {
    
    private static final Logger logger = LoggerUtil.getLogger(SchedulerService.class);
    private final JobScheduler scheduler;
    
    public SchedulerService() {
        this.scheduler = JobScheduler.getInstance();
    }
    
    /**
     * Agenda um job ETL.
     * 
     * @param job Job a ser agendado
     * @return true se foi agendado com sucesso
     */
    public boolean scheduleJob(EtlJob job) {
        if (job == null || job.getSchedule() == null) {
            return false;
        }
        
        ScheduleConfig scheduleConfig = job.getSchedule();
        
        if (!scheduleConfig.isValid()) {
            logger.warn("Configuração de agendamento inválida para job: {}", job.getId());
            return false;
        }
        
        return scheduler.scheduleJob(job, scheduleConfig);
    }
    
    /**
     * Remove o agendamento de um job.
     */
    public boolean unscheduleJob(String jobId) {
        return scheduler.unscheduleJob(jobId);
    }
    
    /**
     * Pausa o agendamento de um job.
     */
    public boolean pauseJob(String jobId) {
        return scheduler.pauseJob(jobId);
    }
    
    /**
     * Retoma o agendamento de um job.
     */
    public boolean resumeJob(String jobId) {
        return scheduler.resumeJob(jobId);
    }
    
    /**
     * Verifica se um job está agendado.
     */
    public boolean isScheduled(String jobId) {
        return scheduler.isScheduled(jobId);
    }
    
    /**
     * Retorna o próximo horário de execução de um job.
     */
    public String getNextFireTime(String jobId) {
        return scheduler.getNextFireTime(jobId);
    }
    
    /**
     * Retorna todos os jobs agendados.
     */
    public Map<String, EtlJob> getScheduledJobs() {
        return scheduler.getScheduledJobs();
    }
    
    /**
     * Agenda todos os jobs que têm agendamento habilitado.
     */
    public void scheduleAllJobs(java.util.List<EtlJob> jobs) {
        logger.info("Agendando todos os jobs habilitados...");
        
        int scheduled = 0;
        for (EtlJob job : jobs) {
            if (job.getSchedule() != null && job.getSchedule().isEnabled()) {
                if (scheduleJob(job)) {
                    scheduled++;
                }
            }
        }
        
        logger.info("{} jobs agendados com sucesso", scheduled);
    }
}

