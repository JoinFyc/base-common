package com.mengxiang.base.common.log;

import com.mengxiang.base.common.log.log.*;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 日志组件工具类
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/23 11:08 PM
 */
public final class Logger {

    private static final String FQCN = Logger.class.getName();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Const.Log.LOG_LOGGER_NAME);
    private static final ThreadLocal<Map<String, Object>> TARGET_CACHE = ThreadLocal.withInitial(HashMap::new);
    private static String appId;

    static {
        appId = System.getProperty("app.id");
        if (appId == null) {
            appId = System.getProperty("spring.application.name");
        }
        if (appId == null) {
            appId = "UNKNOWN";
        }
    }

    public static void debug(Supplier<String> msg) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        buildAndLog(LogLevel.DEBUG, msg.get());
    }

    public static void debug(String msg) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        buildAndLog(LogLevel.DEBUG, msg);
    }

    public static void debug(String pattern, Supplier<String> msgSupplier) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, msgSupplier.get());
        buildAndLog(LogLevel.DEBUG, formatMessage);
    }

    public static void debug(String pattern, Throwable t) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, t);
        buildAndLog(LogLevel.DEBUG, formatMessage);
    }

    public static void debug(String pattern, Object... param) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, param);
        buildAndLog(LogLevel.DEBUG, formatMessage);
    }

    public static void info(Supplier<String> msg) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        buildAndLog(LogLevel.INFO, msg.get());
    }

    public static void info(String msg) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        buildAndLog(LogLevel.INFO, msg);
    }

    public static void info(String pattern, Supplier<String> msgSupplier) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, msgSupplier.get());
        buildAndLog(LogLevel.INFO, formatMessage);
    }

    public static void info(String pattern, Throwable t) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, t);
        buildAndLog(LogLevel.INFO, formatMessage);
    }

    public static void info(String pattern, Object... param) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, param);
        buildAndLog(LogLevel.INFO, formatMessage);
    }

    public static void warn(Supplier<String> msg) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        buildAndLog(LogLevel.WARN, msg.get());
    }

    public static void warn(String msg) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        buildAndLog(LogLevel.WARN, msg);
    }

    public static void warn(String pattern, Supplier<String> msgSupplier) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, msgSupplier.get());
        buildAndLog(LogLevel.WARN, formatMessage);
    }

    public static void warn(String pattern, Throwable t) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, t);
        buildAndLog(LogLevel.WARN, formatMessage);
    }

    public static void warn(String pattern, Object... param) {
        if (!LOGGER.isWarnEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, param);
        buildAndLog(LogLevel.WARN, formatMessage);
    }

    public static void error(Supplier<String> msg) {
        if (!LOGGER.isErrorEnabled()) {
            return;
        }
        buildAndLog(LogLevel.ERROR, msg.get());
    }

    public static void error(String msg) {
        if (!LOGGER.isErrorEnabled()) {
            return;
        }
        buildAndLog(LogLevel.ERROR, msg);
    }

    public static void error(String pattern, Supplier<String> msgSupplier) {
        if (!LOGGER.isErrorEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, msgSupplier.get());
        buildAndLog(LogLevel.ERROR, formatMessage);
    }

    public static void error(String pattern, Throwable t) {
        if (!LOGGER.isErrorEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, t);
        buildAndLog(LogLevel.ERROR, formatMessage);
    }

    public static void error(String pattern, Object... param) {
        if (!LOGGER.isErrorEnabled()) {
            return;
        }
        String formatMessage = LogUtils.formatMessage(pattern, param);
        buildAndLog(LogLevel.ERROR, formatMessage);
    }

    /**
     * 构造log内容，输出日志
     *
     * @param level
     * @param msg
     */
    private static void buildAndLog(LogLevel level, String msg) {
        StackTraceElement stackTrace = LogUtils.getStackTrace(FQCN);
        LogBuilder builder = Log.builder();
        builder.level(level).msg(msg).aid(appId).fqcn(FQCN)
                .logger(Const.Log.LOG_LOGGER_NAME).stackTrace(stackTrace);
        builder.tag(TARGET_CACHE.get());
        Log log = builder.build();
        writeLog(level, msg, log);
    }

    /**
     * 打印Log日志
     *
     * @param level
     * @param msg
     */
    private static void writeLog(LogLevel level, String msg, Log log) {
        switch (level) {
            case DEBUG:
                LOGGER.debug(msg, log);
                break;
            case INFO:
            default:
                LOGGER.info(msg, log);
                break;
            case WARN:
                LOGGER.warn(msg, log);
                break;
            case ERROR:
                LOGGER.error(msg, log);
                break;
        }
    }

    public static void addTag(String key, Object value) {
        TARGET_CACHE.get().put(key, value);
    }

    public static void removeTag(String key) {
        TARGET_CACHE.get().remove(key);
    }

    public static void cleanTags() {
        TARGET_CACHE.get().clear();
    }
}
