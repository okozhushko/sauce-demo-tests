package com.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyReader {

    private PropertyReader() {
    }

    public static Properties load(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = PropertyReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IllegalStateException("Properties file not found on classpath: " + fileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load properties file: " + fileName, e);
        }
        return properties;
    }
}
