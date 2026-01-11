// utils/LogUtils.java
package ll.luolin.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 */
public class LogUtils {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * 记录日志
     */
    public static void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        
        System.out.printf("[%s] [%s] [%s] %s%n", 
            timestamp, threadName, level, message);
    }
    
    /**
     * 记录调试信息
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    
    /**
     * 记录信息
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * 记录警告
     */
    public static void warn(String message) {
        log(LogLevel.WARN, message);
    }
    
    /**
     * 记录错误
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * 记录错误（带异常）
     */
    public static void error(String message, Throwable throwable) {
        error(message + ": " + throwable.getMessage());
        throwable.printStackTrace();
    }
}
