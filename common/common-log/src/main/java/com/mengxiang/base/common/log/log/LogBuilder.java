package com.mengxiang.base.common.log.log;

import com.alibaba.fastjson.JSONObject;
import com.mengxiang.base.common.log.PidUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.Date;
import java.util.Map;

/**
 * Log构造器
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/10 9:19 PM
 */
public class LogBuilder {

    private Log log;

    LogBuilder() {
        log = new Log();
    }

    public LogBuilder level(LogLevel level) {
        log.setLevel(level);
        return this;
    }

    public LogBuilder aid(String aid) {
        log.setBusiness(aid);
        return this;
    }

    public LogBuilder stackTrace(StackTraceElement e) {
        Source source = new Source(e);
        log.setSource(source);
        return this;
    }

    public LogBuilder logger(String logger) {
        log.setLoggerName(logger);
        return this;
    }

    public LogBuilder fqcn(String fqcn) {
        log.setLoggerFqcn(fqcn);
        return this;
    }

    public LogBuilder msg(String msg) {
        log.setMessage(msg);
        return this;
    }

    public LogBuilder tag(String name, Object value) {
        log.addTag(name, value);
        return this;
    }

    public LogBuilder tag(Map<String, Object> tag) {
        for (Map.Entry<String, Object> entry : tag.entrySet()) {
            log.addTag(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Log build() {
        Thread t = Thread.currentThread();
        log.setThread(t.getName());
        log.setThreadId(t.getId());
        log.setThreadPriority(t.getPriority());
        Instant instant = new Instant();
        log.setTimestamp(new Date(instant.getEpochSecond()));
        log.setInstant(instant);
        initPidMdc();
        initCustomMdc();
        return log;
    }

    private void initCustomMdc() {
        Map<String, Object> tag = log.getTag();
        if (tag != null && tag.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("|");
            for (Map.Entry<String, Object> entry : tag.entrySet()) {
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue().toString());
            }
            MDC.put("custom", sb.toString());
        }
    }

    private void initPidMdc() {
        MDC.put("PID", PidUtils.currentPid());
    }
}
