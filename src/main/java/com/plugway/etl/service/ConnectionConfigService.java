package com.plugway.etl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.plugway.etl.model.DatabaseConfig;
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
 * Serviço para persistência de configurações de conexão em arquivo JSON.
 */
public class ConnectionConfigService {
    
    private static final Logger logger = LoggerUtil.getLogger(ConnectionConfigService.class);
    private static final String CONFIG_DIR = "config";
    private static final String CONNECTIONS_FILE = "connections.json";
    
    private final ObjectMapper objectMapper;
    private final Path connectionsFilePath;
    
    private static ConnectionConfigService instance;
    
    private ConnectionConfigService() {
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
        
        this.connectionsFilePath = configDir.resolve(CONNECTIONS_FILE);
    }
    
    public static synchronized ConnectionConfigService getInstance() {
        if (instance == null) {
            instance = new ConnectionConfigService();
        }
        return instance;
    }
    
    /**
     * Carrega todas as conexões salvas do arquivo JSON.
     * 
     * @return Lista de configurações de conexão
     */
    public List<DatabaseConfig> loadConnections() {
        List<DatabaseConfig> connections = new ArrayList<>();
        
        if (!Files.exists(connectionsFilePath)) {
            logger.info("Arquivo de conexões não encontrado: {}. Retornando lista vazia.", 
                       connectionsFilePath.toAbsolutePath());
            return connections;
        }
        
        try {
            File file = connectionsFilePath.toFile();
            if (file.length() == 0) {
                logger.info("Arquivo de conexões está vazio. Retornando lista vazia.");
                return connections;
            }
            
            connections = objectMapper.readValue(file, new TypeReference<List<DatabaseConfig>>() {});
            logger.info("Carregadas {} conexões do arquivo: {}", 
                      connections.size(), connectionsFilePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Erro ao carregar conexões do arquivo: {}", 
                        connectionsFilePath.toAbsolutePath(), e);
        }
        
        return connections;
    }
    
    /**
     * Salva uma lista de conexões no arquivo JSON.
     * 
     * @param connections Lista de configurações de conexão a serem salvas
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveConnections(List<DatabaseConfig> connections) {
        if (connections == null) {
            logger.warn("Tentativa de salvar lista de conexões nula. Ignorando.");
            return false;
        }
        
        try {
            // Cria o arquivo se não existir
            if (!Files.exists(connectionsFilePath)) {
                Files.createFile(connectionsFilePath);
            }
            
            objectMapper.writeValue(connectionsFilePath.toFile(), connections);
            logger.info("Salvas {} conexões no arquivo: {}", 
                       connections.size(), connectionsFilePath.toAbsolutePath());
            return true;
            
        } catch (IOException e) {
            logger.error("Erro ao salvar conexões no arquivo: {}", 
                        connectionsFilePath.toAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Adiciona ou atualiza uma conexão na lista e salva no arquivo.
     * 
     * @param connection Configuração de conexão a ser adicionada/atualizada
     * @param allConnections Lista completa de conexões
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean saveConnection(DatabaseConfig connection, List<DatabaseConfig> allConnections) {
        if (connection == null) {
            logger.warn("Tentativa de salvar conexão nula. Ignorando.");
            return false;
        }
        
        // Verifica se já existe uma conexão com o mesmo nome
        boolean updated = false;
        for (int i = 0; i < allConnections.size(); i++) {
            if (allConnections.get(i).getName().equals(connection.getName())) {
                allConnections.set(i, connection);
                updated = true;
                logger.debug("Conexão atualizada: {}", connection.getName());
                break;
            }
        }
        
        if (!updated) {
            allConnections.add(connection);
            logger.debug("Nova conexão adicionada: {}", connection.getName());
        }
        
        return saveConnections(allConnections);
    }
    
    /**
     * Remove uma conexão da lista e salva no arquivo.
     * 
     * @param connectionName Nome da conexão a ser removida
     * @param allConnections Lista completa de conexões
     * @return true se salvou com sucesso, false caso contrário
     */
    public boolean deleteConnection(String connectionName, List<DatabaseConfig> allConnections) {
        if (connectionName == null || connectionName.trim().isEmpty()) {
            logger.warn("Tentativa de remover conexão com nome vazio. Ignorando.");
            return false;
        }
        
        boolean removed = allConnections.removeIf(conn -> connectionName.equals(conn.getName()));
        
        if (removed) {
            logger.debug("Conexão removida: {}", connectionName);
            return saveConnections(allConnections);
        } else {
            logger.warn("Conexão não encontrada para remoção: {}", connectionName);
            return false;
        }
    }
    
    /**
     * Retorna o caminho do arquivo de conexões.
     * 
     * @return Caminho absoluto do arquivo
     */
    public Path getConnectionsFilePath() {
        return connectionsFilePath.toAbsolutePath();
    }
}

