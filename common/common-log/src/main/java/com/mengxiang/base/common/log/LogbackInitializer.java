package com.mengxiang.base.common.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.Encoder;
import com.mengxiang.base.common.log.layout.LogbackConsoleLayoutEncoder;
import com.mengxiang.base.common.log.log.LogLevel;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Logback动态Logger的初始化
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/26 5:34 PM
 */
public class LogbackInitializer implements ILoggerInitializer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogbackInitializer.class);
    private static final Map<String, Appender> APPENDER_CACHE = new HashMap<>(5);
    private static final Set<String> LOGGER_NAME_SET = new HashSet<>(3);

    static {
        LOGGER_NAME_SET.add(Const.Log.LOG_LOGGER_NAME);
        LOGGER_NAME_SET.add(Const.Log.TRACE_LOGGER_NAME);
        LOGGER_NAME_SET.add(Const.Log.METRICS_LOGGER_NAME);
    }

    private static Object lock = new Object();

    private LoggingConfig loggingConfig;

    @Override
    public void initialize(LoggingConfig config) {
        synchronized (lock) {
            loggingConfig = config;
            createLogbackLogger();
            LOGGER.info("Logback initialized.");
        }
    }

    @Override
    public LoggingConfig getLoggingConfig() {
        return loggingConfig;
    }

    private void createLogbackLogger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        removeLoggers(context);
        if (loggingConfig.getLogEnabled()) {
            createLogLogger(context);
        }
    }

    private void removeLoggers(LoggerContext context) {
        List<Logger> loggers = context.getLoggerList();
        Iterator<Logger> iterable = loggers.iterator();
        while (iterable.hasNext()) {
            Logger logger = iterable.next();
            if (LOGGER_NAME_SET.contains(logger.getName())) {
                iterable.remove();
            }
        }
    }

    private void createLogLogger(LoggerContext context) {
        Logger logger = context.getLogger(Const.Log.LOG_LOGGER_NAME);
        logger.addAppender(getConsoleAppender(context, Const.Log.CONSOLE_APPENDER_NAME, loggingConfig.getLogLevel()));
        logger.setAdditive(false);
    }

    private Appender getConsoleAppender(
            LoggerContext context, String appenderName, LogLevel level) {

        ConsoleAppender consoleAppender =
                (ConsoleAppender) getAppenderInCache(appenderName);
        if (consoleAppender != null) {
            consoleAppender.clearAllFilters();
            consoleAppender.addFilter(getThresholdFilter(level));
            return consoleAppender;
        }

        consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(context);
        consoleAppender.setName(Const.Log.CONSOLE_APPENDER_NAME);
        consoleAppender.setEncoder(getConsoleLayoutEncoder(context, Const.Log.LOGBACK_CONSOLE_PATTERN));
        consoleAppender.addFilter(getThresholdFilter(level));
        consoleAppender.start();
        APPENDER_CACHE.put(Const.Log.CONSOLE_APPENDER_NAME, consoleAppender);
        return consoleAppender;
    }

    private Encoder getConsoleLayoutEncoder(LoggerContext context, String pattern) {
        LogbackConsoleLayoutEncoder encoder = new LogbackConsoleLayoutEncoder();
        encoder.setCharset(Charset.forName("UTF-8"));
        encoder.setPattern(pattern);
        encoder.setContext(context);
        encoder.start();
        return encoder;
    }

    private ThresholdFilter getThresholdFilter(LogLevel level) {
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(level.toString());
        filter.start();
        return filter;
    }

    private Appender getAppenderInCache(String appenderName) {
        if (APPENDER_CACHE.containsKey(appenderName)) {
            Appender appender = APPENDER_CACHE.get(appenderName);
            if (!appender.isStarted()) {
                appender.start();
            }
            return appender;
        }
        return null;
    }
}
