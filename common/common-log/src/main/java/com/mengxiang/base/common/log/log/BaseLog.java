package com.mengxiang.base.common.log.log;

import com.mengxiang.base.common.log.LogType;

import java.io.Serializable;
import java.util.Date;

/**
 * Log基础信息
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/26 1:43 PM
 */
public class BaseLog implements Serializable {

    private LogType type;

    /**
     * Logger名称
     */
    private String loggerName;

    /**
     * Logger类名
     */
    private String loggerFqcn;

    /**
     * 当前时间点，毫秒和微秒
     */
    private Instant instant;

    /**
     * 日志时间
     */
    private Date timestamp;

    /**
     * 应用ID
     */
    private String business;

    /**
     * 记录日志的位置
     */
    private Source source;

    /**
     * 线程名称
     */
    private String thread;

    /**
     * 线程ID
     */
    private Long threadId;

    /**
     * 线程优先级
     */
    private Integer threadPriority;

    public LogType getType() {
        return type;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getLoggerFqcn() {
        return loggerFqcn;
    }

    public Instant getInstant() {
        return instant;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getBusiness() {
        return business;
    }

    public Source getSource() {
        return source;
    }

    public String getThread() {
        return thread;
    }

    public Long getThreadId() {
        return threadId;
    }

    public Integer getThreadPriority() {
        return threadPriority;
    }

    void setType(LogType type) {
        this.type = type;
    }

    void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    void setLoggerFqcn(String loggerFqcn) {
        this.loggerFqcn = loggerFqcn;
    }

    void setInstant(Instant instant) {
        this.instant = instant;
    }

    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    void setBusiness(String business) {
        this.business = business;
    }

    void setSource(Source source) {
        this.source = source;
    }

    void setThread(String thread) {
        this.thread = thread;
    }

    void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    void setThreadPriority(Integer threadPriority) {
        this.threadPriority = threadPriority;
    }
}
