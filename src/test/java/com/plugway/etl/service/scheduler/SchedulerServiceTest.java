package com.plugway.etl.service.scheduler;

import com.plugway.etl.model.EtlJob;
import com.plugway.etl.model.ScheduleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SchedulerService.
 */
@DisplayName("SchedulerService Tests")
class SchedulerServiceTest {
    
    private SchedulerService schedulerService;
    
    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerService();
    }
    
    @Test
    @DisplayName("SchedulerService deve ser criado corretamente")
    void testSchedulerServiceCreation() {
        assertNotNull(schedulerService);
    }
    
    @Test
    @DisplayName("SchedulerService deve agendar job com schedule config válido")
    void testScheduleJob() {
        EtlJob job = createTestJob();
        ScheduleConfig scheduleConfig = new ScheduleConfig();
        scheduleConfig.setCronExpression("0 0 12 * * ?"); // Diariamente ao meio-dia
        scheduleConfig.setEnabled(true);
        job.setSchedule(scheduleConfig);
        
        boolean scheduled = schedulerService.scheduleJob(job);
        
        assertTrue(scheduled);
        assertTrue(schedulerService.isScheduled(job.getId()));
        
        // Limpa após teste
        schedulerService.unscheduleJob(job.getId());
    }
    
    @Test
    @DisplayName("SchedulerService deve remover agendamento")
    void testUnscheduleJob() {
        EtlJob job = createTestJob();
        ScheduleConfig scheduleConfig = new ScheduleConfig();
        scheduleConfig.setCronExpression("0 0 12 * * ?");
        scheduleConfig.setEnabled(true);
        job.setSchedule(scheduleConfig);
        
        schedulerService.scheduleJob(job);
        assertTrue(schedulerService.isScheduled(job.getId()));
        
        boolean unscheduled = schedulerService.unscheduleJob(job.getId());
        
        assertTrue(unscheduled);
        assertFalse(schedulerService.isScheduled(job.getId()));
    }
    
    @Test
    @DisplayName("SchedulerService deve pausar e retomar agendamento")
    void testPauseResumeJob() {
        EtlJob job = createTestJob();
        ScheduleConfig scheduleConfig = new ScheduleConfig();
        scheduleConfig.setCronExpression("0 0 12 * * ?");
        scheduleConfig.setEnabled(true);
        job.setSchedule(scheduleConfig);
        
        schedulerService.scheduleJob(job);
        
        boolean paused = schedulerService.pauseJob(job.getId());
        assertTrue(paused);
        
        boolean resumed = schedulerService.resumeJob(job.getId());
        assertTrue(resumed);
        
        // Limpa após teste
        schedulerService.unscheduleJob(job.getId());
    }
    
    @Test
    @DisplayName("SchedulerService deve retornar próximo horário de execução")
    void testGetNextFireTime() {
        EtlJob job = createTestJob();
        ScheduleConfig scheduleConfig = new ScheduleConfig();
        scheduleConfig.setCronExpression("0 0 12 * * ?");
        scheduleConfig.setEnabled(true);
        job.setSchedule(scheduleConfig);
        
        schedulerService.scheduleJob(job);
        
        String nextFireTime = schedulerService.getNextFireTime(job.getId());
        
        assertNotNull(nextFireTime);
        assertFalse(nextFireTime.isEmpty());
        
        // Limpa após teste
        schedulerService.unscheduleJob(job.getId());
    }
    
    @Test
    @DisplayName("SchedulerService deve retornar false para job sem schedule")
    void testScheduleJobWithoutSchedule() {
        EtlJob job = createTestJob();
        job.setSchedule(null);
        
        boolean scheduled = schedulerService.scheduleJob(job);
        
        assertFalse(scheduled);
    }
    
    /**
     * Cria um job de teste básico.
     */
    private EtlJob createTestJob() {
        EtlJob job = new EtlJob();
        job.setId("test-job-" + System.currentTimeMillis());
        job.setName("Test Job");
        job.setEnabled(true);
        return job;
    }
}

