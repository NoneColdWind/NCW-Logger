package cn.ncw.logger.log;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LogEvent(
        LEVEL level,
        String loggerName,
        String message,
        String threadName,
        Throwable throwable,
        Instant timestamp,
        String callerClass,
        String callerMethod,
        String callerLine
) {

    public LogEvent(LEVEL level, String loggerName, String message, String threadName, Throwable throwable) {
        this(level, loggerName, message, threadName, throwable, Instant.now(), "", "", "");
    }

    public boolean hasThrowable() {
        return throwable != null;
    }

    public String callerLocation() {
        if (!callerLine().isEmpty()) {
            return callerClass() + "." + callerMethod() + "(" + callerLine() + ")";
        }
        return callerClass() + "." + callerMethod();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LEVEL level;
        private String loggerName = "";
        private String message = "";
        private String threadName = "";
        private Throwable throwable;
        private Instant timestamp = Instant.now();
        private String callerClass = "";
        private String callerMethod = "";
        private String callerLine = "";

        public Builder level(LEVEL level) { this.level = level; return this; }
        public Builder loggerName(String name) { this.loggerName = name; return this; }
        public Builder message(String msg) { this.message = msg; return this; }
        public Builder threadName(String name) { this.threadName = name; return this; }
        public Builder throwable(Throwable t) { this.throwable = t; return this; }
        public Builder timestamp(Instant t) { this.timestamp = t; return this; }
        public Builder callerClass(String cls) { this.callerClass = cls; return this; }
        public Builder callerMethod(String method) { this.callerMethod = method; return this; }
        public Builder callerLine(String line) { this.callerLine = line; return this; }

        public LogEvent build() {
            return new LogEvent(level, loggerName, message, threadName, throwable,
                    timestamp, callerClass, callerMethod, callerLine);
        }
    }
}
