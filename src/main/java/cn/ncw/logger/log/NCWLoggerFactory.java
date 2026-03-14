package cn.ncw.logger.log;

import java.util.HashMap;
import java.util.Map;

public class NCWLoggerFactory {

    private final String name;

    private final Logger logger;
    private final ThreadLogger threadLogger;

    private Map<String, ThreadLogger.LogLevel> logLevelMap = new HashMap<>();

    public NCWLoggerFactory(String name) {
        this.name = name;
        this.logger = new Logger(name);
        this.threadLogger = new ThreadLogger(name);

        this.logLevelMap.put("TRACE", ThreadLogger.LogLevel.TRACE);
        this.logLevelMap.put("INFO", ThreadLogger.LogLevel.INFO);
        this.logLevelMap.put("WARN", ThreadLogger.LogLevel.WARN);
        this.logLevelMap.put("ERROR", ThreadLogger.LogLevel.ERROR);
        this.logLevelMap.put("FATAL", ThreadLogger.LogLevel.FATAL);
        this.logLevelMap.put("DEBUG", ThreadLogger.LogLevel.DEBUG);
        this.logLevelMap.put("OFF", ThreadLogger.LogLevel.OFF);
    }

    public void log(String level, String message, String threadName) {
        this.log(level, message, threadName, null);
    }

    public void log(String level, String message, String threadName, Exception e) {
        this.logger.log(level, message, threadName);
        this.threadLogger.log(this.logLevelMap.get(level), message ,threadName, e);
    }

    public void off(String message, String threadName) {
        this.off(message, threadName, null);
    }

    public void off(String message, String threadName, Exception e) {
        this.log("OFF", message, threadName, e);
    }

    public void trace(String message, String threadName) {
        this.trace(message, threadName, null);
    }

    public void trace(String message, String threadName, Exception e) {
        this.log("TRACE", message, threadName, e);
    }

    public void info(String message, String threadName) {
        this.info(message, threadName, null);
    }

    public void info(String message, String threadName, Exception e) {
        this.log("INFO", message, threadName, e);
    }

    public void warn(String message, String threadName) {
        this.warn(message, threadName, null);
    }

    public void warn(String message, String threadName, Exception e) {
        this.log("WARN", message, threadName, e);
    }

    public void error(String message, String threadName) {
        this.error(message, threadName, null);
    }

    public void error(String message, String threadName, Exception e) {
        this.log("ERROR", message, threadName, e);
    }

    public void fatal(String message, String threadName) {
        this.fatal(message, threadName, null);
    }

    public void fatal(String message, String threadName, Exception e) {
        this.log("FATAL", message, threadName, e);
    }

    public void debug(String message, String threadName) {
        this.debug(message, threadName, null);
    }

    public void debug(String message, String threadName, Exception e) {
        this.log("DEBUG", message, threadName, e);
    }

    public String getName() {
        return name;
    }
}
