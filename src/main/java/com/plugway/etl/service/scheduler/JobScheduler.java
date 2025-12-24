package com.plugway.etl.service.scheduler;

import com.plugway.etl.eip.ControlBus;
import com.plugway.etl.model.EtlJob;
import com.plugway.etl.model.JobStatus;
import com.plugway.etl.model.ScheduleConfig;
import com.plugway.etl.service.orchestrator.EtlOrchestrator;
import com.plugway.etl.service.orchestrator.MessagingGateway;
import com.plugway.etl.util.LoggerUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agendador de jobs ETL usando Quartz Scheduler.
 * Permite agendar execuções de jobs usando cron expressions ou intervalos.
 */
public class JobScheduler {
    
    private static final Logger logger = LoggerUtil.getLogger(JobScheduler.class);
    private static JobScheduler instance;
    
    private Scheduler scheduler;
    private final Map<String, EtlJob> scheduledJobs;
    private final EtlOrchestrator orchestrator;
    
    private JobScheduler() {
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.orchestrator = new EtlOrchestrator();
        initializeScheduler();
    }
    
    public static synchronized JobScheduler getInstance() {
        if (instance == null) {
            instance = new JobScheduler();
        }
        return instance;
    }
    
    /**
     * Inicializa o scheduler do Quartz.
     */
    private void initializeScheduler() {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            logger.info("JobScheduler inicializado com sucesso");
        } catch (SchedulerException e) {
            logger.error("Erro ao inicializar JobScheduler", e);
            throw new RuntimeException("Falha ao inicializar scheduler", e);
        }
    }
    
    /**
     * Agenda um job para execução usando cron expression.
     * 
     * @param job Job ETL a ser agendado
     * @param scheduleConfig Configuração de agendamento
     * @return true se foi agendado com sucesso
     */
    public boolean scheduleJob(EtlJob job, ScheduleConfig scheduleConfig) {
        if (job == null || scheduleConfig == null || !scheduleConfig.isEnabled()) {
            return false;
        }
        
        try {
            String jobId = job.getId();
            
            // Remove agendamento existente se houver
            unscheduleJob(jobId);
            
            // Armazena o job para referência (antes de criar o JobDetail)
            scheduledJobs.put(jobId, job);
            
            // Cria JobDetail
            JobDetail jobDetail = JobBuilder.newJob(EtlJobExecutor.class)
                    .withIdentity(jobId, "ETL_JOBS")
                    .usingJobData("jobId", jobId)
                    .storeDurably(false)
                    .build();
            
            // Cria Trigger baseado no tipo de agendamento
            Trigger trigger;
            
            if (scheduleConfig.getCronExpression() != null && !scheduleConfig.getCronExpression().isEmpty()) {
                // Agendamento via cron
                CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(scheduleConfig.getCronExpression());
                
                if (scheduleConfig.getTimezone() != null) {
                    cronSchedule.inTimeZone(java.util.TimeZone.getTimeZone(ZoneId.of(scheduleConfig.getTimezone())));
                }
                
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + "_trigger", "ETL_TRIGGERS")
                        .withSchedule(cronSchedule)
                        .build();
                
                logger.info("Job agendado com cron: {} | Expression: {}", jobId, scheduleConfig.getCronExpression());
                
            } else if (scheduleConfig.getIntervalSeconds() > 0) {
                // Agendamento por intervalo
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobId + "_trigger", "ETL_TRIGGERS")
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(scheduleConfig.getIntervalSeconds())
                                .repeatForever())
                        .startNow()
                        .build();
                
                logger.info("Job agendado com intervalo: {} | Intervalo: {}s", jobId, scheduleConfig.getIntervalSeconds());
                
            } else {
                logger.warn("Configuração de agendamento inválida para job: {}", jobId);
                return false;
            }
            
            // Agenda o job
            scheduler.scheduleJob(jobDetail, trigger);
            
            logger.info("Job agendado com sucesso: {}", jobId);
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao agendar job: {}", job.getId(), e);
            return false;
        }
    }
    
    /**
     * Remove o agendamento de um job.
     * 
     * @param jobId ID do job
     * @return true se foi removido com sucesso
     */
    public boolean unscheduleJob(String jobId) {
        try {
            JobKey jobKey = JobKey.jobKey(jobId, "ETL_JOBS");
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                scheduledJobs.remove(jobId);
                logger.info("Agendamento removido: {}", jobId);
                return true;
            }
            
            return false;
            
        } catch (SchedulerException e) {
            logger.error("Erro ao remover agendamento: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Pausa o agendamento de um job.
     */
    public boolean pauseJob(String jobId) {
        try {
            JobKey jobKey = JobKey.jobKey(jobId, "ETL_JOBS");
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                logger.info("Agendamento pausado: {}", jobId);
                return true;
            }
            
            return false;
            
        } catch (SchedulerException e) {
            logger.error("Erro ao pausar agendamento: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Retoma o agendamento de um job pausado.
     */
    public boolean resumeJob(String jobId) {
        try {
            JobKey jobKey = JobKey.jobKey(jobId, "ETL_JOBS");
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                logger.info("Agendamento retomado: {}", jobId);
                return true;
            }
            
            return false;
            
        } catch (SchedulerException e) {
            logger.error("Erro ao retomar agendamento: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Verifica se um job está agendado.
     */
    public boolean isScheduled(String jobId) {
        try {
            JobKey jobKey = JobKey.jobKey(jobId, "ETL_JOBS");
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            logger.error("Erro ao verificar agendamento: {}", jobId, e);
            return false;
        }
    }
    
    /**
     * Retorna informações sobre o próximo trigger de um job.
     */
    public String getNextFireTime(String jobId) {
        try {
            JobKey jobKey = JobKey.jobKey(jobId, "ETL_JOBS");
            
            if (scheduler.checkExists(jobKey)) {
                TriggerKey triggerKey = TriggerKey.triggerKey(jobId + "_trigger", "ETL_TRIGGERS");
                Trigger trigger = scheduler.getTrigger(triggerKey);
                
                if (trigger != null) {
                    java.util.Date nextFireTime = trigger.getNextFireTime();
                    if (nextFireTime != null) {
                        return java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                .format(nextFireTime.toInstant().atZone(java.time.ZoneId.systemDefault()));
                    }
                }
            }
            
            return "N/A";
            
        } catch (SchedulerException e) {
            logger.error("Erro ao obter próximo fire time: {}", jobId, e);
            return "N/A";
        }
    }
    
    /**
     * Retorna todos os jobs agendados.
     */
    public Map<String, EtlJob> getScheduledJobs() {
        return new HashMap<>(scheduledJobs);
    }
    
    /**
     * Encerra o scheduler.
     */
    public void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
                logger.info("JobScheduler encerrado");
            }
        } catch (SchedulerException e) {
            logger.error("Erro ao encerrar JobScheduler", e);
        }
    }
    
    /**
     * Job do Quartz que executa um EtlJob.
     */
    public static class EtlJobExecutor implements Job {
        
        private static final Logger logger = LoggerUtil.getLogger(EtlJobExecutor.class);
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String jobId = dataMap.getString("jobId");
            
            logger.info("Executando job agendado: {}", jobId);
            
            try {
                JobScheduler scheduler = JobScheduler.getInstance();
                EtlJob job = scheduler.scheduledJobs.get(jobId);
                
                if (job == null) {
                    logger.error("Job não encontrado: {}", jobId);
                    return;
                }
                
                // Executa o job via orchestrator
                EtlOrchestrator orchestrator = new EtlOrchestrator();
                com.plugway.etl.model.JobExecutionInfo executionInfo = orchestrator.execute(job);
                
                logger.info("Job agendado executado: {} | Status: {} | Registros: {}", 
                           jobId, executionInfo.getStatus(), executionInfo.getRecordsProcessed());
                
            } catch (Exception e) {
                logger.error("Erro ao executar job agendado: {}", jobId, e);
                throw new JobExecutionException("Erro ao executar job: " + jobId, e);
            }
        }
    }
}

