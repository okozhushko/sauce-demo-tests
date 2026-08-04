package com.example.config;

public final class BrowserConfig {

    private BrowserConfig() {
    }

    public static String getBrowser() {
        return Config.getProperty("browser");
    }

    public static boolean isHeadless(String browser) {
        return Boolean.parseBoolean(Config.getProperty("browser." + browser + ".headless"));
    }
}
