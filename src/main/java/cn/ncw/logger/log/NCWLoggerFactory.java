package cn.ncw.logger.log;

import java.util.ArrayList;
import java.util.List;

public class NCWLoggerFactory {

    private final String name;

    private final Logger logger;
    private final ThreadLogger threadLogger;

    private Integer length = 24;

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

        if (level.ordinal() >= this.logLevel.ordinal()) {
            this.logger.log(level, message, threadName);
            this.threadLogger.log(level, message, threadName, e);
        }

    }

    public void log(String level, String message, String threadName) {
        this.log(level, message, threadName, null);
    }

    public void log(String level, String message, String threadName, Exception e) {

        this.logger.log(LEVEL.getLevel(level), message, threadName);
        this.threadLogger.log(LEVEL.getLevel(level), message ,threadName, e);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message, String threadName) {
        this.off(message, threadName, null);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message, String threadName, Exception e) {
        this.log(LEVEL.OFF, message, threadName, e);
    }

    public void trace(String message, String threadName) {
        this.trace(message, threadName, null);
    }

    public void trace(String message, String threadName, Exception e) {
        this.log(LEVEL.TRACE, message, threadName, e);
    }

    public void info(String message, String threadName) {
        this.info(message, threadName, null);
    }

    public void info(String message, String threadName, Exception e) {
        this.log(LEVEL.INFO, message, threadName, e);
    }

    public void warn(String message, String threadName) {
        this.warn(message, threadName, null);
    }

    public void warn(String message, String threadName, Exception e) {
        this.log(LEVEL.WARN, message, threadName, e);
    }

    public void error(String message, String threadName) {
        this.error(message, threadName, null);
    }

    public void error(String message, String threadName, Exception e) {
        this.log(LEVEL.ERROR, message, threadName, e);
    }

    public void fatal(String message, String threadName) {
        this.fatal(message, threadName, null);
    }

    public void fatal(String message, String threadName, Exception e) {
        this.log(LEVEL.FATAL, message, threadName, e);
    }

    public void debug(String message, String threadName) {
        this.debug(message, threadName, null);
    }

    public void debug(String message, String threadName, Exception e) {
        this.log(LEVEL.DEBUG, message, threadName, e);
    }

    public void debug(String message) {
        debug(message, getName(), null);
    }

    public void info(String message) {
        info(message, getName(), null);
    }

    public void error(String message) {
        error(message, getName(), null);
    }

    public void warn(String message) {
        warn(message, getName(), null);
    }

    public void trace(String message) {
        trace(message, getName(), null);
    }

    public void fatal(String message) {
        fatal(message, getName(), null);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String message) {
        off(message, getName(), null);
    }

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

    public void log(LEVEL level, List<String> message, String threadName, Exception e) {

        if (level.ordinal() >= this.logLevel.ordinal()) {
            this.logger.log(level, message, threadName);
            this.threadLogger.log(level, message, threadName, e);
        }

    }

    public void log(String level, List<String> message, String threadName) {
        this.log(level, message, threadName, null);
    }

    public void log(String level, List<String> message, String threadName, Exception e) {

        this.logger.log(LEVEL.getLevel(level), message, threadName);
        this.threadLogger.log(LEVEL.getLevel(level), message ,threadName, e);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message, String threadName) {
        this.off(message, threadName, null);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.OFF, message, threadName, e);
    }

    public void trace(List<String> message, String threadName) {
        this.trace(message, threadName, null);
    }

    public void trace(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.TRACE, message, threadName, e);
    }

    public void info(List<String> message, String threadName) {
        this.info(message, threadName, null);
    }

    public void info(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.INFO, message, threadName, e);
    }

    public void warn(List<String> message, String threadName) {
        this.warn(message, threadName, null);
    }

    public void warn(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.WARN, message, threadName, e);
    }

    public void error(List<String> message, String threadName) {
        this.error(message, threadName, null);
    }

    public void error(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.ERROR, message, threadName, e);
    }

    public void fatal(List<String> message, String threadName) {
        this.fatal(message, threadName, null);
    }

    public void fatal(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.FATAL, message, threadName, e);
    }

    public void debug(List<String> message, String threadName) {
        this.debug(message, threadName, null);
    }

    public void debug(List<String> message, String threadName, Exception e) {
        this.log(LEVEL.DEBUG, message, threadName, e);
    }

    public void debug(List<String> message) {
        debug(message, getName(), null);
    }

    public void info(List<String> message) {
        info(message, getName(), null);
    }

    public void error(List<String> message) {
        error(message, getName(), null);
    }

    public void warn(List<String> message) {
        warn(message, getName(), null);
    }

    public void trace(List<String> message) {
        trace(message, getName(), null);
    }

    public void fatal(List<String> message) {
        fatal(message, getName(), null);
    }

    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * */
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(List<String> message) {
        off(message, getName(), null);
    }

    static void main() {
        NCWLoggerFactory logger = new NCWLoggerFactory("Test");
        List<String> messages = new ArrayList<String>();

        logger.setLogLevel(LEVEL.DEBUG);
        for (int i = 0; i < 10; i++) {
            messages.add("1145141919810");
        }

        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            logger.info("1145141919810", "main");
        }
        Long end = System.currentTimeMillis();
        logger.info(Long.valueOf(end-start).toString(), "main");
    }

}
