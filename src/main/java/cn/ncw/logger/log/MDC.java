package cn.ncw.logger.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class MDC {

    private static final InheritableThreadLocal<ContextMap> CONTEXT = new InheritableThreadLocal<>();

    private MDC() {}

    public static void put(String key, String value) {
        getContextMap().put(key, value);
    }

    public static String get(String key) {
        ContextMap map = CONTEXT.get();
        return map == null ? null : map.get(key);
    }

    public static void remove(String key) {
        ContextMap map = CONTEXT.get();
        if (map != null) {
            map.remove(key);
        }
    }

    public static void clear() {
        ContextMap map = CONTEXT.get();
        if (map != null) {
            map.clear();
        }
    }

    public static Map<String, String> getCopyOfContextMap() {
        ContextMap map = CONTEXT.get();
        if (map == null) {
            return Map.of();
        }
        return Map.copyOf(map);
    }

    public static String getAndRemove(String key) {
        ContextMap map = CONTEXT.get();
        if (map == null) {
            return null;
        }
        return map.remove(key);
    }

    public static String computeIfAbsent(String key, Supplier<String> supplier) {
        ContextMap map = getContextMap();
        return map.computeIfAbsent(key, k -> supplier.get());
    }

    public static <T> T wrap(T value, Runnable action) {
        try {
            return value;
        } finally {
            action.run();
        }
    }

    public static String formatContext() {
        Map<String, String> map = getCopyOfContextMap();
        if (map.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        map.forEach((key, val) -> {
            if (sb.length() > 0) sb.append(" ");
            sb.append(key).append("=").append(val);
        });
        return sb.toString();
    }

    public static String formatContext(String separator) {
        Map<String, String> map = getCopyOfContextMap();
        if (map.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        map.forEach((key, val) -> {
            if (sb.length() > 0) sb.append(separator);
            sb.append(key).append("=").append(val);
        });
        return sb.toString();
    }

    private static ContextMap getContextMap() {
        ContextMap map = CONTEXT.get();
        if (map == null) {
            map = new ContextMap();
            CONTEXT.set(map);
        }
        return map;
    }

    private static class ContextMap extends ConcurrentHashMap<String, String> {}
}
