package cn.ncw.logger.log;


public enum LEVEL {
    DEBUG, TRACE, INFO, WARN, ERROR, FATAL, OFF;


    public static LEVEL getLevel( String level) {
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
