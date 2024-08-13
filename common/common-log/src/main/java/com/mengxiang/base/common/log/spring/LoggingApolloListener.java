package com.mengxiang.base.common.log.spring;

import com.mengxiang.base.common.apollo.client.listener.BaseApolloConfigChange;
import com.mengxiang.base.common.apollo.client.listener.BaseApolloConfigChangeEvent;
import com.mengxiang.base.common.apollo.client.listener.BaseApolloConfigListener;
import com.mengxiang.base.common.log.ILoggerInitializer;
import com.mengxiang.base.common.log.LoggerInitializerFactory;
import com.mengxiang.base.common.log.LoggingConfig;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 日志组件apollo监听器
 *
 * @author ice
 * @version 1.0
 * @date 2019/8/15 10:41 AM
 */
public class LoggingApolloListener implements BaseApolloConfigListener {

    private static final String LOGGING_KEY_PREFIX = "app.logging.";

    private LoggingSystem loggingSystem;

    public LoggingApolloListener(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }

    @Override
    public String namespace() {
        return "*";
    }

    @Override
    public void onConfigChange(BaseApolloConfigChangeEvent event) {
        if (logRefreshed(event)) {
            return;
        }
        ILoggerInitializer initializer = LoggerInitializerFactory
                .getLoggerInitializer(LoggingApolloListener.class.getClassLoader());
        LoggingConfig config = initializer.getLoggingConfig();
        for (String key : event.getChangedKeys()) {
            if (key.contains(LOGGING_KEY_PREFIX)) {
                BaseApolloConfigChange change = event.getChanges().get(key);
                String newValue = change.getNewValue();
                if (StringUtils.hasText(newValue) && !Objects.equals(change.getOldValue(), newValue)) {
                    LoggingConfig.updateField(config, getFieldName(key), newValue);
                    updateSpringbootLogLevel(newValue);
                }
            }
        }
        initializer.initialize(config);
    }

    private boolean logRefreshed(BaseApolloConfigChangeEvent event) {
        boolean needRefreshLog = false;
        for (String changedKey : event.getChangedKeys()) {
            if (changedKey.contains(LOGGING_KEY_PREFIX)) {
                needRefreshLog = true;
                break;
            }
        }
        return needRefreshLog;
    }

    private void updateSpringbootLogLevel(String newValue) {
        if ("debug".equalsIgnoreCase(newValue)) {
            loggingSystem.setLogLevel("", LogLevel.DEBUG);
        }
        if ("info".equalsIgnoreCase(newValue)) {
            loggingSystem.setLogLevel("", LogLevel.INFO);
        }
        if ("warn".equalsIgnoreCase(newValue)) {
            loggingSystem.setLogLevel("", LogLevel.WARN);
        }
        if ("error".equalsIgnoreCase(newValue)) {
            loggingSystem.setLogLevel("", LogLevel.ERROR);
        }
    }

    private String getFieldName(String key) {
        return key.substring(LOGGING_KEY_PREFIX.length());
    }
}
