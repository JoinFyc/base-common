package com.mengxiang.base.common.log.spring;

import com.mengxiang.base.common.log.ILoggerInitializer;
import com.mengxiang.base.common.log.LoggerInitializerFactory;
import com.mengxiang.base.common.log.LoggingConfig;
import com.mengxiang.base.common.log.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Springboot项目启动时，通过该Initializer初始化日志配置
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/23 12:00 AM
 */
public class SpringBootLoggerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootLoggerInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {

        LoggingConfig config = loadLoggingConfig(context.getEnvironment());
        ILoggerInitializer initializer = LoggerInitializerFactory
                .getLoggerInitializer(context.getClassLoader());
        if (initializer != null) {
            initializer.initialize(config);
        } else {
            LOGGER.warn("Create logger initializer failed.");
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 10;
    }

    private LoggingConfig loadLoggingConfig(ConfigurableEnvironment env) {
        String logLevel = env.getProperty("app.logging.logLevel", "INFO");
        String logEnabled = env.getProperty("app.logging.logEnabled", "true");
        String traceEnabled = env.getProperty("app.logging.traceEnabled", "false");
        String metricsEnabled = env.getProperty("app.logging.metricsEnabled", "false");

        LoggingConfig config = LoggingConfig.defaultConfig();
        config.setLogLevel(LogLevel.of(logLevel));
        config.setLogEnabled(Boolean.valueOf(logEnabled));
        config.setTraceEnabled(Boolean.valueOf(traceEnabled));
        config.setMetricsEnabled(Boolean.valueOf(metricsEnabled));
        return config;
    }
}
