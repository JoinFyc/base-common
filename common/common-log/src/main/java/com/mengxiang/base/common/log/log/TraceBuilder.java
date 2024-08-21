package com.mengxiang.base.common.log.log;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Trace构造器
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/26 2:41 PM
 */
public class TraceBuilder {

    private static final String ROOT_SPAN = "root";
    private Trace trace;

    TraceBuilder() {
        trace = new Trace();
    }

    public TraceBuilder aid(String aid) {
        trace.setBusiness(aid);
        return this;
    }

    public TraceBuilder stackTrace(StackTraceElement e) {
        Source source = new Source(e);
        trace.setSource(source);
        return this;
    }

    public TraceBuilder logger(String logger) {
        trace.setLoggerName(logger);
        return this;
    }

    public TraceBuilder fqcn(String fqcn) {
        trace.setLoggerFqcn(fqcn);
        return this;
    }

    public TraceBuilder tag(String name, Object value) {
        trace.addTag(name, value);
        return this;
    }

    public TraceBuilder tag(Map<String, Object> tag) {
        for (Map.Entry<String, Object> entry : tag.entrySet()) {
            trace.addTag(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 通过Span的起始Trace对象构造Span的结束Trace对象
     *
     * @param trace
     * @return
     */
    public TraceBuilder withStartTrace(Trace trace) {
        this.trace.setTraceId(trace.getTraceId());
        this.trace.setSpanId(trace.getSpanId());
        this.trace.setParentId(trace.getParentId());
        this.trace.setBusiness(trace.getBusiness());
        this.trace.setLoggerFqcn(trace.getLoggerFqcn());
        this.trace.setLoggerName(trace.getLoggerName());
        return this;
    }

    /**
     * 通过父级Span的起始Trace对象，构造当前Span的起始Trace对象
     *
     * @param trace
     * @return
     */
    public TraceBuilder withParentTrace(Trace trace) {
        this.trace.setTraceId(trace.getTraceId());
        this.trace.setParentId(trace.getSpanId());
        this.trace.setBusiness(trace.getBusiness());
        return this;
    }

    public Trace build() {
        if (trace.getTraceId() == null) {
            trace.setTraceId(UUID.randomUUID().toString());
        }
        if (trace.getParentId() == null) {
            trace.setParentId(ROOT_SPAN);
        }
        Thread t = Thread.currentThread();
        trace.setThread(t.getName());
        trace.setThreadId(t.getId());
        trace.setThreadPriority(t.getPriority());
        Instant instant = new Instant();
        trace.setTimestamp(new Date(instant.getEpochSecond()));
        trace.setInstant(instant);
        if (trace.getSpanId() == null) {
            trace.setSpanId(genSpanId(instant));
        }
        return trace;
    }

    private String genSpanId(Instant instant) {
        return "spanId";
    }
}
