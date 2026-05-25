package cn.ncw.logger.log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Logger {

    private final String loggerName;

    private final Map<String, String> formatMap;

    private int length = 24;

    public void setLength(int value) {
        this.length = value;
        this.formatMap.putAll(buildFormatMap(value));
    }

    public int getLength() {
        return this.length;
    }

    private static Map<String, String> buildFormatMap(int len) {
        String lenStr = String.valueOf(len);
        Map<String, String> map = new HashMap<>();
        map.put("TRACE",   "\033[94m%s \033[92m[%-5s] \033[90m%-" + lenStr + "s \033[94m%s\033[0m");
        map.put("INFO",    "\033[94m%s \033[92m[%-5s] \033[90m%-" + lenStr + "s \033[92m%s\033[0m");
        map.put("WARN",    "\033[94m%s \033[33m[%-5s] \033[90m%-" + lenStr + "s \033[33m%s\033[0m");
        map.put("DEBUG",   "\033[94m%s \033[90m[%-5s] \033[90m%-" + lenStr + "s \033[90m%s\033[0m");
        map.put("ERROR",   "\033[94m%s \033[31m[%-5s] \033[90m%-" + lenStr + "s \033[31m%s\033[0m");
        map.put("FATAL",   "\033[94m%s \033[35m[%-5s] \033[90m%-" + lenStr + "s \033[35m%s\033[0m");
        map.put("DEFAULT", "\033[94m%s \033[37m[%-5s] \033[90m%-" + lenStr + "s \033[37m%s\033[0m");
        return map;
    }

    public Logger(String appName) {
        this(appName, 24);
    }

    public Logger(String appName, int length) {
        this.loggerName = appName;
        this.length = length;
        this.formatMap = buildFormatMap(length);
    }

    public void log(LEVEL level, String text, String name) {
        if (level == LEVEL.OFF) return;
        log(level.name(), text, name);
    }

    public void log(LEVEL level, List<String> texts, String name) {
        if (level == LEVEL.OFF) return;
        log(level.name(), texts, name);
    }

    private void log(String level, List<String> texts, String name) {
        String text = joinTexts(texts);
        log(level, text, name);
    }

    private void log(String level, String text, String name) {
        if ("OFF".equals(level)) return;
        String timestamp = buildTimestamp();
        String format = formatMap.getOrDefault(level, formatMap.get("DEFAULT"));
        String output = String.format(format, timestamp, level, loggerName + ":" + name, text);
        System.out.println(output);
    }

    public void trace(String text, String name) { log("TRACE", text, name); }
    public void info(String text, String name)  { log("INFO", text, name); }
    public void warn(String text, String name)   { log("WARN", text, name); }
    public void error(String text, String name)  { log("ERROR", text, name); }
    public void fatal(String text, String name)  { log("FATAL", text, name); }
    public void debug(String text, String name)  { log("DEBUG", text, name); }

    public void off(String text, String name) {}
    public void off(List<String> texts, String name) {}

    public void trace(List<String> texts, String name) { log("TRACE", texts, name); }
    public void info(List<String> texts, String name)  { log("INFO", texts, name); }
    public void warn(List<String> texts, String name)   { log("WARN", texts, name); }
    public void error(List<String> texts, String name)  { log("ERROR", texts, name); }
    public void fatal(List<String> texts, String name)  { log("FATAL", texts, name); }
    public void debug(List<String> texts, String name)  { log("DEBUG", texts, name); }

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

    private static String buildTimestamp() {
        String today = LocalDate.now().toString();
        String currentTime = LocalTime.now().toString();
        int maxLength = 15;
        String truncated = currentTime.substring(0, Math.min(currentTime.length(), maxLength));
        String formatted = String.format("%-" + maxLength + "s", truncated);
        return today + " " + formatted;
    }
}
