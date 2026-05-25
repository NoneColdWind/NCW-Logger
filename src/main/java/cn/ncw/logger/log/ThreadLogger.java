package cn.ncw.logger.log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ThreadLogger {

    private final String appName;

    private String format;

    private int length = 24;

    public void setLength(int value) {
        this.length = value;
        this.format = buildFormat(value);
    }

    public int getLength() {
        return this.length;
    }

    private static String buildFormat(int len) {
        return "%s [%-5s] %-" + len + "s %s";
    }

    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>(5000);
    private volatile boolean isRunning = true;
    private PrintWriter logWriter;
    private final ReentrantLock writerLock = new ReentrantLock();

    private volatile String logDirectory = "./logs";
    private volatile String logFileNamePattern;
    private volatile LogFileStrategy fileStrategy = LogFileStrategy.DAILY;
    private volatile long maxFileSize = 50 * 1024 * 1024;
    private volatile int maxFiles = 10;
    private volatile LEVEL currentLogLevel = LEVEL.INFO;

    private Path currentLogFile;
    private volatile long currentFileSize;
    private volatile Instant lastRotationCheck = Instant.now();

    public enum LogFileStrategy {
        SINGLE_FILE,
        DAILY,
        WEEKLY,
        SIZE_BASED,
        HOURLY
    }

    public ThreadLogger(String appName) {
        this.appName = appName;
        this.format = buildFormat(this.length);
        this.logFileNamePattern = appName + "_{timestamp}.log";
        Runtime.getRuntime().addShutdownHook(new Thread(this::safeShutdown));
        initializeLogger();
        startLoggerThread();
    }

    public synchronized void configure(Consumer<LogConfigBuilder> configurator) {
        LogConfigBuilder builder = new LogConfigBuilder();
        configurator.accept(builder);
        applyConfiguration(builder);

        if (logWriter != null) {
            rotateLogFile();
        }
    }

    private void applyConfiguration(LogConfigBuilder builder) {
        if (builder.logDirectory != null) {
            this.logDirectory = builder.logDirectory;
        }
        if (builder.logFileNamePattern != null) {
            this.logFileNamePattern = builder.logFileNamePattern;
        }
        if (builder.fileStrategy != null) {
            this.fileStrategy = builder.fileStrategy;
        }
        if (builder.maxFileSize > 0) {
            this.maxFileSize = builder.maxFileSize;
        }
        if (builder.maxFiles > 0) {
            this.maxFiles = builder.maxFiles;
        }
        if (builder.logLevel != null) {
            this.currentLogLevel = builder.logLevel;
        }
    }

    private void initializeLogger() {
        try {
            Files.createDirectories(Paths.get(logDirectory));
            createNewLogFile();
        } catch (IOException e) {
            System.err.println("日志初始化失败: " + e.getMessage());
        }
    }

    private void createNewLogFile() {
        writerLock.lock();
        try {
            if (logWriter != null) {
                try {
                    logWriter.flush();
                    logWriter.close();
                } catch (Exception e) {
                    System.err.println("关闭日志文件失败: " + e.getMessage());
                }
            }

            createLogDirectory();

            String fileName = resolveValidFileName();
            Path filePath = Paths.get(logDirectory, fileName);

            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                } catch (FileAlreadyExistsException e) {
                    // concurrent creation is fine
                }
            }

            currentLogFile = filePath;
            currentFileSize = Files.size(filePath);

            logWriter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(filePath.toFile(), true),
                                    StandardCharsets.UTF_8
                            )
                    ),
                    true
            );

        } catch (Exception e) {
            System.err.println("创建日志文件失败: " + e.getMessage());
            createFallbackLogFile(e);
        } finally {
            writerLock.unlock();
        }
    }

    private void createLogDirectory() throws IOException {
        Path logDirPath = Paths.get(logDirectory);

        if (!Files.exists(logDirPath)) {
            try {
                Files.createDirectories(logDirPath);
            } catch (FileAlreadyExistsException e) {
                // concurrent creation is fine
            } catch (Exception e) {
                if (!Files.exists(logDirPath)) {
                    logDirectory = "./logs_fallback";
                    logDirPath = Paths.get(logDirectory);
                    Files.createDirectories(logDirPath);
                }
            }
        }

        File dir = logDirPath.toFile();
        if (!dir.canWrite()) {
            throw new IOException("目录不可写: " + logDirectory);
        }
    }

    private String resolveValidFileName() {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String safePattern = logFileNamePattern
                .replace("\\", "_")
                .replace("/", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_");

        return safePattern
                .replace("{timestamp}", timestamp)
                .replace("{date}", LocalDate.now().toString());
    }

    private void createFallbackLogFile(Exception originalError) {
        try {
            Path tempDir = Files.createTempDirectory("logger_fallback_");
            Path fallbackFile = tempDir.resolve("error_" + System.currentTimeMillis() + ".log");

            try (FileWriter fw = new FileWriter(fallbackFile.toFile(), StandardCharsets.UTF_8)) {
                fw.write("日志系统初始化失败:\n");
                fw.write("原始错误: " + originalError + "\n");
                fw.write("当前时间: " + new Date() + "\n");

                for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                    fw.write(entry.getKey() + " = " + entry.getValue() + "\n");
                }
            }

            System.err.println("创建了错误日志文件: " + fallbackFile);

            logWriter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(fallbackFile.toFile(), true),
                                    StandardCharsets.UTF_8
                            )
                    ),
                    true
            );
            currentLogFile = fallbackFile;
            currentFileSize = 0;
        } catch (Exception ex) {
            System.err.println("严重错误: 无法创建日志文件 - " + ex.getMessage());
        }
    }

    private String resolveFileName() {
        DateTimeFormatter formatter = switch (fileStrategy) {
            case WEEKLY -> DateTimeFormatter.ofPattern("yyyy-'w'ww");
            case HOURLY -> DateTimeFormatter.ofPattern("yyyyMMdd_HH");
            case SIZE_BASED -> DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            default -> DateTimeFormatter.ofPattern("yyyyMMdd");
        };

        String timestamp = LocalDateTime.now().format(formatter);
        return logFileNamePattern.replace("{timestamp}", timestamp);
    }

    private void startLoggerThread() {
        Thread loggerThread = new Thread(this::processLogs, "Logger-Thread");
        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    public void shutdown() {
        isRunning = false;
        safeShutdown();
    }

    private void safeShutdown() {
        writerLock.lock();
        try {
            if (logWriter != null) {
                while (!logQueue.isEmpty()) {
                    LogEntry entry = logQueue.poll();
                    if (entry != null) {
                        writeToLog(entry);
                    }
                }
                logWriter.flush();
                logWriter.close();
                logWriter = null;
            }
        } finally {
            writerLock.unlock();
        }
    }

    public void log(LEVEL level, String message, String threadName, Throwable throwable) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < currentLogLevel.ordinal()) return;
        try {
            logQueue.put(new LogEntry(level, message, appName + ":" + threadName, throwable));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("日志队列已满：" + message);
        }
    }

    public void log(LEVEL level, List<String> texts, String threadName, Throwable throwable) {
        if (level == LEVEL.OFF) return;
        if (level.ordinal() < currentLogLevel.ordinal()) return;
        String message = joinTexts(texts);
        try {
            logQueue.put(new LogEntry(level, message, appName + ":" + threadName, throwable));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("日志队列已满：" + message);
        }
    }

    private void processLogs() {
        while (isRunning || !logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    checkRotationNeeded();
                    writeToLog(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        while (!logQueue.isEmpty()) {
            writeToLog(logQueue.poll());
        }
    }

    private void checkRotationNeeded() {
        if (fileStrategy == LogFileStrategy.SINGLE_FILE) return;

        boolean needRotation = false;
        Instant now = Instant.now();

        needRotation = switch (fileStrategy) {
            case DAILY -> Duration.between(lastRotationCheck, now).toDays() >= 1;
            case HOURLY -> Duration.between(lastRotationCheck, now).toHours() >= 1;
            case WEEKLY -> Duration.between(lastRotationCheck, now).toDays() >= 7;
            case SIZE_BASED -> currentFileSize >= maxFileSize;
            default -> false;
        };

        if (needRotation) {
            rotateLogFile();
            lastRotationCheck = now;
        }
    }

    private void rotateLogFile() {
        createNewLogFile();
        cleanupOldFiles();
    }

    private void cleanupOldFiles() {
        try {
            File logDir = new File(logDirectory);
            if (!logDir.exists() || !logDir.isDirectory()) {
                System.err.println("日志目录不存在: " + logDirectory);
                return;
            }

            String basePattern = logFileNamePattern.replace("{timestamp}", "");
            String regexSafePrefix = Pattern.quote(basePattern);

            File[] logFiles = logDir.listFiles((dir, name) ->
                    name.matches("^" + regexSafePrefix + ".+\\.log$")
            );

            if (logFiles != null && logFiles.length > maxFiles) {
                Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

                for (int i = 0; i < logFiles.length - maxFiles; i++) {
                    Files.deleteIfExists(logFiles[i].toPath());
                }
            }
        } catch (SecurityException e) {
            System.err.println("安全异常: 无法访问日志目录 - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("清理日志文件失败: " + e.getMessage());
        }
    }

    private void writeToLog(LogEntry entry) {
        writerLock.lock();
        try {
            if (logWriter != null) {
                String logLine = formatLogEntry(entry);
                if (logLine != null) {
                    logWriter.println(logLine);
                    currentFileSize += logLine.getBytes(StandardCharsets.UTF_8).length
                            + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

                    if (entry.level().ordinal() >= LEVEL.ERROR.ordinal()) {
                        logWriter.flush();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("日志写入失败: " + e.getMessage());
        } finally {
            writerLock.unlock();
        }
    }

    private String formatLogEntry(LogEntry entry) {
        if (entry.level() == LEVEL.OFF) {
            return null;
        }
        String timestamp = buildTimestamp();
        return String.format(format, timestamp, entry.level(), entry.threadName(), entry.message());
    }

    private static String buildTimestamp() {
        String today = LocalDate.now().toString();
        String currentTime = LocalTime.now().toString();
        int maxLength = 15;
        String truncated = currentTime.substring(0, Math.min(currentTime.length(), maxLength));
        String formatted = String.format("%-" + maxLength + "s", truncated);
        return today + " " + formatted;
    }

    public void changeLogDirectory(String newDirectory) {
        String normalizedDir = newDirectory
                .replace("\\", File.separator)
                .replace("/", File.separator);

        configure(builder -> builder.setLogDirectory(normalizedDir));
    }

    private String joinTexts(List<String> texts) {
        StringBuilder sb = new StringBuilder();
        String indent = " ".repeat(36 + length);
        for (int i = 0; i < texts.size(); i++) {
            if (i > 0) {
                sb.append("\n").append(indent);
            }
            sb.append(texts.get(i));
        }
        return sb.toString();
    }

    private record LogEntry(LEVEL level, String message, String threadName, Throwable throwable) {
    }

    public static class LogConfigBuilder {
        private String logDirectory;
        private String logFileNamePattern;
        private LogFileStrategy fileStrategy;
        private long maxFileSize;
        private int maxFiles;
        private LEVEL logLevel;

        public LogConfigBuilder setLogDirectory(String dir) {
            this.logDirectory = dir;
            return this;
        }

        public LogConfigBuilder setFileNamePattern(String pattern) {
            this.logFileNamePattern = pattern;
            return this;
        }

        public LogConfigBuilder setRotationStrategy(LogFileStrategy strategy) {
            this.fileStrategy = strategy;
            return this;
        }

        public LogConfigBuilder setMaxFileSizeMB(int sizeMB) {
            this.maxFileSize = sizeMB * 1024 * 1024L;
            return this;
        }

        public LogConfigBuilder setMaxFiles(int count) {
            this.maxFiles = count;
            return this;
        }

        public LogConfigBuilder setLogLevel(LEVEL level) {
            this.logLevel = level;
            return this;
        }
    }
}
