package com.plugway.etl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.plugway.etl.model.ApiConfig;
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
 * Serviço para persistência de configurações de API REST em arquivo JSON.
 */
public class ApiConfigService {
    
    private static final Logger logger = LoggerUtil.getLogger(ApiConfigService.class);
    private static final String CONFIG_DIR = "config";
    private static final String APIS_FILE = "apis.json";
    
    private final ObjectMapper objectMapper;
    private final Path apisFilePath;
    
    private static ApiConfigService instance;
    
    private ApiConfigService() {
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
        
        this.apisFilePath = configDir.resolve(APIS_FILE);
    }
    
    public static synchronized ApiConfigService getInstance() {
        if (instance == null) {
            instance = new ApiConfigService();
        }
        return instance;
    }
    
    /**
     * Carrega todas as APIs salvas do arquivo JSON.
     * 
     * @return Lista de configurações de API
     */
    public List<ApiConfig> loadApis() {
        List<ApiConfig> apis = new ArrayList<>();
        
        if (!Files.exists(apisFilePath)) {
            logger.info("Arquivo de APIs não encontrado: {}. Retornando lista vazia.", 
                       apisFilePath.toAbsolutePath());
            return apis;
        }
        
        try {
            File file = apisFilePath.toFile();
            if (file.length() == 0) {
                logger.info("Arquivo de APIs está vazio. Retornando lista vazia.");
                return apis;
            }
            
            apis = objectMapper.readValue(file, new TypeReference<List<ApiConfig>>() {});
            logger.info("Carregadas {} APIs do arquivo: {}", 
                      apis.size(), apisFilePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Erro ao carregar APIs do arquivo: {}", 
                        apisFilePath.toAbsolutePath(), e);
        }
        
        return apis;
    }
    
    /**
     * Salva uma lista de APIs no arquivo JSON.
     * 
     * @param apis Lista de configurações de API a serem salvas
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveApis(List<ApiConfig> apis) {
        if (apis == null) {
            logger.warn("Tentativa de salvar lista de APIs nula. Ignorando.");
            return false;
        }
        
        try {
            // Cria o arquivo se não existir
            if (!Files.exists(apisFilePath)) {
                Files.createFile(apisFilePath);
            }
            
            objectMapper.writeValue(apisFilePath.toFile(), apis);
            logger.info("Salvas {} APIs no arquivo: {}", 
                       apis.size(), apisFilePath.toAbsolutePath());
            return true;
            
        } catch (IOException e) {
            logger.error("Erro ao salvar APIs no arquivo: {}", 
                        apisFilePath.toAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Adiciona ou atualiza uma API na lista e salva no arquivo.
     * 
     * @param api Configuração de API a ser adicionada/atualizada
     * @param allApis Lista completa de APIs
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveApi(ApiConfig api, List<ApiConfig> allApis) {
        if (api == null) {
            logger.warn("Tentativa de salvar API nula. Ignorando.");
            return false;
        }
        
        // Verifica se já existe uma API com o mesmo nome
        boolean updated = false;
        for (int i = 0; i < allApis.size(); i++) {
            if (allApis.get(i).getName().equals(api.getName())) {
                allApis.set(i, api);
                updated = true;
                logger.debug("API atualizada: {}", api.getName());
                break;
            }
        }
        
        if (!updated) {
            allApis.add(api);
            logger.debug("Nova API adicionada: {}", api.getName());
        }
        
        return saveApis(allApis);
    }
    
    /**
     * Remove uma API da lista e salva no arquivo.
     * 
     * @param apiName Nome da API a ser removida
     * @param allApis Lista completa de APIs
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean deleteApi(String apiName, List<ApiConfig> allApis) {
        if (apiName == null || apiName.trim().isEmpty()) {
            logger.warn("Tentativa de remover API com nome vazio. Ignorando.");
            return false;
        }
        
        boolean removed = allApis.removeIf(api -> apiName.equals(api.getName()));
        
        if (removed) {
            logger.debug("API removida: {}", apiName);
            return saveApis(allApis);
        } else {
            logger.warn("API não encontrada para remoção: {}", apiName);
            return false;
        }
    }
    
    /**
     * Retorna o caminho do arquivo de APIs.
     * 
     * @return Caminho absoluto do arquivo
     */
    public Path getApisFilePath() {
        return apisFilePath.toAbsolutePath();
    }
}

