package cn.ncw.logger.log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Layout {

    public abstract String format(LogEvent event);

    public static class PatternLayout extends Layout {

        private final String pattern;
        private final PatternToken[] tokens;
        private final boolean colorEnabled;

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        private static final Pattern TOKEN_PATTERN = Pattern.compile(
                "%(-?\\d*)?(\\.\\d+)?([cdExXmprRnlt%]|msg|message|level|logger|thread|date|time|mdc|mdc\\{([^}]+)\\}|caller|class|method|line)"
        );

        public PatternLayout(String pattern) {
            this(pattern, true);
        }

        public PatternLayout(String pattern, boolean colorEnabled) {
            this.pattern = pattern;
            this.colorEnabled = colorEnabled;
            this.tokens = parseTokens(pattern);
        }

        private PatternToken[] parseTokens(String pattern) {
            Matcher matcher = TOKEN_PATTERN.matcher(pattern);
            java.util.ArrayList<PatternToken> list = new java.util.ArrayList<>();

            int lastEnd = 0;
            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    list.add(new LiteralToken(pattern.substring(lastEnd, matcher.start())));
                }

                String widthStr = matcher.group(1);
                int width = 0;
                if (widthStr != null && !widthStr.isEmpty()) {
                    try {
                        width = Integer.parseInt(widthStr.replace("-", ""));
                    } catch (NumberFormatException ignored) {}
                }
                boolean leftAlign = widthStr != null && widthStr.startsWith("-");

                String precision = matcher.group(2);

                String type = matcher.group(3);
                String mdcKey = matcher.group(5);

                list.add(new FormatToken(type, width, leftAlign, precision, mdcKey));
                lastEnd = matcher.end();
            }

            if (lastEnd < pattern.length()) {
                list.add(new LiteralToken(pattern.substring(lastEnd)));
            }

            return list.toArray(new PatternToken[0]);
        }

        @Override
        public String format(LogEvent event) {
            StringBuilder sb = new StringBuilder();

            for (PatternToken token : tokens) {
                sb.append(token.render(event, this));
            }

            return sb.toString();
        }

        public String getPattern() {
            return pattern;
        }

        boolean isColorEnabled() {
            return colorEnabled;
        }

        private static abstract class PatternToken {
            abstract String render(LogEvent event, PatternLayout layout);
        }

        private static class LiteralToken extends PatternToken {
            private final String literal;

            LiteralToken(String literal) {
                this.literal = literal;
            }

            @Override
            String render(LogEvent event, PatternLayout layout) {
                return literal;
            }
        }

        private static class FormatToken extends PatternToken {
            private final String type;
            private final int width;
            private final boolean leftAlign;
            private final String precision;
            private final String mdcKey;

            FormatToken(String type, int width, boolean leftAlign, String precision, String mdcKey) {
                this.type = type;
                this.width = width;
                this.leftAlign = leftAlign;
                this.precision = precision;
                this.mdcKey = mdcKey;
            }

            @Override
            String render(LogEvent event, PatternLayout layout) {
                String value = resolveValue(event);
                if (value == null) value = "";

                if (precision != null && value.length() > 0) {
                    try {
                        int maxLen = Integer.parseInt(precision.substring(1));
                        if (value.length() > maxLen) {
                            value = value.substring(value.length() - maxLen);
                        }
                    } catch (NumberFormatException ignored) {}
                }

                if (width > 0) {
                    if (leftAlign) {
                        value = String.format("%-" + width + "s", value);
                    } else {
                        value = String.format("%" + width + "s", value);
                    }
                }

                return value;
            }

            private String resolveValue(LogEvent event) {
                return switch (type) {
                    case "d", "date" -> LocalDate.now().toString();
                    case "t", "time" -> LocalTime.now().format(TIME_FORMAT);
                    case "c", "logger" -> event.loggerName();
                    case "C", "class" -> event.callerClass();
                    case "M", "method" -> event.callerMethod();
                    case "l", "line" -> event.callerLine();
                    case "L", "caller" -> event.callerLocation();
                    case "m", "msg", "message" -> event.message();
                    case "n" -> System.lineSeparator();
                    case "r", "R", "thread" -> event.threadName();
                    case "p", "level" -> event.level().name();
                    case "N", "mdc" -> MDC.formatContext();
                    default -> {
                        if (type.startsWith("mdc{") && mdcKey != null) {
                            String v = MDC.get(mdcKey);
                            yield v != null ? v : "";
                        }
                        if ("%".equals(type)) yield "%";
                        yield "";
                    }
                };
            }
        }

        public static class Builder {
            private String pattern = "%d %p [%t] %c - %m%n";
            private boolean colorEnabled = true;

            public Builder pattern(String pattern) {
                this.pattern = pattern;
                return this;
            }

            public Builder colorEnabled(boolean enabled) {
                this.colorEnabled = enabled;
                return this;
            }

            public PatternLayout build() {
                return new PatternLayout(pattern, colorEnabled);
            }
        }
    }

    public static class JsonLayout extends Layout {

        private final boolean prettyPrint;
        private final boolean includeMdc;
        private final boolean includeCallerInfo;

        public JsonLayout() {
            this(false);
        }

        public JsonLayout(boolean prettyPrint) {
            this(prettyPrint, true, false);
        }

        public JsonLayout(boolean prettyPrint, boolean includeMdc, boolean includeCallerInfo) {
            this.prettyPrint = prettyPrint;
            this.includeMdc = includeMdc;
            this.includeCallerInfo = includeCallerInfo;
        }

        @Override
        public String format(LogEvent event) {
            StringBuilder sb = new StringBuilder();
            if (prettyPrint) {
                sb.append("{\n");
                appendField(sb, "timestamp", LocalDate.now() + "T" + LocalTime.now().format(TIME_FORMAT), true);
                appendField(sb, "level", event.level().name(), false);
                appendField(sb, "logger", event.loggerName(), false);
                appendField(sb, "thread", event.threadName(), false);
                appendField(sb, "message", event.message(), false);
                appendField(sb, "throwable", event.hasThrowable() ? stackTraceToString(event.throwable()) : null, false);
                if (includeMdc) {
                    Map<String, String> mdc = MDC.getCopyOfContextMap();
                    if (!mdc.isEmpty()) {
                        sb.append("  \"mdc\": {");
                        int i = 0;
                        for (Map.Entry<String, String> entry : mdc.entrySet()) {
                            if (i++ > 0) sb.append(",");
                            sb.append("\"").append(escape(entry.getKey())).append("\": \"")
                                    .append(escape(entry.getValue())).append("\"");
                        }
                        sb.append("},\n");
                    }
                }
                if (includeCallerInfo) {
                    appendField(sb, "callerClass", event.callerClass(), false);
                    appendField(sb, "callerMethod", event.callerMethod(), false);
                    appendField(sb, "callerLine", event.callerLine(), false);
                }
                sb.setLength(sb.length() - 2);
                sb.append("\n}");
            } else {
                sb.append("{");
                appendField(sb, "timestamp", LocalDate.now() + "T" + LocalTime.now().format(TIME_FORMAT), true);
                appendField(sb, "level", event.level().name(), false);
                appendField(sb, "logger", event.loggerName(), false);
                appendField(sb, "thread", event.threadName(), false);
                appendField(sb, "message", event.message(), false);
                appendField(sb, "throwable", event.hasThrowable() ? stackTraceToString(event.throwable()) : null, false);
                if (includeMdc) {
                    Map<String, String> mdc = MDC.getCopyOfContextMap();
                    if (!mdc.isEmpty()) {
                        sb.append("\"mdc\":{");
                        int i = 0;
                        for (Map.Entry<String, String> entry : mdc.entrySet()) {
                            if (i++ > 0) sb.append(",");
                            sb.append("\"").append(escape(entry.getKey())).append("\":\"")
                                    .append(escape(entry.getValue())).append("\"");
                        }
                        sb.append("},");
                    }
                }
                if (includeCallerInfo) {
                    appendField(sb, "callerClass", event.callerClass(), false);
                    appendField(sb, "callerMethod", event.callerMethod(), false);
                    appendField(sb, "callerLine", event.callerLine(), false);
                }
                sb.setLength(sb.length() - 1);
                sb.append("}");
            }
            return sb.toString();
        }

        private void appendField(StringBuilder sb, String key, String value, boolean trailingComma) {
            if (value == null) {
                if (trailingComma) return;
            }
            sb.append("\"").append(escape(key)).append("\":");
            if (value != null) {
                sb.append("\"").append(escape(value)).append("\"");
            } else {
                sb.append("null");
            }
            sb.append(",\n");
        }

        private void appendField(StringBuilder sb, String key, String value, boolean trailingComma, boolean quote) {
            if (value == null) {
                if (trailingComma) return;
            }
            sb.append("\"").append(escape(key)).append("\":");
            if (quote) {
                if (value != null) {
                    sb.append("\"").append(escape(value)).append("\"");
                } else {
                    sb.append("null");
                }
            } else {
                if (value != null) {
                    sb.append(escape(value));
                } else {
                    sb.append("null");
                }
            }
            if (trailingComma) sb.append(",");
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }

        private String stackTraceToString(Throwable t) {
            if (t == null) return "";
            StringBuilder sb = new StringBuilder();
            sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
            for (StackTraceElement e : t.getStackTrace()) {
                sb.append("\n\tat ").append(e.toString());
            }
            return sb.toString();
        }

        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        public static class Builder {
            private boolean prettyPrint = false;
            private boolean includeMdc = true;
            private boolean includeCallerInfo = false;

            public Builder prettyPrint(boolean prettyPrint) {
                this.prettyPrint = prettyPrint;
                return this;
            }

            public Builder includeMdc(boolean includeMdc) {
                this.includeMdc = includeMdc;
                return this;
            }

            public Builder includeCallerInfo(boolean includeCallerInfo) {
                this.includeCallerInfo = includeCallerInfo;
                return this;
            }

            public JsonLayout build() {
                return new JsonLayout(prettyPrint, includeMdc, includeCallerInfo);
            }
        }
    }

    public static class SimpleLayout extends Layout {
        @Override
        public String format(LogEvent event) {
            String ts = LocalDate.now() + " " + LocalTime.now().toString().substring(0, 12);
            return String.format("%s [%s] [%s] %s",
                    ts,
                    event.level().name(),
                    event.threadName(),
                    event.message());
        }
    }

    public static Layout defaultLayout() {
        return new PatternLayout("%d %p [%t] %c - %m%n");
    }
}
