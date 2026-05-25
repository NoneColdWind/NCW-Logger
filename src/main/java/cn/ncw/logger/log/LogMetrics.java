package cn.ncw.logger.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class LogMetrics {

    private static final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private static final AtomicLong totalLogs = new AtomicLong(0);
    private static final AtomicLong totalErrors = new AtomicLong(0);
    private static final AtomicLong totalDropped = new AtomicLong(0);
    private static final AtomicLong totalBytes = new AtomicLong(0);
    private static volatile long startTime = System.currentTimeMillis();

    private LogMetrics() {}

    public static void increment(LEVEL level) {
        String key = level.name();
        counters.computeIfAbsent(key, k -> new Counter()).increment();
        totalLogs.incrementAndGet();
        if (level.ordinal() >= LEVEL.ERROR.ordinal()) {
            totalErrors.incrementAndGet();
        }
    }

    public static void addDropped(int count) {
        totalDropped.addAndGet(count);
    }

    public static void addBytes(long bytes) {
        totalBytes.addAndGet(bytes);
    }

    public static long getTotalLogs() {
        return totalLogs.get();
    }

    public static long getTotalErrors() {
        return totalErrors.get();
    }

    public static long getTotalDropped() {
        return totalDropped.get();
    }

    public static long getTotalBytes() {
        return totalBytes.get();
    }

    public static long getCount(LEVEL level) {
        Counter c = counters.get(level.name());
        return c == null ? 0 : c.get();
    }

    public static long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    public static double getLogsPerSecond() {
        long uptime = getUptimeMs();
        if (uptime == 0) return 0;
        return (totalLogs.get() * 1000.0) / uptime;
    }

    public static void reset() {
        counters.clear();
        totalLogs.set(0);
        totalErrors.set(0);
        totalDropped.set(0);
        totalBytes.set(0);
        startTime = System.currentTimeMillis();
    }

    public static String getSummary() {
        return String.format(
                "NCW-Logger Metrics:\n" +
                "  Uptime: %s\n" +
                "  Total Logs: %d (%.2f/s)\n" +
                "  Errors: %d\n" +
                "  Dropped: %d\n" +
                "  Total Bytes: %s\n" +
                "  By Level:\n" +
                "    TRACE: %d\n" +
                "    DEBUG: %d\n" +
                "    INFO:  %d\n" +
                "    WARN:  %d\n" +
                "    ERROR: %d\n" +
                "    FATAL: %d",
                formatUptime(getUptimeMs()),
                totalLogs.get(), getLogsPerSecond(),
                totalErrors.get(),
                totalDropped.get(),
                formatBytes(totalBytes.get()),
                getCount(LEVEL.TRACE),
                getCount(LEVEL.DEBUG),
                getCount(LEVEL.INFO),
                getCount(LEVEL.WARN),
                getCount(LEVEL.ERROR),
                getCount(LEVEL.FATAL)
        );
    }

    private static String formatUptime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private static class Counter {
        private final AtomicLong count = new AtomicLong(0);

        void increment() {
            count.incrementAndGet();
        }

        long get() {
            return count.get();
        }
    }
}
