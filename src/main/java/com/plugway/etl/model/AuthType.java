package com.plugway.etl.model;

/**
 * Enum que representa os tipos de autenticação suportados para APIs REST.
 */
public enum AuthType {
    NONE("Nenhuma"),
    BEARER("Bearer Token"),
    API_KEY("API Key"),
    BASIC("Basic Authentication");
    
    private final String displayName;
    
    AuthType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retorna o AuthType baseado no nome (case-insensitive).
     */
    public static AuthType fromString(String name) {
        if (name == null) {
            return NONE;
        }
        for (AuthType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return NONE;
    }
}

