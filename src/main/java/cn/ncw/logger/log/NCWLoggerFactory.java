package cn.ncw.logger.log;

import java.util.ArrayList;
import java.util.List;

public class NCWLoggerFactory {

    private final String name;

    private final Logger logger;
    private final ThreadLogger threadLogger;

    private int length = 24;

    public void setLength(int value) {
        this.length = value;
        this.logger.setLength(value);
        this.threadLogger.setLength(value);
    }

    public int getLength() {
        return this.length;
    }

    private LEVEL logLevel = LEVEL.INFO;

    public NCWLoggerFactory(String name) {
        this.name = name;
        this.logger = new Logger(name);
        this.threadLogger = new ThreadLogger(name);
        this.logger.setLength(length);
        this.threadLogger.setLength(length);
    }

    public void log(LEVEL level, String message, String threadName, Exception e) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < this.logLevel.ordinal()) return;
        this.logger.log(level, message, threadName);
        this.threadLogger.log(level, message, threadName, e);
    }

    public void log(String level, String message, String threadName) {
        this.log(level, message, threadName, null);
    }

    public void log(String level, String message, String threadName, Exception e) {
        LEVEL resolved = LEVEL.getLevel(level);
        if (resolved == LEVEL.OFF) return;
        if (resolved.ordinal() < this.logLevel.ordinal()) return;
        this.logger.log(resolved, message, threadName);
        this.threadLogger.log(resolved, message, threadName, e);
    }

    public void log(LEVEL level, List<String> message, String threadName, Exception e) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < this.logLevel.ordinal()) return;
        this.logger.log(level, message, threadName);
        this.threadLogger.log(level, message, threadName, e);
    }

    public void log(String level, List<String> message, String threadName) {
        this.log(level, message, threadName, null);
    }

    public void log(String level, List<String> message, String threadName, Exception e) {
        LEVEL resolved = LEVEL.getLevel(level);
        if (resolved == LEVEL.OFF) return;
        if (resolved.ordinal() < this.logLevel.ordinal()) return;
        this.logger.log(resolved, message, threadName);
        this.threadLogger.log(resolved, message, threadName, e);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message, String threadName) {
        off(message, threadName, null);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message, String threadName, Exception e) {
        this.log(LEVEL.OFF, message, threadName, e);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message, String threadName) {
        off(message, threadName, null);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.OFF, message, threadName, e);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message) {
        off(message, getName(), null);
    }

    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message) {
        off(message, getName(), null);
    }

    public void trace(String message, String threadName) { this.log(LEVEL.TRACE, message, threadName, null); }
    public void trace(String message, String threadName, Exception e) { this.log(LEVEL.TRACE, message, threadName, e); }
    public void info(String message, String threadName)  { this.log(LEVEL.INFO, message, threadName, null); }
    public void info(String message, String threadName, Exception e)  { this.log(LEVEL.INFO, message, threadName, e); }
    public void warn(String message, String threadName)   { this.log(LEVEL.WARN, message, threadName, null); }
    public void warn(String message, String threadName, Exception e)   { this.log(LEVEL.WARN, message, threadName, e); }
    public void error(String message, String threadName)  { this.log(LEVEL.ERROR, message, threadName, null); }
    public void error(String message, String threadName, Exception e)  { this.log(LEVEL.ERROR, message, threadName, e); }
    public void fatal(String message, String threadName)  { this.log(LEVEL.FATAL, message, threadName, null); }
    public void fatal(String message, String threadName, Exception e)  { this.log(LEVEL.FATAL, message, threadName, e); }
    public void debug(String message, String threadName)  { this.log(LEVEL.DEBUG, message, threadName, null); }
    public void debug(String message, String threadName, Exception e)  { this.log(LEVEL.DEBUG, message, threadName, e); }

    public void trace(List<String> message, String threadName) { this.log(LEVEL.TRACE, message, threadName, null); }
    public void trace(List<String> message, String threadName, Exception e) { this.log(LEVEL.TRACE, message, threadName, e); }
    public void info(List<String> message, String threadName)  { this.log(LEVEL.INFO, message, threadName, null); }
    public void info(List<String> message, String threadName, Exception e)  { this.log(LEVEL.INFO, message, threadName, e); }
    public void warn(List<String> message, String threadName)   { this.log(LEVEL.WARN, message, threadName, null); }
    public void warn(List<String> message, String threadName, Exception e)   { this.log(LEVEL.WARN, message, threadName, e); }
    public void error(List<String> message, String threadName)  { this.log(LEVEL.ERROR, message, threadName, null); }
    public void error(List<String> message, String threadName, Exception e)  { this.log(LEVEL.ERROR, message, threadName, e); }
    public void fatal(List<String> message, String threadName)  { this.log(LEVEL.FATAL, message, threadName, null); }
    public void fatal(List<String> message, String threadName, Exception e)  { this.log(LEVEL.FATAL, message, threadName, e); }
    public void debug(List<String> message, String threadName)  { this.log(LEVEL.DEBUG, message, threadName, null); }
    public void debug(List<String> message, String threadName, Exception e)  { this.log(LEVEL.DEBUG, message, threadName, e); }

    public void trace(String message) { trace(message, getName(), null); }
    public void info(String message)  { info(message, getName(), null); }
    public void error(String message) { error(message, getName(), null); }
    public void warn(String message)  { warn(message, getName(), null); }
    public void debug(String message) { debug(message, getName(), null); }
    public void fatal(String message) { fatal(message, getName(), null); }

    public void trace(List<String> message) { trace(message, getName(), null); }
    public void info(List<String> message)  { info(message, getName(), null); }
    public void error(List<String> message) { error(message, getName(), null); }
    public void warn(List<String> message)  { warn(message, getName(), null); }
    public void debug(List<String> message) { debug(message, getName(), null); }
    public void fatal(List<String> message) { fatal(message, getName(), null); }

    public String getName() {
        return name;
    }

    public LEVEL getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LEVEL logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = LEVEL.getLevel(logLevel);
    }

    static void main() {
        NCWLoggerFactory logger = new NCWLoggerFactory("Test");

        logger.setLogLevel(LEVEL.DEBUG);
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add("1145141919810");
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            logger.info("1145141919810", "main");
        }
        long end = System.currentTimeMillis();
        logger.info(String.valueOf(end - start), "main");
    }
}
