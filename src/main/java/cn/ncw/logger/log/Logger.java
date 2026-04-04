package cn.ncw.logger.log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Logger {

    private final String LoggerName;

    public Map<String, String> formatMap = new HashMap<>();

    private Integer length = 24;

    public void setLength(int value) {
        this.length = value;
        updateFormatMap(value);
    }

    public int getLength() {
        return this.length;
    }

    public void updateFormatMap(Integer value) {
        this.formatMap.put("TRACE", "\033[94m%s \033[92m[%-5s] \033[90m%-{}s \033[94m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("INFO", "\033[94m%s \033[92m[%-5s] \033[90m%-{}s \033[92m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("WARN", "\033[94m%s \033[33m[%-5s] \033[90m%-{}s \033[33m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("DEBUG", "\033[94m%s \033[90m[%-5s] \033[90m%-{}s \033[90m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("ERROR", "\033[94m%s \033[31m[%-5s] \033[90m%-{}s \033[31m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("FATAL", "\033[94m%s \033[35m[%-5s] \033[90m%-{}s \033[35m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("OFF", "\033[94m%s \033[35m[%-5s] \033[90m%-{}s \033[90m%s\033[0m".replace("{}", value.toString()));
        this.formatMap.put("DEFAULT", "\033[94m%s \033[37m[%-5s] \033[90m%-{}s \033[37m%s\033[0m".replace("{}", value.toString()));
    }

    public Logger(String app_name) {
        this.LoggerName = app_name;
        new Logger(app_name, 24);
    }

    public Logger(String app_name, int _length) {
        this.LoggerName = app_name;
        this.length = _length;
        this.formatMap.put("TRACE", "\033[94m%s \033[92m[%-5s] \033[90m%-{}s \033[94m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("INFO", "\033[94m%s \033[92m[%-5s] \033[90m%-{}s \033[92m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("WARN", "\033[94m%s \033[33m[%-5s] \033[90m%-{}s \033[33m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("DEBUG", "\033[94m%s \033[90m[%-5s] \033[90m%{}-s \033[90m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("ERROR", "\033[94m%s \033[31m[%-5s] \033[90m%{}-s \033[31m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("FATAL", "\033[94m%s \033[35m[%-5s] \033[90m%{}-s \033[35m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("OFF", "\033[94m%s \033[35m[%-5s] \033[90m%{}-s \033[90m%s\033[0m".replace("{}", length.toString()));
        this.formatMap.put("DEFAULT", "\033[94m%s \033[37m[%-5s] \033[90m%{}-s \033[37m%s\033[0m".replace("{}", length.toString()));

    }

    public void log(LEVEL level, String text, String name) {
        switch (level) {
            case OFF: off(text, name); // 何意味;
            case DEBUG: debug(text, name); break;
            case TRACE: trace(text, name); break;
            case INFO: info(text, name); break;
            case WARN: warn(text, name); break;
            case ERROR: error(text, name); break;
            case FATAL: fatal(text, name); break;
        }
    }

    public void log(LEVEL level, List<String> texts, String name) {
        switch (level) {
            case OFF: off(texts, name); // 何意味;
            case DEBUG: debug(texts, name); break;
            case TRACE: trace(texts, name); break;
            case INFO: info(texts, name); break;
            case WARN: warn(texts, name); break;
            case ERROR: error(texts, name); break;
            case FATAL: fatal(texts, name); break;
        }
    }

    private void log(String level, List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }

        log(level, text.toString(), name);

    }

    public void trace(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("TRACE");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }

    public void info(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("INFO");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }

    public void warn(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("WARN");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }

    public void error(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("ERROR");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }

    public void fatal(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("FATAL");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }

    public void debug(List<String> texts, String name) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < texts.size(); i++) {
            if (!Objects.equals(i, 0)) {
                text.append("\n").append(" ".repeat(36 + length)).append(texts.get(i));
            } else {
                text.append(texts.get(i));
            }
        }
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("DEBUG");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text.toString());
        System.out.println(formatLogEntry(list_wait));
    }



    private void log(String level, String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast(level);
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        if (Objects.equals(level, "OFF")) return;
        System.out.println(formatLogEntry(list_wait));
    }

    public void trace(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("TRACE");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }

    public void info(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("INFO");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }

    public void warn(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("WARN");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }

    public void error(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("ERROR");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }

    public void fatal(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("FATAL");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }

    public void debug(String text, String name) {
        List<String> list_wait = new ArrayList<>();
        list_wait.addLast("DEBUG");
        list_wait.addLast(LoggerName + ":" + name);
        list_wait.addLast(text);
        System.out.println(formatLogEntry(list_wait));
    }


    /**
     * Do not attempt to log a message at the OFF level.
     * <p>
     * 不要尝试记录一个等级为OFF的日志。
     * <p>
     * 何意味。
     * */
    public void off(String text, String name) {

    }

    public void off(List<String> texts, String name) {

    }

    private String formatLogEntry(List<String> context) {

        String today = LocalDate.now().toString();
        String currentTime = LocalTime.now().toString();
        int maxLength = 15;
        String truncated = currentTime.substring(0, Math.min(currentTime.length(), maxLength));
        String formatted = String.format("%-" + maxLength + "s", truncated);
        String timestamp = today + " " + formatted;

        return switch (context.get(0)) {
            case "TRACE" -> String.format(formatMap.get("TRACE"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "INFO" -> String.format(formatMap.get("INFO"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "WARN" -> String.format(formatMap.get("WARN"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "DEBUG" -> String.format(formatMap.get("DEBUG"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "ERROR" -> String.format(formatMap.get("ERROR"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "FATAL" -> String.format(formatMap.get("FATAL"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "OFF" -> String.format(formatMap.get("OFF"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case null, default -> String.format(formatMap.get("DEFAULT"),
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
        };
    }

}

