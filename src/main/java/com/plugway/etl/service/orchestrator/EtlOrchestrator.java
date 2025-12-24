package com.plugway.etl.service.orchestrator;

import com.plugway.etl.dao.DatabaseEndpoint;
import com.plugway.etl.dao.DatabaseConnectionFactory;
import com.plugway.etl.dao.ExtractService;
import com.plugway.etl.eip.EtlPipeline;
import com.plugway.etl.eip.MessageTransformer;
import com.plugway.etl.eip.WireTap;
import com.plugway.etl.model.*;
import com.plugway.etl.service.load.LoadService;
import com.plugway.etl.service.load.RestApiEndpoint;
import com.plugway.etl.service.monitoring.ExecutionMetrics;
import com.plugway.etl.service.transform.*;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.List;

/**
 * Orquestrador ETL que coordena Extract → Transform → Load.
 * Implementa o fluxo completo de um job ETL.
 */
public class EtlOrchestrator {
    
    private static final Logger logger = LoggerUtil.getLogger(EtlOrchestrator.class);
    
    private final ExtractService extractService;
    private final LoadService loadService;
    private final WireTap wireTap;
    
    public EtlOrchestrator() {
        this.extractService = new ExtractService();
        this.loadService = new LoadService();
        this.wireTap = new WireTap(true);
    }
    
