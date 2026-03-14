package cn.ncw.logger.log;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ThreadLogger {

    private final String APP_NAME;

    private String LoggerName;
    // 日志级别枚举

    // 单例实例
    private static volatile ThreadLogger INSTANCE;
    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>(5000);
    private volatile boolean isRunning = true;
    private PrintWriter logWriter;
    private final ReentrantLock writerLock = new ReentrantLock();

    // 日志配置
    private volatile String logDirectory = "./logs"; // 默认日志目录
    private volatile String logFileNamePattern;
    private volatile LogFileStrategy fileStrategy = LogFileStrategy.DAILY;
    private volatile long maxFileSize = 50 * 1024 * 1024; // 默认50MB
    private volatile int maxFiles = 10; // 保留10个文件
    private volatile LEVEL currentLogLevel = LEVEL.INFO;

    // 当前日志文件信息
    private Path currentLogFile;
    private volatile long currentFileSize;
    private volatile Instant lastRotationCheck = Instant.now();

    // 日志文件轮换策略
    public enum LogFileStrategy {
        SINGLE_FILE,     // 只使用一个文件
        DAILY,           // 每天轮换
        WEEKLY,          // 每周轮换
        SIZE_BASED,      // 大小达到阈值轮换
        HOURLY           // 每小时轮换
    }

    public ThreadLogger(String app_name) {
        this.APP_NAME = app_name;
        this.LoggerName = app_name;
        this.logFileNamePattern = APP_NAME + "_{timestamp}.log";
        Runtime.getRuntime().addShutdownHook(new Thread(this::safeShutdown));
        initializeLogger();
        startLoggerThread();
    }

    public static ThreadLogger getInstance() {
        if (INSTANCE == null) {
            synchronized (ThreadLogger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ThreadLogger(Thread.currentThread().getName());
                }
            }
        }
        return INSTANCE;
    }

    // 配置日志设置（线程安全）
    public synchronized void configure(Consumer<LogConfigBuilder> configurator) {
        LogConfigBuilder builder = new LogConfigBuilder();
        configurator.accept(builder);
        applyConfiguration(builder);

        // 如果已经初始化，需要重新打开日志文件
        if (logWriter != null) {
            rotateLogFile();
        }
    }

    // 应用新配置
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

    // 初始化日志记录器
    private void initializeLogger() {
        try {
            // 确保日志目录存在
            Files.createDirectories(Paths.get(logDirectory));

            // 创建初始日志文件
            createNewLogFile();
        } catch (IOException e) {
            System.err.println("日志初始化失败: " + e.getMessage());
        }
    }

    // 创建新的日志文件
    private void createNewLogFile() {
        writerLock.lock();
        try {
            // 关闭当前日志文件（如果存在）
            if (logWriter != null) {
                try {
                    logWriter.flush();
                    logWriter.close();
                } catch (Exception e) {
                    System.err.println("关闭日志文件失败: " + e.getMessage());
                }
            }

            // 确保日志目录存在
            createLogDirectory();

            // 生成有效的文件名
            String fileName = resolveValidFileName();
            Path filePath = Paths.get(logDirectory, fileName);

            // 创建新文件
            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                } catch (FileAlreadyExistsException e) {
                    // 文件已存在是正常情况
                }
            }

            currentLogFile = filePath;
            currentFileSize = Files.size(filePath); // 获取现有文件大小

            // 创建新的日志写入器
            logWriter = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(filePath.toFile(), true)
                    ),
                    true // 自动刷新
            );

        } catch (Exception e) {
            System.err.println("创建日志文件失败: " + e.getMessage());
            // 创建备用日志文件
            createFallbackLogFile(e);
        } finally {
            writerLock.unlock();
        }
    }
    // 确保日志目录存在（修复问题核心）
    private void createLogDirectory() throws IOException {
        Path logDirPath = Paths.get(logDirectory);

        // 检查目录是否存在
        if (!Files.exists(logDirPath)) {
            try {
                Files.createDirectories(logDirPath);
            } catch (FileAlreadyExistsException e) {
                // 并发情况下其他线程可能已创建目录
            } catch (Exception e) {
                // 创建失败时尝试在当前目录创建
                if (!Files.exists(logDirPath)) {
                    logDirectory = "./logs_fallback";
                    logDirPath = Paths.get(logDirectory);
                    Files.createDirectories(logDirPath);
                }
            }
        }

        // 确保目录是可写的
        File dir = logDirPath.toFile();
        if (!dir.canWrite()) {
            throw new IOException("目录不可写: " + logDirectory);
        }
    }

    // 生成有效的文件名（避免无效字符）
    private String resolveValidFileName() {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // 清理非法字符
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

        // 限制文件名长度
        return safePattern
                .replace("{timestamp}", timestamp)
                .replace("{date}", LocalDate.now().toString());
    }

    // 文件名错误时创建备用文件
    private void createFallbackLogFile(Exception originalError) {
        try {
            // 在临时目录创建日志文件
            Path tempDir = Files.createTempDirectory("logger_fallback_");
            Path fallbackFile = tempDir.resolve("error_" + System.currentTimeMillis() + ".log");

            FileWriter fw = new FileWriter(fallbackFile.toFile());
            fw.write("日志系统初始化失败:\n");
            fw.write("原始错误: " + originalError + "\n");
            fw.write("当前时间: " + new Date() + "\n");

            // 写入系统属性
            System.getProperties().forEach((k, v) -> {
                try {
                    fw.write(k + " = " + v + "\n");
                } catch (IOException e) {
                    // 忽略
                }
            });

            fw.close();
            System.err.println("创建了错误日志文件: " + fallbackFile);

            // 设置当前日志文件
            logWriter = new PrintWriter(fallbackFile.toFile());
            currentLogFile = fallbackFile;
            currentFileSize = 0;
        } catch (Exception ex) {
            System.err.println("严重错误: 无法创建日志文件 - " + ex.getMessage());
        }
    }

    // 解析文件名
    private String resolveFileName() {
        DateTimeFormatter formatter = switch (fileStrategy) {
            case DAILY -> DateTimeFormatter.ofPattern("yyyyMMdd");
            case WEEKLY ->
                // ISO周号格式
                    DateTimeFormatter.ofPattern("yyyy-'w'ww");
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

    // 安全关闭日志
    public void shutdown() {
        isRunning = false;
        safeShutdown();
    }

    private void safeShutdown() {
        if (logWriter != null) {
            writerLock.lock();
            try {
                logWriter.flush();
                logWriter.close();
                logWriter = null;

                // 处理队列中剩余日志
                while (!logQueue.isEmpty()) {
                    writeToLog(logQueue.poll());
                }
            } finally {
                writerLock.unlock();
            }
        }
    }

    // 记录日志
    public void log(LEVEL level, String message, String ThreadName, Throwable throwable) {
        if (level.ordinal() >= currentLogLevel.ordinal()) {
            try {
                logQueue.put(new LogEntry(
                        level,
                        message,
                        LoggerName + ":" + ThreadName,
                        throwable
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("日志队列已满：" + message);
            }
        }
    }

    // 处理日志消息
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

        // 处理剩余日志
        while (!logQueue.isEmpty()) {
            writeToLog(logQueue.poll());
        }
    }

    // 检查是否需要轮换日志文件
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

    // 轮换日志文件
    private void rotateLogFile() {
        createNewLogFile();
        cleanupOldFiles();
    }

    // 清理旧日志文件
    private void cleanupOldFiles() {
        try {
            // 检查目录是否存在
            File logDir = new File(logDirectory);
            if (!logDir.exists() || !logDir.isDirectory()) {
                System.err.println("日志目录不存在: " + logDirectory);
                return;
            }

            // 动态生成文件名前缀
            String basePattern = logFileNamePattern.replace("{timestamp}", "");
            String patternPrefix = basePattern
                    .replace(".", "-")
                    .replace("[", "-")
                    .replace("]", "-")
                    .replace("(", "-")
                    .replace(")", "-");

            File[] logFiles = logDir.listFiles((dir, name) ->
                    name.matches("^" + patternPrefix + ".+\\.log$")
            );

            if (logFiles != null && logFiles.length > maxFiles) {
                // 按最后修改时间排序（最旧的在前面）
                Arrays.sort(logFiles, (f1, f2) ->
                        Long.compare(f1.lastModified(), f2.lastModified()));

                // 删除最旧的文件
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

    // 写入日志
    private void writeToLog(LogEntry entry) {
        writerLock.lock();
        try {
            if (logWriter != null) {
                String logLine = formatLogEntry(entry);
                logWriter.println(logLine);
                if (logLine != null) {
                    currentFileSize += logLine.getBytes().length + System.lineSeparator().getBytes().length;
                }

                // 错误日志立即刷新
                if (entry.level == LEVEL.ERROR) {
                    logWriter.flush();
                }
            }
        } catch (Exception e) {
            System.err.println("日志写入失败: " + e.getMessage());
        } finally {
            writerLock.unlock();
        }
    }

    // 格式化日志条目
    private String formatLogEntry(LogEntry entry) {

        if (entry.level == LEVEL.OFF) {
            return null;
        }

        String today = LocalDate.now().toString();
        String currentTime = LocalTime.now().toString();
        int maxLength = 15;
        String truncated = currentTime.substring(0, Math.min(currentTime.length(), maxLength));
        String formatted = String.format("%-" + maxLength + "s", truncated);
        String timestamp = today + " " + formatted;

        return String.format("%s [%-5s] %-40s %s",
                timestamp,
                entry.level,
                entry.threadName,
                entry.message);
    }

    // 直接更改日志目录
    public void changeLogDirectory(String newDirectory) {
        // 规范化路径分隔符
        String normalizedDir = newDirectory
                .replace("\\", File.separator)
                .replace("/", File.separator);

        configure(builder -> builder.setLogDirectory(normalizedDir));
    }

    private void printDirectoryStatus(Path path) {
        try {
            System.out.println("目录信息: " + path);
            System.out.println("是否存在: " + Files.exists(path));
            System.out.println("是否为目录: " + Files.isDirectory(path));
            System.out.println("权限: 读=" + Files.isReadable(path)
                    + " 写=" + Files.isWritable(path)
                    + " 执行=" + Files.isExecutable(path));
        } catch (Exception e) {
            System.err.println("获取目录状态失败: " + e.getMessage());
        }
    }

    // 内部日志条目类
        private record LogEntry(LEVEL level, String message, String threadName, Throwable throwable) {
    }

    // 配置构建器（简化配置）
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