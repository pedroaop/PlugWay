package com.plugway.etl.config;

import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe para gerenciar propriedades da aplicação.
 * Carrega propriedades do arquivo application.properties.
 */
public class ApplicationProperties {
    
    private static final Logger logger = LoggerUtil.getLogger(ApplicationProperties.class);
    private static final String PROPERTIES_FILE = "application.properties";
    
    private static ApplicationProperties instance;
    private Properties properties;
    
    private ApplicationProperties() {
        loadProperties();
    }
    
    public static synchronized ApplicationProperties getInstance() {
        if (instance == null) {
            instance = new ApplicationProperties();
        }
        return instance;
    }
    
    /**
     * Carrega as propriedades do arquivo application.properties.
     */
    private void loadProperties() {
        properties = new Properties();
        
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            
            if (inputStream == null) {
                logger.warn("Arquivo {} não encontrado nos recursos. Usando propriedades padrão.", PROPERTIES_FILE);
                return;
            }
            
            properties.load(inputStream);
            logger.info("Propriedades carregadas de {}", PROPERTIES_FILE);
            
        } catch (IOException e) {
            logger.error("Erro ao carregar propriedades de " + PROPERTIES_FILE, e);
        }
    }
    
    /**
     * Retorna o valor de uma propriedade.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Retorna o valor de uma propriedade com valor padrão.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Retorna todas as propriedades.
     */
    public Properties getProperties() {
        return new Properties(properties);
    }
    
    /**
     * Define uma propriedade (não persiste no arquivo).
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}

