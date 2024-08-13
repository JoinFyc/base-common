package com.mengxiang.base.common.log.log;

import com.mengxiang.base.common.log.Const;
import com.mengxiang.base.common.log.LogType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics类内容
 *
 * @author ice
 * @version 1.0
 * @date 2019/7/29 9:32 PM
 */
public class Metrics extends BaseLog {

    private Map<String, Number> metrics;

    private Map<String, Object> tag;

    Metrics() {
        setType(LogType.metrics);
        metrics = new ConcurrentHashMap<>(Const.DEFAULT_METRICS_SIZE);
        tag = new ConcurrentHashMap<>(Const.MAX_TAG_SIZE);
    }

    public Map<String, ? extends Number> getMetrics() {
        return metrics;
    }

    public Map<String, Object> getTag() {
        return tag;
    }

    <T extends Number> void addMetrics(String metricsName, T metricsValue) {
        metrics.put(metricsName, metricsValue);
    }

    void removeMetrics(String name) {
        metrics.remove(name);
    }

    synchronized void addTag(String name, Object value) {
        boolean lessThanMaxSize = (tag.size() < Const.MAX_TAG_SIZE);
        boolean equalsMaxSizeAndContainsKey = (tag.size() == Const.MAX_TAG_SIZE && tag.containsKey(name));
        if (lessThanMaxSize || equalsMaxSizeAndContainsKey) {
            tag.put(name, value);
        }
    }

    void removeTag(String name) {
        tag.remove(name);
    }

    public static MetricsBuilder builder() {
        return new MetricsBuilder();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(Map.Entry<String, Number> e : metrics.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append(";");
        }
        sb.append("}");
        return sb.toString();
    }
}
