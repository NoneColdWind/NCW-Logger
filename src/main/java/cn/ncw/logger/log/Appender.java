package cn.ncw.logger.log;

import java.io.Closeable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public interface Appender extends Closeable {

    String getName();
    void append(LogEvent event);
    default void flush() {}
    default void close() {}

    default boolean isActive() { return true; }

    abstract class ConsoleAppender implements Appender {
        private final String name;
        private final Layout layout;
        private final boolean colorEnabled;
        private volatile boolean active = true;

        public ConsoleAppender(String name, Layout layout) {
            this(name, layout, true);
        }

        public ConsoleAppender(String name, Layout layout, boolean colorEnabled) {
            this.name = name;
            this.layout = layout;
            this.colorEnabled = colorEnabled;
        }

        @Override
        public String getName() { return name; }

        @Override
        public void append(LogEvent event) {
            String output = layout.format(event);
            if (output == null || output.isEmpty()) return;

            String colored;
            if (colorEnabled && layout instanceof Layout.PatternLayout pl) {
                colored = applyColor(event.level(), output);
            } else {
                colored = output;
            }
            System.out.println(colored);
        }

        private String applyColor(LEVEL level, String message) {
            return switch (level) {
                case TRACE -> "\033[94m" + message + "\033[0m";
                case DEBUG -> "\033[90m" + message + "\033[0m";
                case INFO  -> "\033[92m" + message + "\033[0m";
                case WARN  -> "\033[33m" + message + "\033[0m";
                case ERROR -> "\033[31m" + message + "\033[0m";
                case FATAL -> "\033[35m" + message + "\033[0m";
                default -> message;
            };
        }

        @Override
        public boolean isActive() { return active; }

        @Override
        public void close() { active = false; }
    }

    abstract class FileAppender implements Appender {
        private final String name;
        private final Layout layout;
        private volatile String logDirectory;
        private volatile String fileNamePattern;
        private volatile long maxFileSize = 50 * 1024 * 1024;
        private volatile int maxFiles = 10;
        private volatile long currentFileSize;
        private volatile Instant lastRotationCheck = Instant.now();
        private volatile RollingStrategy rollingStrategy = RollingStrategy.DAILY;
        private volatile boolean active = true;

        private final BlockingQueue<LogEvent> queue = new LinkedBlockingQueue<>(5000);
        private final java.util.concurrent.locks.ReentrantLock writeLock = new java.util.concurrent.locks.ReentrantLock();
        private java.io.PrintWriter writer;

        private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        private static final AtomicLong writtenBytes = new AtomicLong(0);
        private static final AtomicLong droppedEvents = new AtomicLong(0);

        public FileAppender(String name, Layout layout, String directory, String filePattern) {
            this.name = name;
            this.layout = layout;
            this.logDirectory = directory;
            this.fileNamePattern = filePattern;
            initialize();
            startWriterThread();
        }

        private void initialize() {
            try {
                java.nio.file.Files.createDirectories(java.nio.file.Paths.get(logDirectory));
                openFile();
            } catch (Exception e) {
                System.err.println("FileAppender 初始化失败: " + e.getMessage());
            }
        }

        private void openFile() {
            writeLock.lock();
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }

                String fileName = resolveFileName();
                java.nio.file.Path filePath = java.nio.file.Paths.get(logDirectory, fileName);

                if (!java.nio.file.Files.exists(filePath)) {
                    try {
                        java.nio.file.Files.createFile(filePath);
                    } catch (java.nio.file.FileAlreadyExistsException ignored) {}
                }

                currentFileSize = java.nio.file.Files.size(filePath);
                writer = new java.io.PrintWriter(
                        new java.io.BufferedWriter(
                                new java.io.OutputStreamWriter(
                                        new java.io.FileOutputStream(filePath.toFile(), true),
                                        java.nio.charset.StandardCharsets.UTF_8
                                )
                        ),
                        true
                );
            } catch (Exception e) {
                System.err.println("打开日志文件失败: " + e.getMessage());
            } finally {
                writeLock.unlock();
            }
        }

        private String resolveFileName() {
            String ts = java.time.LocalDateTime.now().format(FILE_DATE);
            return fileNamePattern
                    .replace("{timestamp}", ts)
                    .replace("{date}", java.time.LocalDate.now().toString());
        }

        private void startWriterThread() {
            Thread t = new Thread(() -> {
                while (active || !queue.isEmpty()) {
                    try {
                        LogEvent event = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (event != null) {
                            checkRotation();
                            writeEvent(event);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                flush();
            }, "FileAppender-Writer");
            t.setDaemon(true);
            t.start();
        }

        private void checkRotation() {
            if (rollingStrategy == RollingStrategy.NONE) return;

            long now = System.currentTimeMillis();
            boolean needRotate = switch (rollingStrategy) {
                case DAILY -> {
                    java.time.Duration d = java.time.Duration.between(lastRotationCheck, java.time.Instant.now());
                    yield d.toDays() >= 1;
                }
                case HOURLY -> {
                    java.time.Duration d = java.time.Duration.between(lastRotationCheck, java.time.Instant.now());
                    yield d.toHours() >= 1;
                }
                case SIZE -> currentFileSize >= maxFileSize;
                default -> false;
            };

            if (needRotate) {
                rotate();
                lastRotationCheck = java.time.Instant.now();
            }
        }

        private void rotate() {
            writeLock.lock();
            try {
                openFile();
                cleanupOldFiles();
            } finally {
                writeLock.unlock();
            }
        }

        private void cleanupOldFiles() {
            try {
                java.io.File dir = new java.io.File(logDirectory);
                if (!dir.exists() || !dir.isDirectory()) return;

                String pattern = java.util.regex.Pattern.quote(
                        fileNamePattern.replace("{timestamp}", "").replace("{date}", ""));

                java.io.File[] files = dir.listFiles((d, n) ->
                        n.matches("^" + pattern + ".+\\.log$")
                );

                if (files != null && files.length > maxFiles) {
                    java.util.Arrays.sort(files, java.util.Comparator.comparingLong(java.io.File::lastModified));
                    for (int i = 0; i < files.length - maxFiles; i++) {
                        java.nio.file.Files.deleteIfExists(files[i].toPath());
                    }
                }
            } catch (Exception e) {
                System.err.println("清理日志文件失败: " + e.getMessage());
            }
        }

        private void writeEvent(LogEvent event) {
            writeLock.lock();
            try {
                if (writer == null) return;

                String line = layout.format(event);
                if (line == null || line.isEmpty()) return;

                writer.println(line);
                currentFileSize += line.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                        + System.lineSeparator().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

                if (event.level().ordinal() >= LEVEL.ERROR.ordinal()) {
                    writer.flush();
                }
            } catch (Exception e) {
                System.err.println("写入日志失败: " + e.getMessage());
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public String getName() { return name; }

        @Override
        public void append(LogEvent event) {
            if (!active) return;
            if (event.level() == LEVEL.OFF) return;
            boolean offered = queue.offer(event);
            if (!offered) {
                droppedEvents.incrementAndGet();
                System.err.println("日志队列已满，事件被丢弃: " + event.message());
            }
            writtenBytes.addAndGet(event.message().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        }

        @Override
        public void flush() {
            writeLock.lock();
            try {
                if (writer != null) writer.flush();
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public void close() {
            active = false;
            writeLock.lock();
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                    writer = null;
                }
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public boolean isActive() { return active; }

        public int queueSize() { return queue.size(); }
        public long totalWrittenBytes() { return writtenBytes.get(); }
        public long droppedEventsCount() { return droppedEvents.get(); }

        public void setMaxFileSize(long bytes) { this.maxFileSize = bytes; }
        public void setMaxFiles(int count) { this.maxFiles = count; }
        public void setRollingStrategy(RollingStrategy strategy) { this.rollingStrategy = strategy; }
        public void setDirectory(String dir) {
            this.logDirectory = dir;
            rotate();
        }

        public enum RollingStrategy {
            NONE, DAILY, HOURLY, SIZE
        }
    }

    static ConsoleAppender console(String name) {
        return console(name, Layout.defaultLayout());
    }

    static ConsoleAppender console(String name, Layout layout) {
        return new ConsoleAppender(name, layout) {};
    }

    static FileAppender file(String name, String directory, String filePattern) {
        return file(name, Layout.defaultLayout(), directory, filePattern);
    }

    static FileAppender file(String name, Layout layout, String directory, String filePattern) {
        return new FileAppender(name, layout, directory, filePattern) {};
    }
}
