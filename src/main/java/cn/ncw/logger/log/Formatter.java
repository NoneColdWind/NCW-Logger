package cn.ncw.logger.log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Formatter {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\}");

    private Formatter() {}

    public static String format(String pattern, Object... args) {
        if (pattern == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return pattern;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER.matcher(pattern);
        int argIndex = 0;

        while (matcher.find()) {
            if (argIndex < args.length) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(stringify(args[argIndex++])));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("{}"));
            }
        }
        matcher.appendTail(result);

        while (argIndex < args.length) {
            result.append(" ").append(stringify(args[argIndex++]));
        }

        return result.toString();
    }

    public static String format(List<String> patternParts, Object... args) {
        if (patternParts == null || patternParts.isEmpty()) {
            return "";
        }
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < patternParts.size(); i++) {
            if (i > 0) combined.append(" ");
            combined.append(patternParts.get(i));
        }
        return format((String) combined.toString(), args);
    }

    private static String stringify(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof Throwable) {
            return arg.toString();
        }
        return arg.toString();
    }

    public static String formatException(Throwable t) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");

        StackTraceElement[] stackTrace = t.getStackTrace();
        int maxDepth = Math.min(stackTrace.length, 10);
        for (int i = 0; i < maxDepth; i++) {
            sb.append("\tat ").append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > 10) {
            sb.append("\t... ").append(stackTrace.length - 10).append(" more\n");
        }

        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            sb.append("Caused by: ");
            sb.append(formatException(cause));
        }

        return sb.toString();
    }

    public static String formatException(Throwable t, int maxDepth) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");

        StackTraceElement[] stackTrace = t.getStackTrace();
        int depth = Math.min(stackTrace.length, maxDepth);
        for (int i = 0; i < depth; i++) {
            sb.append("\tat ").append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > maxDepth) {
            sb.append("\t... ").append(stackTrace.length - maxDepth).append(" more\n");
        }

        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            sb.append("Caused by: ");
            sb.append(formatException(cause, maxDepth));
        }

        return sb.toString();
    }
}
