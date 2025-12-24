package com.plugway.etl.service.monitoring;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas de execução de um job ETL.
 */
public class ExecutionMetrics {
    
    private final String jobId;
    private final Instant startTime;
    private Instant endTime;
    private final AtomicLong recordsProcessed;
    private final AtomicLong recordsExtracted;
    private final AtomicLong recordsTransformed;
    private final AtomicLong recordsLoaded;
    private long extractDuration;
    private long transformDuration;
    private long loadDuration;
    private boolean success;
    private String errorMessage;
    
    public ExecutionMetrics(String jobId) {
        this.jobId = jobId;
        this.startTime = Instant.now();
        this.recordsProcessed = new AtomicLong(0);
        this.recordsExtracted = new AtomicLong(0);
        this.recordsTransformed = new AtomicLong(0);
        this.recordsLoaded = new AtomicLong(0);
        this.success = false;
    }
    
    /**
     * Marca o início da etapa de extração.
     */
    public void startExtract() {
        // Pode ser usado para medir tempo de extração
    }
    
    /**
     * Marca o fim da etapa de extração.
     */
    public void endExtract(long durationMillis, long recordCount) {
        this.extractDuration = durationMillis;
        this.recordsExtracted.set(recordCount);
        this.recordsProcessed.set(recordCount);
    }
    
    /**
     * Marca o início da etapa de transformação.
     */
    public void startTransform() {
        // Pode ser usado para medir tempo de transformação
    }
    
    /**
     * Marca o fim da etapa de transformação.
     */
    public void endTransform(long durationMillis, long recordCount) {
        this.transformDuration = durationMillis;
        this.recordsTransformed.set(recordCount);
    }
    
    /**
     * Marca o início da etapa de carga.
     */
    public void startLoad() {
        // Pode ser usado para medir tempo de carga
    }
    
    /**
     * Marca o fim da etapa de carga.
     */
    public void endLoad(long durationMillis, long recordCount) {
        this.loadDuration = durationMillis;
        this.recordsLoaded.set(recordCount);
    }
    
    /**
     * Marca a execução como bem-sucedida.
     */
    public void markSuccess() {
        this.success = true;
        this.endTime = Instant.now();
    }
    
    /**
     * Marca a execução como falha.
     */
    public void markFailure(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.endTime = Instant.now();
    }
    
    /**
     * Retorna a duração total em milissegundos.
     */
    public long getTotalDuration() {
        if (endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }
    
    // Getters
    
    public String getJobId() {
        return jobId;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public long getRecordsProcessed() {
        return recordsProcessed.get();
    }
    
    public long getRecordsExtracted() {
        return recordsExtracted.get();
    }
    
    public long getRecordsTransformed() {
        return recordsTransformed.get();
    }
    
    public long getRecordsLoaded() {
        return recordsLoaded.get();
    }
    
    public long getExtractDuration() {
        return extractDuration;
    }
    
    public long getTransformDuration() {
        return transformDuration;
    }
    
    public long getLoadDuration() {
        return loadDuration;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}

