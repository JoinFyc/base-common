package com.mengxiang.base.common.log;

import com.mengxiang.base.common.log.layout.Log4j2ConsoleLayoutProxy;
import com.mengxiang.base.common.log.log.LogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log4j2动态Logger的初始化
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/19 4:00 PM
 */
public class Log4j2Initializer implements ILoggerInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Log4j2Initializer.class);
    private static final String log4jContextName = "org.apache.logging.log4j.core.LoggerContext";

    static {
    }

    private static Object lock = new Object();

    private LoggingConfig loggingConfig;

    @Override
    public void initialize(LoggingConfig config) {
        synchronized (lock) {
            loggingConfig = config;
            createLog4j2Logger();
            LOGGER.info("Log4j2 initialized.");
        }
    }

    @Override
    public LoggingConfig getLoggingConfig() {
        return loggingConfig;
    }

    private void createLog4j2Logger() {
        org.apache.logging.log4j.spi.LoggerContext loggerContext = LogManager.getContext();
        LoggerContext context = null;
        if (log4jContextName.equals(loggerContext)) {
            context = (LoggerContext) loggerContext;
        } else {
            LOGGER.error("can't find log4j context");
            return;
        }
        Configuration config = context.getConfiguration();
        config.removeLogger(Const.Log.LOG_LOGGER_NAME);
        config.removeLogger(Const.Log.TRACE_LOGGER_NAME);
        config.removeLogger(Const.Log.METRICS_LOGGER_NAME);

        // 构造自定义logger
        if (loggingConfig.getLogEnabled()) {
            createLogLogger(config);
        }
        context.updateLoggers(config);
    }

    private void createLogLogger(Configuration config) {

        AppenderRef consoleAppenderRef = AppenderRef.createAppenderRef(
                Const.Log.CONSOLE_APPENDER_NAME, null, null);
        AppenderRef infoLogAppenderRef = AppenderRef.createAppenderRef(
                Const.Log.INFO_LOG_APPENDER_NAME, null, null);
        AppenderRef errorLogAppenderRef = AppenderRef.createAppenderRef(
                Const.Log.ERROR_LOG_APPENDER_NAME, null, null);

        AppenderRef[] logAppenderRefs = new AppenderRef[]{
                consoleAppenderRef,
                infoLogAppenderRef,
                errorLogAppenderRef
        };

        LoggerConfig logConfig = LoggerConfig.createLogger(
                Boolean.FALSE, exchange(loggingConfig.getLogLevel()),
                Const.Log.LOG_LOGGER_NAME, Const.Log.INCLUDE_LOCATION,
                logAppenderRefs, null, config, null);

        Appender consoleAppender = getConsoleAppender(config);
        logConfig.addAppender(consoleAppender, exchange(loggingConfig.getLogLevel()), null);

        config.addLogger(Const.Log.LOG_LOGGER_NAME, logConfig);
    }

    /**
     * 获取控制台的Appender对象
     *
     * @param config 日志配置
     * @return ConsoleAppender
     */
    private Appender getConsoleAppender(Configuration config) {

        Appender appender = config.getAppender(Const.Log.CONSOLE_APPENDER_NAME);
        if (appender != null) {
            return appender;
        }
        appender = ConsoleAppender.newBuilder().setName(Const.Log.CONSOLE_APPENDER_NAME).setLayout(getConsoleLayout())
                .build();
        appender.start();
        config.addAppender(appender);
        return appender;
    }

    private Layout getConsoleLayout() {
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern(Const.Log.LOG4J2_CONSOLE_PATTERN)
                .build();
        Log4j2ConsoleLayoutProxy proxy = new Log4j2ConsoleLayoutProxy(layout);
        return (Layout) proxy.getProxy();
    }

    private Level exchange(LogLevel level) {
        switch (level) {
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            default:
                return Level.INFO;
        }
    }

}
