package com.mengxiang.base.common.log;

/**
 * @author ice
 * @version 1.0
 * @date 2019/7/10 8:55 PM
 */
public interface Const {

    /**
     * tag数量限制
     */
    int MAX_TAG_SIZE = 5;

    /**
     * 默认Metrics数量
     */
    int DEFAULT_METRICS_SIZE = 5;

    /**
     * trace类型
     */
    String TRACE_TYPE_TAG = "type";

    /**
     * 持续时间
     */
    String TRACE_DURATION_TAG = "duration";

    /**
     * 进入应用的trace
     */
    String TRACE_TYPE_APP_IN = "app.in";

    /**
     * 离开应用的trace
     */
    String TRACE_TYPE_APP_OUT = "app.out";

    /**
     * 进入方法的trace
     */
    String TRACE_TYPE_METHOD_IN = "method.in";

    /**
     * 离开方法的trace
     */
    String TRACE_TYPE_METHOD_OUT = "method.out";

    /**
     * 日志相关常量
     */
    interface Log {

        /**
         * 文件滚动间隔
         */
        String ROLLING_INTERVAL = "1";

        /**
         * 文件滚动时是否按照时间取整命名
         */
        String ROLLING_MODULATE = "true";

        /**
         * 最大文件窗口大小
         */
        String MAX_WINDOW_SIZE = "7";

        /**
         * 最大文件大小 1G
         */
        long MAX_FILE_SIZE = 1024 * 1024 * 1024;

        /**
         * 最大文件窗口大小
         */
        String TOTAL_FILE_SIZE = 10 * MAX_FILE_SIZE + "";

        /**
         * 用户根目录
         */
        String USER_HOME = System.getProperty("user.home") == null
                ? "/home/ops" : System.getProperty("user.home");

        /**
         * INFO日志文件默认文件名
         */
        String INFO_FILE_NAME = USER_HOME + "/logs/log.log";

        /**
         * INFO日志文件滚动文件名
         */
        String INFO_FILE_NAME_PATTERN = USER_HOME + "/logs/log-%d{yyyy-MM-dd}-%i.log";

        /**
         * ERROR日志文件默认文件名
         */
        String ERROR_FILE_NAME = USER_HOME + "/logs/error.log";

        /**
         * ERROR日志文件滚动文件名
         */
        String ERROR_FILE_NAME_PATTERN = USER_HOME + "/logs/error-%d{yyyy-MM-dd}.log";

        /**
         * TRACE文件默认文件名
         */
        String TRACE_FILE_NAME = USER_HOME + "/logs/trace.log";

        /**
         * TRACE日志文件滚动文件名
         */
        String TRACE_FILE_NAME_PATTERN = USER_HOME + "/logs/trace-%d{yyyy-MM-dd}-%i.log";

        /**
         * METRICS文件默认文件名
         */
        String METRICS_FILE_NAME = USER_HOME + "/logs/metrics.log";

        /**
         * METRICS日志文件滚动文件名
         */
        String METRICS_FILE_NAME_PATTERN = USER_HOME + "/logs/metrics-%d{yyyy-MM-dd}-%i.log";

        /**
         * 控制台日志Apppender名称
         */
        String CONSOLE_APPENDER_NAME = "CONSOLE_APPENDER";

        /**
         * 控制台日志Apppender名称
         */
        String INFO_LOG_APPENDER_NAME = "INFO_APPENDER";

        /**
         * 控制台日志Apppender名称
         */
        String ERROR_LOG_APPENDER_NAME = "ERROR_APPENDER";

        /**
         * 控制台日志Apppender名称
         */
        String TRACE_APPENDER_NAME = "TRACE_APPENDER";

        /**
         * 控制台日志Apppender名称
         */
        String METRICS_APPENDER_NAME = "METRICS_APPENDER";

        /**
         * Log4j2控制台日志格式
         */
        String LOG4J2_CONSOLE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS}" +
                "|%5p|${sys:PID}|[%15.15t][%tid] %-40.40c{1.}|%m|%n";

        /**
         * Logback控制台日志格式
         */
        String LOGBACK_CONSOLE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS}" +
                "|%X{PID}|%5p|[%15.15t]|[%tid]| %-40.40logger{39}%X{custom}|%m%n";

        /**
         * 文件日志格式
         */
        String FILE_PATTERN = "%m%n";

        /**
         * Log Logger Name
         */
        String LOG_LOGGER_NAME = "log.Logger";

        /**
         * Trace Logger Name
         */
        String TRACE_LOGGER_NAME = "trace.Logger";

        /**
         * Metrics Logger Name
         */
        String METRICS_LOGGER_NAME = "metrics.Logger";

        /**
         * 日志是否包含调用位置
         */
        String INCLUDE_LOCATION = "false";
    }
}
