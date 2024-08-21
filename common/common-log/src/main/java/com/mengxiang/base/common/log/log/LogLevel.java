package com.mengxiang.base.common.log.log;

/**
 * 日志级别
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/2 9:44 AM
 */
public enum LogLevel {

    /**
     * 调试
     */
    DEBUG,

    /**
     * 信息
     */
    INFO,

    /**
     * 警告
     */
    WARN,

    /**
     * 错误
     */
    ERROR,
    ;

    public static LogLevel of(String name) {
        for(LogLevel level : values()) {
            if(level.toString().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }
}
