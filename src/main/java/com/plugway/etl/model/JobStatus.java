package com.plugway.etl.model;

/**
 * Status de execução de um job ETL.
 */
public enum JobStatus {
    PENDING,      // Aguardando execução
    RUNNING,      // Em execução
    SUCCESS,      // Concluído com sucesso
    FAILED,       // Falhou
    CANCELLED     // Cancelado
}

