package com.mengxiang.base.common.log;

import com.mengxiang.base.common.log.log.LogLevel;

import java.lang.reflect.Field;

/**
 * 日志组件配置
 *
 * @author ice
 * @version 1.0
 * @date 2019/8/16 4:40 PM
 */
public class LoggingConfig {

    private LogLevel logLevel = LogLevel.INFO;

    private Boolean logEnabled = Boolean.TRUE;

    private Boolean traceEnabled = Boolean.FALSE;

    private Boolean metricsEnabled = Boolean.FALSE;

    private LoggingConfig() {
    }

    public Boolean getLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(Boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public Boolean getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(Boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public Boolean getMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(Boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        if (logLevel != null) {
            this.logLevel = logLevel;
        }
    }

    /**
     * 默认配置
     *
     * @return 日志配置
     */
    public static LoggingConfig defaultConfig() {
        return new LoggingConfig();
    }

    /**
     * 更新指定字段的值
     *
     * @param config    日志配置
     * @param fieldName 字段名
     * @param value     字段值
     */
    public static void updateField(LoggingConfig config, String fieldName, String value) {
        try {
            Field field = LoggingConfig.class.getDeclaredField(fieldName);
            Class type = field.getType();
            Object newValue = value;
            if (type == Boolean.class) {
                newValue = Boolean.valueOf(value);
            } else if (type == LogLevel.class) {
                newValue = LogLevel.of(value);
            }
            field.setAccessible(true);
            field.set(config, newValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
        }
    }
}
