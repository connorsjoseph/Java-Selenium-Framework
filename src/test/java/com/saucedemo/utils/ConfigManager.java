package com.saucedemo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

    private ConfigManager() {
    }

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPERTIES = loadProperties();

    private static final String FALLBACK_BASE_URL = "https://www.saucedemo.com/";
    private static final String FALLBACK_GRID_URL = "http://localhost:4444/wd/hub";
    private static final String FALLBACK_EXECUTION_MODE = "local";
    private static final String FALLBACK_HEADLESS = "false";

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("WARNING: " + CONFIG_FILE + " not found on classpath, using defaults.");
            }
        } catch (IOException e) {
            System.err.println("WARNING: Failed to read " + CONFIG_FILE + ": " + e.getMessage());
        }
        return properties;
    }

    public static String getBaseUrl() {
        return resolve("base.url", "BASE_URL", FALLBACK_BASE_URL);
    }

    public static String getExecutionMode() {
        return resolve("execution.mode", "EXECUTION_MODE", FALLBACK_EXECUTION_MODE).trim().toLowerCase();
    }

    public static boolean isRemoteExecution() {
        return "remote".equals(getExecutionMode());
    }

    public static String getGridUrl() {
        return resolve("grid.url", "GRID_URL", FALLBACK_GRID_URL);
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(resolve("headless", "HEADLESS", FALLBACK_HEADLESS));
    }

    public static String getEdgeBinaryPath() {
        return resolve("edge.binary.path", "EDGE_BINARY_PATH", "");
    }

    private static String resolve(String propertiesKey, String envVarKey, String hardcodedFallback) {
        String envValue = System.getenv(envVarKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String systemPropertyValue = System.getProperty(propertiesKey);
        if (systemPropertyValue != null && !systemPropertyValue.isBlank()) {
            return systemPropertyValue;
        }

        String fileValue = PROPERTIES.getProperty(propertiesKey);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }

        return hardcodedFallback;
    }
}
