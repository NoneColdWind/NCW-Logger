package cn.ncw.logger.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class NCWLogger {

    private final String name;
    private final List<Appender> appenders = new CopyOnWriteArrayList<>();
    private volatile LEVEL logLevel = LEVEL.INFO;
    private volatile boolean additive = true;
    private final NCWLogger parent;

    private static final Map<String, NCWLogger> LOGGER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile NCWLogger ROOT = createRoot();

    private NCWLogger(String name, NCWLogger parent) {
        this.name = name;
        this.parent = parent;
    }

    private static NCWLogger createRoot() {
        NCWLogger root = new NCWLogger("root", null);
        root.addAppender(Appender.console("root-console", Layout.defaultLayout()));
        return root;
    }

    public static NCWLogger getLogger() {
        return getLogger("app");
    }

    public static NCWLogger getLogger(String name) {
        return LOGGER_CACHE.computeIfAbsent(name, n -> {
            String parentName = getParentName(n);
            NCWLogger parentLogger = parentName != null ? getLogger(parentName) : ROOT;
            return new NCWLogger(n, parentLogger);
        });
    }

    public static NCWLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private static String getParentName(String name) {
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : null;
    }

    public static void configure(LogConfig config) {
        String rootLevel = config.getString("root.level", "INFO");
        ROOT.setLevel(LEVEL.getLevel(rootLevel));

        String consolePattern = config.getString("appender.console.layout", "%d %p [%t] %c - %m%n");
        boolean consoleColor = config.getBoolean("appender.console.color", true);
        boolean consoleEnabled = config.getBoolean("appender.console.enabled", true);

        if (consoleEnabled) {
            ROOT.addAppender(Appender.console("console",
                    new Layout.PatternLayout(consolePattern, consoleColor)));
        }

        String fileDir = config.getString("appender.file.directory", "./logs");
        String filePattern = config.getString("appender.file.pattern", "{timestamp}.log");
        boolean fileEnabled = config.getBoolean("appender.file.enabled", false);

        if (fileEnabled) {
            Layout fileLayout = new Layout.PatternLayout(
                    config.getString("appender.file.layout", "%d %p [%t] %c - %m%n"));
            ROOT.addAppender(Appender.file("file", fileLayout, fileDir, filePattern));
        }
    }

    public static void configure(String configFile) throws java.io.IOException {
        configure(new LogConfig(configFile));
    }

    public void addAppender(Appender appender) {
        if (appender != null && !appenders.contains(appender)) {
            appenders.add(appender);
        }
    }

    public void removeAppender(Appender appender) {
        appenders.remove(appender);
    }

    public void clearAppenders() {
        appenders.clear();
    }

    public List<Appender> getAppenders() {
        return List.copyOf(appenders);
    }

    public NCWLogger getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public LEVEL getLevel() {
        return logLevel;
    }

    public void setLevel(LEVEL level) {
        this.logLevel = level;
    }

    public void setLevel(String level) {
        this.logLevel = LEVEL.getLevel(level);
    }

    public boolean isAdditive() {
        return additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public void trace(String message) {
        trace(message, (Object[]) null);
    }

    public void trace(String message, Object... args) {
        log(LEVEL.TRACE, message, args, null);
    }

    public void trace(String message, Throwable t) {
        log(LEVEL.TRACE, message, null, t);
    }

    public void trace(String message, Throwable t, Object... args) {
        log(LEVEL.TRACE, message, args, t);
    }

    public void debug(String message) {
        debug(message, (Object[]) null);
    }

    public void debug(String message, Object... args) {
        log(LEVEL.DEBUG, message, args, null);
    }

    public void debug(String message, Throwable t) {
        log(LEVEL.DEBUG, message, null, t);
    }

    public void info(String message) {
        info(message, (Object[]) null);
    }

    public void info(String message, Object... args) {
        log(LEVEL.INFO, message, args, null);
    }

    public void info(String message, Throwable t) {
        log(LEVEL.INFO, message, null, t);
    }

    public void warn(String message) {
        warn(message, (Object[]) null);
    }

    public void warn(String message, Object... args) {
        log(LEVEL.WARN, message, args, null);
    }

    public void warn(String message, Throwable t) {
        log(LEVEL.WARN, message, null, t);
    }

    public void error(String message) {
        error(message, (Object[]) null);
    }

    public void error(String message, Object... args) {
        log(LEVEL.ERROR, message, args, null);
    }

    public void error(String message, Throwable t) {
        log(LEVEL.ERROR, message, null, t);
    }

    public void fatal(String message) {
        fatal(message, (Object[]) null);
    }

    public void fatal(String message, Object... args) {
        log(LEVEL.FATAL, message, args, null);
    }

    public void fatal(String message, Throwable t) {
        log(LEVEL.FATAL, message, null, t);
    }

    public void log(LEVEL level, String message) {
        log(level, message, null, null);
    }

    public void log(LEVEL level, String message, Throwable t) {
        log(level, message, null, t);
    }

    public void log(LEVEL level, String message, Object[] args) {
        log(level, message, args, null);
    }

    public void log(LEVEL level, String message, Object[] args, Throwable t) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < effectiveLevel().ordinal()) return;

        CallerInfo callerInfo = extractCallerInfo();

        String formattedMessage = (args != null && args.length > 0)
                ? Formatter.format((String) message, args)
                : message;

        if (t != null) {
            formattedMessage += "\n" + Formatter.formatException(t);
        }

        LogEvent event = LogEvent.builder()
                .level(level)
                .loggerName(name)
                .message(formattedMessage)
                .threadName(Thread.currentThread().getName())
                .throwable(t)
                .callerClass(callerInfo.className)
                .callerMethod(callerInfo.methodName)
                .callerLine(callerInfo.lineNumber)
                .build();

        for (Appender appender : appenders) {
            if (appender.isActive()) {
                appender.append(event);
            }
        }

        if (additive && parent != null) {
            parent.log(level, formattedMessage, args, t);
        }

        LogMetrics.increment(level);
    }

    public void log(LEVEL level, List<String> messages) {
        log(level, messages, null);
    }

    public void log(LEVEL level, List<String> messages, Throwable t) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < effectiveLevel().ordinal()) return;

        StringBuilder sb = new StringBuilder();
        messages.forEach(m -> {
            if (sb.length() > 0) sb.append("\n");
            sb.append(m);
        });

        log(level, sb.toString(), t);
    }

    private LEVEL effectiveLevel() {
        if (logLevel != LEVEL.OFF) {
            return logLevel;
        }
        if (parent != null) {
            return parent.effectiveLevel();
        }
        return LEVEL.INFO;
    }

    private CallerInfo extractCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement e = stackTrace[i];
            String className = e.getClassName();
            if (!className.startsWith("cn.ncw.logger.log.NCWLogger") &&
                !className.startsWith("cn.ncw.logger.log.Appender") &&
                !className.startsWith("java.lang.Thread")) {
                return new CallerInfo(
                        getSimpleClassName(className),
                        e.getMethodName(),
                        String.valueOf(e.getLineNumber())
                );
            }
        }
        return new CallerInfo("unknown", "unknown", "");
    }

    private String getSimpleClassName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot > 0 ? fullName.substring(lastDot + 1) : fullName;
    }

    public void trace(List<String> messages) {
        log(LEVEL.TRACE, messages);
    }

    public void debug(List<String> messages) {
        log(LEVEL.DEBUG, messages);
    }

    public void info(List<String> messages) {
        log(LEVEL.INFO, messages);
    }

    public void warn(List<String> messages) {
        log(LEVEL.WARN, messages);
    }

    public void error(List<String> messages) {
        log(LEVEL.ERROR, messages);
    }

    public void fatal(List<String> messages) {
        log(LEVEL.FATAL, messages);
    }

    public static void main(String[] args) throws Exception {
        NCWLogger logger = NCWLogger.getLogger("TestLogger");
        logger.setLevel(LEVEL.DEBUG);

        MDC.put("user", "Alice");
        MDC.put("requestId", "req-12345");

        logger.info("Application started");
        logger.debug("Debug message with {} param {}", "test", 123);
        logger.info("User {} logged in", "Alice");
        logger.warn("Warning: something might be wrong");
        logger.error("Error occurred", new RuntimeException("Test exception"));

        try {
            throw new IllegalStateException("Something went wrong");
        } catch (Exception e) {
            logger.error("Caught exception in try block", e);
        }

        logger.fatal("Fatal error: system shutting down");

        System.out.println("\n" + LogMetrics.getSummary());

        logger.info("Log event count by level: TRACE={}, DEBUG={}, INFO={}, WARN={}, ERROR={}, FATAL={}",
                LogMetrics.getCount(LEVEL.TRACE),
                LogMetrics.getCount(LEVEL.DEBUG),
                LogMetrics.getCount(LEVEL.INFO),
                LogMetrics.getCount(LEVEL.WARN),
                LogMetrics.getCount(LEVEL.ERROR),
                LogMetrics.getCount(LEVEL.FATAL));

        System.out.println("\nJSON Output Example:");
        NCWLogger jsonLogger = NCWLogger.getLogger("JsonLogger");
        jsonLogger.clearAppenders();
        jsonLogger.addAppender(new Appender.ConsoleAppender("json-console",
                new Layout.JsonLayout(true, true, true)) {});
        jsonLogger.info("This is a JSON formatted log message");

        System.out.println("\nMDC Context: " + MDC.formatContext());
        MDC.clear();
    }

    private record CallerInfo(String className, String methodName, String lineNumber) {}
}
