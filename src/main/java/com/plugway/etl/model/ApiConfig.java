package com.plugway.etl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de API REST para envio de dados.
 */
public class ApiConfig {
    
    private String name;
    private String baseUrl;
    private String endpoint;
    private String method; // POST, PUT, PATCH
    private AuthType authType;
    private String authToken;
    private String apiKey;
    private String apiKeyHeader;
    private String username;
    private String password;
    private Map<String, String> headers;
    private int timeout;
    private int maxRetries;
    private long retryDelay;
    private boolean useExponentialBackoff;
    
    public ApiConfig() {
        this.method = "POST";
        this.authType = AuthType.NONE;
        this.headers = new HashMap<>();
        this.timeout = 30000; // 30 segundos
        this.maxRetries = 3;
        this.retryDelay = 1000; // 1 segundo
        this.useExponentialBackoff = true;
    }
    
    /**
     * Constrói a URL completa do endpoint.
     */
    public String buildFullUrl() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Base URL is not set");
        }
        
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String endpointPath = endpoint != null ? endpoint : "";
        
        if (!endpointPath.isEmpty() && !endpointPath.startsWith("/")) {
            endpointPath = "/" + endpointPath;
        }
        
        return base + endpointPath;
    }
    
    /**
     * Valida se a configuração está completa.
     */
    @JsonIgnore
    public boolean isValid() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }
        
        if (method == null || method.trim().isEmpty()) {
            return false;
        }
        
        // Valida autenticação baseada no tipo
        if (authType != null && authType != AuthType.NONE) {
            switch (authType) {
                case BEARER:
                    return authToken != null && !authToken.trim().isEmpty();
                case API_KEY:
                    return apiKey != null && !apiKey.trim().isEmpty() &&
                           apiKeyHeader != null && !apiKeyHeader.trim().isEmpty();
                case BASIC:
                    return username != null && !username.trim().isEmpty() &&
                           password != null && !password.trim().isEmpty();
                default:
                    return true;
            }
        }
        
        return true;
    }
    
    // Getters e Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method != null ? method.toUpperCase() : "POST";
    }
    
    public AuthType getAuthType() {
        return authType;
    }
    
    public void setAuthType(AuthType authType) {
        this.authType = authType != null ? authType : AuthType.NONE;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKeyHeader() {
        return apiKeyHeader;
    }
    
    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }
    
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public String getHeader(String key) {
        return this.headers.get(key);
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout > 0 ? timeout : 30000;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries >= 0 ? maxRetries : 3;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay > 0 ? retryDelay : 1000;
    }
    
    public boolean isUseExponentialBackoff() {
        return useExponentialBackoff;
    }
    
    public void setUseExponentialBackoff(boolean useExponentialBackoff) {
        this.useExponentialBackoff = useExponentialBackoff;
    }
    
    @Override
    public String toString() {
        return "ApiConfig{" +
                "name='" + name + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", authType=" + authType +
                '}';
    }
}

