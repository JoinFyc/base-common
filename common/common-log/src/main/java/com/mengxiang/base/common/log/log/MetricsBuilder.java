package com.mengxiang.base.common.log.log;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.Map;

/**
 * Metrics构造器
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/29 9:54 PM
 */
public class MetricsBuilder {

    private Metrics metrics;

    MetricsBuilder() {
        metrics = new Metrics();
    }

    public MetricsBuilder aid(String aid) {
        metrics.setBusiness(aid);
        return this;
    }

    public MetricsBuilder stackTrace(StackTraceElement e) {
        Source source = new Source(e);
        metrics.setSource(source);
        return this;
    }

    public MetricsBuilder logger(String logger) {
        metrics.setLoggerName(logger);
        return this;
    }

    public MetricsBuilder fqcn(String fqcn) {
        metrics.setLoggerFqcn(fqcn);
        return this;
    }

    public MetricsBuilder metrics(String name, Number value) {
        metrics.addMetrics(name, value);
        return this;
    }

    public MetricsBuilder metrics(Map<String, Number> metrics) {
        for (Map.Entry<String, Number> entry : metrics.entrySet()) {
            this.metrics.addMetrics(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public MetricsBuilder tag(String name, Object value) {
        metrics.addTag(name, value);
        return this;
    }

    public MetricsBuilder tag(Map<String, Object> tag) {
        for (Map.Entry<String, Object> entry : tag.entrySet()) {
            metrics.addTag(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Metrics build() {
        Thread t = Thread.currentThread();
        metrics.setThread(t.getName());
        metrics.setThreadId(t.getId());
        metrics.setThreadPriority(t.getPriority());
        Instant instant = new Instant();
        metrics.setInstant(instant);
        metrics.setTimestamp(new Date(instant.getEpochSecond()));
        return metrics;
    }
}
