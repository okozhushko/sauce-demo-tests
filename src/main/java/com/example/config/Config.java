package com.example.config;

import com.example.utils.PropertyReader;

import java.util.Properties;

public final class Config {

    private static final String BASE_PROPERTIES_FILE = "properties/application.properties";
    private static final Properties PROPERTIES = loadMerged();

    private Config() {
    }

    private static Properties loadMerged() {
        Properties merged = new Properties();
        merged.putAll(PropertyReader.load(BASE_PROPERTIES_FILE));

        String environment = System.getProperty("environment", merged.getProperty("environment", "dev"));
        String envFile = "properties/" + environment + ".properties";
        try {
            merged.putAll(PropertyReader.load(envFile));
        } catch (IllegalStateException ignored) {
            // no environment-specific overrides for this environment, base properties stand
        }
        return merged;
    }

    public static String getProperty(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static int getTimeout() {
        return Integer.parseInt(getProperty("timeout"));
    }

    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page.load.timeout"));
    }

    public static int getRetryCount() {
        return Integer.parseInt(getProperty("retry.count"));
    }
}
