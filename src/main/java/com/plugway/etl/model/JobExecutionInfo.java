package com.plugway.etl.model;

import java.time.Instant;

/**
 * Informações sobre a execução de um job ETL.
 */
public class JobExecutionInfo {
    
    private String jobId;
    private JobStatus status;
    private Instant startTime;
    private Instant endTime;
    private long durationMillis;
    private int recordsProcessed;
    private String errorMessage;
    private Exception exception;
    
    public JobExecutionInfo(String jobId) {
        this.jobId = jobId;
        this.status = JobStatus.PENDING;
        this.recordsProcessed = 0;
    }
    
    public void start() {
        this.startTime = Instant.now();
        this.status = JobStatus.RUNNING;
    }
    
    public void success(int recordsProcessed) {
        this.endTime = Instant.now();
        this.status = JobStatus.SUCCESS;
        this.recordsProcessed = recordsProcessed;
        if (startTime != null) {
            this.durationMillis = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
    
    public void fail(String errorMessage, Exception exception) {
        this.endTime = Instant.now();
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.exception = exception;
        if (startTime != null) {
            this.durationMillis = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
    
    public void cancel() {
        this.endTime = Instant.now();
        this.status = JobStatus.CANCELLED;
        if (startTime != null) {
            this.durationMillis = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
    
    // Getters
    
    public String getJobId() {
        return jobId;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public long getDurationMillis() {
        return durationMillis;
    }
    
    public int getRecordsProcessed() {
        return recordsProcessed;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public boolean isRunning() {
        return status == JobStatus.RUNNING;
    }
    
    public boolean isCompleted() {
        return status == JobStatus.SUCCESS || status == JobStatus.FAILED || status == JobStatus.CANCELLED;
    }
}

