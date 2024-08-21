package com.mengxiang.base.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * Logger初始化器的工厂类
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/26 5:14 PM
 */
public final class LoggerInitializerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerInitializerFactory.class);

    private static final String LOGBACK = "ch.qos.logback.core.Appender";
    private static final String LOG4J2 = "org.apache.logging.log4j.core.impl.Log4jContextFactory";
    private static final String LOG4J2_TO_SLF4j = "org.apache.logging.slf4j.SLF4JProvider";

    private static ILoggerInitializer log4j2Initializer = null;
    private static ILoggerInitializer logbackInitializer = null;
    private static ILoggerInitializer initializer = null;


    private static ILoggerInitializer getLog4j2Initializer() {
        if (log4j2Initializer == null) {
            log4j2Initializer = new Log4j2Initializer();
        }
        return log4j2Initializer;
    }

    private static ILoggerInitializer getLogbackInitializer() {
        if (logbackInitializer == null) {
            logbackInitializer = new LogbackInitializer();
        }
        return logbackInitializer;
    }

    /**
     * 获取Logger初始化类
     *
     * @return {@link Log4j2Initializer}
     */
    public static ILoggerInitializer getLoggerInitializer(ClassLoader classLoader) {

        if (initializer != null) {
            return initializer;
        }

        // 如果log4j2被引入，优先使用log4j2，其次使用logback。如果这两个组件都没有被使用则不做自定义日志组件的初始化。
        if (ClassUtils.isPresent(LOG4J2, classLoader) && !ClassUtils.isPresent(LOG4J2_TO_SLF4j, classLoader)) {
            initializer = getLog4j2Initializer();
        } else if (ClassUtils.isPresent(LOGBACK, classLoader)) {
            initializer = getLogbackInitializer();
        }
        if (initializer == null) {
            LOGGER.error("Neither log4j2 nor logback exists in classpath.");
        }
        return initializer;
    }
}
