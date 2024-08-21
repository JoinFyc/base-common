package com.mengxiang.base.common.log;

/**
 * 日志配置初始化接口
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/26 5:17 PM
 */
public interface ILoggerInitializer {

    /**
     * 初始化Logger
     *
     * @param config 日志配置
     */
    void initialize(LoggingConfig config);

    /**
     * 获取日志配置
     *
     * @return 日志配置
     */
    LoggingConfig getLoggingConfig();
}
