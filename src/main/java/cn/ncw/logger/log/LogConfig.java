package cn.ncw.logger.log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LogConfig {

    private Properties properties = new Properties();

    public LogConfig() {}

    public LogConfig(String configFile) throws IOException {
        load(configFile);
    }

    public LogConfig(File configFile) throws IOException {
        load(configFile);
    }

    public void load(String configFile) throws IOException {
        Path path = Paths.get(configFile);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("配置文件不存在: " + configFile);
        }
        try (InputStream is = Files.newInputStream(path)) {
            properties.load(is);
        }
    }

    public void load(File configFile) throws IOException {
        if (!configFile.exists()) {
            throw new FileNotFoundException("配置文件不存在: " + configFile.getPath());
        }
        try (InputStream is = new FileInputStream(configFile)) {
            properties.load(is);
        }
    }

    public void load(InputStream is) throws IOException {
        properties.load(is);
    }

    public void save(String configFile) throws IOException {
        try (OutputStream os = Files.newOutputStream(Paths.get(configFile))) {
            properties.store(os, "NCW-Logger Configuration");
        }
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public LEVEL getLevel(String key, LEVEL defaultLevel) {
        String val = properties.getProperty(key);
        if (val == null) return defaultLevel;
        return LEVEL.getLevel(val.trim().toUpperCase());
    }

    public Set<String> getAppenderNames() {
        return properties.stringPropertyNames();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getProperties() {
        return Map.copyOf((Map<String, String>)(Map<?,?>)properties);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void setProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void setProperty(String key, long value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void setProperty(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public static LogConfig fromResource(String resource) throws IOException {
        InputStream is = LogConfig.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + resource);
        }
        LogConfig config = new LogConfig();
        config.load(is);
        return config;
    }

    public static LogConfig defaultConfig() {
        LogConfig config = new LogConfig();
        config.setProperty("root.level", "INFO");
        config.setProperty("appender.console.type", "Console");
        config.setProperty("appender.console.layout", "%d %p [%t] %c - %m%n");
        config.setProperty("appender.console.color", "true");
        return config;
    }

    public String toConfigString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# NCW-Logger Configuration\n\n");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