    /**
     * Executa um job ETL completo.
     * 
     * @param job Job ETL a ser executado
     * @return Informações sobre a execução
     */
    public JobExecutionInfo execute(EtlJob job) {
        if (job == null || !job.isValid()) {
            throw new IllegalArgumentException("Job ETL inválido");
        }
        
        if (!job.isEnabled()) {
            logger.warn("Job {} está desabilitado. Pulando execução.", job.getId());
            JobExecutionInfo info = new JobExecutionInfo(job.getId());
            info.cancel();
            return info;
        }
        
        JobExecutionInfo executionInfo = new JobExecutionInfo(job.getId());
        ExecutionMetrics metrics = new ExecutionMetrics(job.getId());
        executionInfo.start();
        
        logger.info("Iniciando execução do job ETL: {} ({})", job.getName(), job.getId());
        
        // Wire Tap: intercepta início da execução
        if (wireTap != null) {
            EtlMessage startMessage = new EtlMessage();
            startMessage.addHeader("jobId", job.getId());
            startMessage.addHeader("jobName", job.getName());
            startMessage.addHeader("action", "job-start");
            wireTap.intercept(startMessage, "orchestrator-start");
        }
        
        try {
            // ETAPA 1: EXTRACT
            logger.info("ETAPA 1: Extraindo dados do banco de dados...");
            long extractStart = System.currentTimeMillis();
            metrics.startExtract();
            
            EtlMessage extractedMessage = extractData(job, executionInfo);
            
            long extractDuration = System.currentTimeMillis() - extractStart;
            int recordCount = getRecordCount(extractedMessage);
            metrics.endExtract(extractDuration, recordCount);
            
            // Wire Tap: intercepta após extração
            if (wireTap != null && extractedMessage != null) {
                wireTap.intercept(extractedMessage, "orchestrator-extract");
            }
            
            if (extractedMessage == null) {
                executionInfo.fail("Falha na extração de dados", null);
                metrics.markFailure("Falha na extração de dados");
                return executionInfo;
            }
            
            // ETAPA 2: TRANSFORM
            logger.info("ETAPA 2: Transformando dados...");
            long transformStart = System.currentTimeMillis();
            metrics.startTransform();
            
            EtlPipeline pipeline = createPipeline(job);
            pipeline.setWireTap(wireTap); // Configura Wire Tap no pipeline
            EtlMessage transformedMessage = pipeline.process(extractedMessage);
            
            long transformDuration = System.currentTimeMillis() - transformStart;
            metrics.endTransform(transformDuration, recordCount);
            
            // Wire Tap: intercepta após transformação
            if (wireTap != null && transformedMessage != null) {
                wireTap.intercept(transformedMessage, "orchestrator-transform");
            }
            
            // ETAPA 3: LOAD
            logger.info("ETAPA 3: Carregando dados para API...");
            long loadStart = System.currentTimeMillis();
            metrics.startLoad();
            
            boolean loadSuccess = loadData(job, transformedMessage);
            
            long loadDuration = System.currentTimeMillis() - loadStart;
            metrics.endLoad(loadDuration, recordCount);
            
            if (loadSuccess) {
                // recordCount já foi calculado anteriormente
                executionInfo.success(recordCount);
                metrics.markSuccess();
                logger.info("Job ETL executado com sucesso: {} | Registros: {}", job.getName(), recordCount);
                
                // Wire Tap: intercepta sucesso
                if (wireTap != null) {
                    EtlMessage successMessage = new EtlMessage();
                    successMessage.addHeader("jobId", job.getId());
                    successMessage.addHeader("jobName", job.getName());
                    successMessage.addHeader("action", "job-success");
                    successMessage.addHeader("recordCount", String.valueOf(recordCount));
                    wireTap.intercept(successMessage, "orchestrator-success");
                }
            } else {
                executionInfo.fail("Falha ao carregar dados para API", null);
                metrics.markFailure("Falha ao carregar dados para API");
                
                // Wire Tap: intercepta falha
                if (wireTap != null) {
                    EtlMessage failureMessage = new EtlMessage();
                    failureMessage.addHeader("jobId", job.getId());
                    failureMessage.addHeader("jobName", job.getName());
                    failureMessage.addHeader("action", "job-failure");
                    failureMessage.addHeader("reason", "Falha ao carregar dados para API");
                    wireTap.intercept(failureMessage, "orchestrator-failure");
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao executar job ETL: {}", job.getName(), e);
            executionInfo.fail("Erro durante execução: " + e.getMessage(), e);
            metrics.markFailure("Erro durante execução: " + e.getMessage());
            
            // Wire Tap: intercepta exceção
            if (wireTap != null) {
                EtlMessage errorMessage = new EtlMessage();
                errorMessage.addHeader("jobId", job.getId());
                errorMessage.addHeader("jobName", job.getName());
                errorMessage.addHeader("action", "job-error");
                errorMessage.addHeader("error", e.getMessage());
                wireTap.intercept(errorMessage, "orchestrator-error");
            }
        }
        
        return executionInfo;
    }
    
    /**
     * Extrai dados do banco de dados.
     */
    private EtlMessage extractData(EtlJob job, JobExecutionInfo executionInfo) {
        try {
            DatabaseConfig sourceConfig = job.getSourceConfig();
            String sqlQuery = job.getSqlQuery();
            
            // Converte parâmetros de query se necessário
            List<Object> parameters = null;
            if (job.getQueryParameters() != null && !job.getQueryParameters().isEmpty()) {
                parameters = new java.util.ArrayList<>();
                for (Object obj : job.getQueryParameters().values()) {
                    parameters.add(obj);
                }
            }
            
            EtlMessage message = extractService.extract(sourceConfig, sqlQuery, parameters);
            
            logger.debug("Extração concluída. Mensagem: {}", message.getMessageId());
            return message;
            
        } catch (Exception e) {
            logger.error("Erro na etapa de extração", e);
            return null;
        }
    }
    
    /**
     * Cria o pipeline de transformação baseado na configuração do job.
     */
    private EtlPipeline createPipeline(EtlJob job) {
        EtlPipeline pipeline = new EtlPipeline("Pipeline-" + job.getId());
        
        // Adiciona Normalizer
        boolean normalizeDates = getBooleanConfig(job, "normalizeDates", true);
        boolean normalizeDecimals = getBooleanConfig(job, "normalizeDecimals", true);
        boolean normalizeColumnNames = getBooleanConfig(job, "normalizeColumnNames", true);
        String nullHandling = getStringConfig(job, "nullHandling", "keep");
        
        Normalizer normalizer = new Normalizer(
            normalizeDates, 
            normalizeDecimals, 
            normalizeColumnNames, 
            true, 
            nullHandling, 
            ""
        );
        pipeline.addFilter(normalizer);
        
        // Adiciona ContentEnricher
        boolean addMetadata = getBooleanConfig(job, "addMetadata", true);
        boolean addStatistics = getBooleanConfig(job, "addStatistics", true);
        ContentEnricher enricher = new ContentEnricher(addMetadata, addStatistics);
        pipeline.addFilter(enricher);
        
        // Adiciona DatabaseToJsonTranslator
        boolean prettyPrint = getBooleanConfig(job, "prettyPrint", false);
        DatabaseToJsonTranslator translator = new DatabaseToJsonTranslator(prettyPrint);
        pipeline.addFilter(translator);
        
        return pipeline;
    }
    
    /**
     * Carrega dados para a API REST.
     */
    private boolean loadData(EtlJob job, EtlMessage message) {
        try {
            ApiConfig targetConfig = job.getTargetConfig();
            return loadService.load(targetConfig, message);
            
        } catch (Exception e) {
            logger.error("Erro na etapa de carga", e);
            return false;
        }
    }
    
    /**
     * Obtém valor booleano da configuração de transformação.
     */
    private boolean getBooleanConfig(EtlJob job, String key, boolean defaultValue) {
        Object value = job.getTransformations().get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    /**
     * Obtém valor String da configuração de transformação.
     */
    private String getStringConfig(EtlJob job, String key, String defaultValue) {
        Object value = job.getTransformations().get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Conta o número de registros na mensagem extraída.
     */
    private int getRecordCount(EtlMessage message) {
        if (message == null) {
            return 0;
        }
        try {
            Object payload = message.getPayload();
            if (payload instanceof List) {
                return ((List<?>) payload).size();
            }
            String recordCountHeader = message.getHeader("recordCount");
            if (recordCountHeader != null) {
                return Integer.parseInt(recordCountHeader);
            }
        } catch (Exception e) {
            logger.debug("Erro ao contar registros", e);
        }
        return 0;
    }
}

