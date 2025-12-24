package com.plugway.etl.service.load;

import com.plugway.etl.model.ApiConfig;
import com.plugway.etl.model.AuthType;
import com.plugway.etl.util.LoggerUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Cliente HTTP para comunicação com APIs REST.
 * Utiliza o HttpClient nativo do Java 11+.
 */
public class RestApiClient {
    
    private static final Logger logger = LoggerUtil.getLogger(RestApiClient.class);
    
    private final HttpClient httpClient;
    private final ApiConfig config;
    
    public RestApiClient(ApiConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
    }
    
    /**
     * Cria e configura o HttpClient.
     */
    private HttpClient createHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL);
        
        return builder.build();
    }
    
    /**
     * Envia dados JSON para o endpoint configurado.
     * 
     * @param jsonData Dados JSON como string
     * @return HttpResponse com a resposta do servidor
     * @throws IOException Se ocorrer erro de I/O
     * @throws InterruptedException Se a requisição for interrompida
     */
    public HttpResponse<String> sendJson(String jsonData) throws IOException, InterruptedException {
        return sendJson(jsonData, null);
    }
    
    /**
     * Envia dados JSON com headers adicionais.
     * 
     * @param jsonData Dados JSON como string
     * @param additionalHeaders Headers adicionais (sobrescrevem os da configuração)
     * @return HttpResponse com a resposta do servidor
     * @throws IOException Se ocorrer erro de I/O
     * @throws InterruptedException Se a requisição for interrompida
     */
    public HttpResponse<String> sendJson(String jsonData, Map<String, String> additionalHeaders) 
            throws IOException, InterruptedException {
        
        String url = config.buildFullUrl();
        logger.debug("Enviando JSON para: {} | Método: {}", url, config.getMethod());
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(config.getTimeout()))
                .header("Content-Type", "application/json");
        
        // Adiciona headers da configuração
        for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }
        
        // Adiciona headers adicionais (sobrescrevem os da configuração)
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }
        
        // Adiciona autenticação
        addAuthentication(requestBuilder);
        
        // Configura o método HTTP e body
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers
                .ofString(jsonData, StandardCharsets.UTF_8);
        
        switch (config.getMethod().toUpperCase()) {
            case "POST":
                requestBuilder.POST(bodyPublisher);
                break;
            case "PUT":
                requestBuilder.PUT(bodyPublisher);
                break;
            case "PATCH":
                requestBuilder.method("PATCH", bodyPublisher);
                break;
            default:
                throw new IllegalArgumentException("Método HTTP não suportado: " + config.getMethod());
        }
        
        HttpRequest request = requestBuilder.build();
        
        // Envia a requisição
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.debug("Resposta recebida: {} | Status: {}", url, response.statusCode());
        
        return response;
    }
    
    /**
     * Adiciona autenticação ao request baseado na configuração.
     */
    private void addAuthentication(HttpRequest.Builder requestBuilder) {
        switch (config.getAuthType()) {
            case BEARER:
                if (config.getAuthToken() != null && !config.getAuthToken().trim().isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + config.getAuthToken());
                    logger.debug("Autenticação Bearer adicionada");
                }
                break;
                
            case API_KEY:
                if (config.getApiKey() != null && config.getApiKeyHeader() != null) {
                    requestBuilder.header(config.getApiKeyHeader(), config.getApiKey());
                    logger.debug("API Key adicionada no header: {}", config.getApiKeyHeader());
                }
                break;
                
            case BASIC:
                if (config.getUsername() != null && config.getPassword() != null) {
                    String credentials = config.getUsername() + ":" + config.getPassword();
                    String encoded = Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8));
                    requestBuilder.header("Authorization", "Basic " + encoded);
                    logger.debug("Autenticação Basic adicionada");
                }
                break;
                
            case NONE:
            default:
                // Sem autenticação
                break;
        }
    }
    
    /**
     * Testa a conexão com a API (faz uma requisição GET simples).
     */
    public boolean testConnection() {
        try {
            String url = config.buildFullUrl();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Considera sucesso se retornar qualquer status (mesmo 404 indica que a API está acessível)
            logger.info("Teste de conexão: {} | Status: {}", url, response.statusCode());
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao testar conexão com API: {}", config.getName(), e);
            return false;
        }
    }
    
    public ApiConfig getConfig() {
        return config;
    }
}

