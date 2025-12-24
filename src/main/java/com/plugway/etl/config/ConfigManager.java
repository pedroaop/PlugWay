package com.plugway.etl.config;

import com.plugway.etl.util.LoggerUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gerenciador de configurações da aplicação.
 * Utiliza Typesafe Config para carregar e gerenciar configurações.
 */
public class ConfigManager {
    
    private static final Logger logger = LoggerUtil.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    
    private Config config;
    private Path configDirectory;
    
    private ConfigManager() {
        // Construtor privado para singleton
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Carrega as configurações da aplicação.
     * Primeiro tenta carregar de arquivo externo, depois usa recursos internos.
     */
    public void loadConfiguration() {
        try {
            // Diretório de configuração (mesmo diretório do executável ou ./config)
            configDirectory = Paths.get("config");
            
            // Criar diretório se não existir
            File configDir = configDirectory.toFile();
            if (!configDir.exists()) {
                configDir.mkdirs();
                logger.info("Diretório de configuração criado: {}", configDirectory);
            }
            
            // Tentar carregar arquivo de configuração externo
            File externalConfig = new File(configDirectory.toFile(), "application.conf");
            Config externalConfigObj = null;
            
            if (externalConfig.exists()) {
                logger.info("Carregando configuração externa: {}", externalConfig.getAbsolutePath());
                externalConfigObj = ConfigFactory.parseFile(externalConfig);
            }
            
            // Carregar configuração padrão dos recursos
            Config defaultConfig = ConfigFactory.parseResources("application.conf");
            
            // Mesclar configurações (externo sobrescreve padrão)
            if (externalConfigObj != null) {
                config = externalConfigObj.withFallback(defaultConfig);
            } else {
                config = defaultConfig;
            }
            
            // Resolver referências
            config = config.resolve();
            
            logger.info("Configurações carregadas com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro ao carregar configurações", e);
            // Usar configuração padrão mínima
            config = ConfigFactory.empty();
        }
    }
    
    /**
     * Retorna a configuração carregada.
     */
    public Config getConfig() {
        if (config == null) {
            loadConfiguration();
        }
        return config;
    }
    
    /**
     * Retorna o caminho do diretório de configuração.
     */
    public Path getConfigDirectory() {
        return configDirectory != null ? configDirectory : Paths.get("config");
    }
    
    /**
     * Retorna uma propriedade de configuração como String.
     */
    public String getString(String path) {
        return getConfig().getString(path);
    }
    
    /**
     * Retorna uma propriedade de configuração como String com valor padrão.
     */
    public String getString(String path, String defaultValue) {
        try {
            return getConfig().getString(path);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Retorna uma propriedade de configuração como int.
     */
    public int getInt(String path) {
        return getConfig().getInt(path);
    }
    
    /**
     * Retorna uma propriedade de configuração como int com valor padrão.
     */
    public int getInt(String path, int defaultValue) {
        try {
            return getConfig().getInt(path);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Retorna uma propriedade de configuração como boolean.
     */
    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }
    
    /**
     * Retorna uma propriedade de configuração como boolean com valor padrão.
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        try {
            return getConfig().getBoolean(path);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Verifica se uma propriedade existe na configuração.
     */
    public boolean hasPath(String path) {
        return getConfig().hasPath(path);
    }
}

