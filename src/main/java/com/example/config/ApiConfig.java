package com.example.config;

public final class ApiConfig {

    private ApiConfig() {
    }

    public static String getBaseUri() {
        return Config.getApiBaseUrl();
    }

    public static int getConnectionTimeoutMs() {
        return Config.getTimeout();
    }
}
