package com.plugway.etl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.plugway.etl.model.EtlJob;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para persistência de configurações de Jobs ETL em arquivo JSON.
 */
public class JobConfigService {
    
    private static final Logger logger = LoggerUtil.getLogger(JobConfigService.class);
    private static final String CONFIG_DIR = "config";
    private static final String JOBS_FILE = "jobs.json";
    
    private final ObjectMapper objectMapper;
    private final Path jobsFilePath;
    
    private static JobConfigService instance;
    
    private JobConfigService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Formatação bonita do JSON
        // Ignora propriedades desconhecidas ao deserializar (como campos que não existem mais)
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Ignora propriedades vazias
        this.objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        
        // Garante que o diretório config existe
        Path configDir = Paths.get(CONFIG_DIR);
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                logger.info("Diretório de configuração criado: {}", configDir.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório de configuração", e);
        }
        
        this.jobsFilePath = configDir.resolve(JOBS_FILE);
    }
    
    public static synchronized JobConfigService getInstance() {
        if (instance == null) {
            instance = new JobConfigService();
        }
        return instance;
    }
    
    /**
     * Carrega todos os jobs salvos do arquivo JSON.
     * 
     * @return Lista de jobs ETL
     */
    public List<EtlJob> loadJobs() {
        List<EtlJob> jobs = new ArrayList<>();
        
        if (!Files.exists(jobsFilePath)) {
            logger.info("Arquivo de jobs não encontrado: {}. Retornando lista vazia.", 
                       jobsFilePath.toAbsolutePath());
            return jobs;
        }
        
        try {
            File file = jobsFilePath.toFile();
            if (file.length() == 0) {
                logger.info("Arquivo de jobs está vazio. Retornando lista vazia.");
                return jobs;
            }
            
            jobs = objectMapper.readValue(file, new TypeReference<List<EtlJob>>() {});
            logger.info("Carregados {} jobs do arquivo: {}", 
                      jobs.size(), jobsFilePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Erro ao carregar jobs do arquivo: {}", 
                        jobsFilePath.toAbsolutePath(), e);
        }
        
        return jobs;
    }
    
    /**
     * Salva uma lista de jobs no arquivo JSON.
     * 
     * @param jobs Lista de jobs ETL a serem salvos
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveJobs(List<EtlJob> jobs) {
        if (jobs == null) {
            logger.warn("Tentativa de salvar lista de jobs nula. Ignorando.");
            return false;
        }
        
        try {
            // Cria o arquivo se não existir
            if (!Files.exists(jobsFilePath)) {
                Files.createFile(jobsFilePath);
            }
            
            objectMapper.writeValue(jobsFilePath.toFile(), jobs);
            logger.info("Salvos {} jobs no arquivo: {}", 
                       jobs.size(), jobsFilePath.toAbsolutePath());
            return true;
            
        } catch (IOException e) {
            logger.error("Erro ao salvar jobs no arquivo: {}", 
                        jobsFilePath.toAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Adiciona ou atualiza um job na lista e salva no arquivo.
     * 
     * @param job Job ETL a ser adicionado/atualizado
     * @param allJobs Lista completa de jobs
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveJob(EtlJob job, List<EtlJob> allJobs) {
        if (job == null) {
            logger.warn("Tentativa de salvar job nulo. Ignorando.");
            return false;
        }
        
        // Verifica se já existe um job com o mesmo ID
        boolean updated = false;
        for (int i = 0; i < allJobs.size(); i++) {
            if (allJobs.get(i).getId().equals(job.getId())) {
                allJobs.set(i, job);
                updated = true;
                logger.debug("Job atualizado: {}", job.getId());
                break;
            }
        }
        
        if (!updated) {
            allJobs.add(job);
            logger.debug("Novo job adicionado: {}", job.getId());
        }
        
        return saveJobs(allJobs);
    }
    
    /**
     * Remove um job da lista e salva no arquivo.
     * 
     * @param jobId ID do job a ser removido
     * @param allJobs Lista completa de jobs
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean deleteJob(String jobId, List<EtlJob> allJobs) {
        if (jobId == null || jobId.trim().isEmpty()) {
            logger.warn("Tentativa de remover job com ID vazio. Ignorando.");
            return false;
        }
        
        boolean removed = allJobs.removeIf(job -> jobId.equals(job.getId()));
        
        if (removed) {
            logger.debug("Job removido: {}", jobId);
            return saveJobs(allJobs);
        } else {
            logger.warn("Job não encontrado para remoção: {}", jobId);
            return false;
        }
    }
    
    /**
     * Retorna o caminho do arquivo de jobs.
     * 
     * @return Caminho absoluto do arquivo
     */
    public Path getJobsFilePath() {
        return jobsFilePath.toAbsolutePath();
    }
}

