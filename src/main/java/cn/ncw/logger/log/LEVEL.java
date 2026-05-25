package cn.ncw.logger.log;


public enum LEVEL {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF;


    public static LEVEL getLevel(String level) {
        return switch (level) {
            case "TRACE" -> TRACE;
            case "DEBUG" -> DEBUG;
            case "INFO" -> INFO;
            case "WARN" -> WARN;
            case "ERROR" -> ERROR;
            case "FATAL" -> FATAL;
            default -> OFF;
        };
    }
}
