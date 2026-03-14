package cn.ncw.logger.log;

import org.jetbrains.annotations.NotNull;

public enum LEVEL {
    DEBUG, TRACE, INFO, WARN, ERROR, FATAL, OFF;


    @NotNull
    public static LEVEL getLevel(@NotNull String level) {
        return switch (level) {
            case "DEBUG" -> DEBUG;
            case "TRACE" -> TRACE;
            case "INFO" -> INFO;
            case "WARN" -> WARN;
            case "ERROR" -> ERROR;
            case "FATAL" -> FATAL;
            default -> OFF;
        };
    }
}
