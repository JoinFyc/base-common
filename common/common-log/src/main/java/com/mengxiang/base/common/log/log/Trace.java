package com.mengxiang.base.common.log.log;

import com.mengxiang.base.common.log.Const;
import com.mengxiang.base.common.log.LogType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trace类内容
 *
 * @author ice
 * @version 1.0
 * @date 2019/6/22 7:58 PM
 */
public class Trace extends BaseLog {

    /**
     * 调用链路ID，贯穿整个请求链路
     */
    private String traceId;

    /**
     * 时间片ID，每次开启追踪会生成一个新的时间片ID
     */
    private String spanId;

    /**
     * 父级时间片ID
     */
    private String parentId;

    /**
     * 自定义tag，最多可以添加5个，超出后调用{@link #addTag(String, Object)}方法将不起作用
     */
    private Map<String, Object> tag;

    Trace() {
        setType(LogType.trace);
        tag = new ConcurrentHashMap<>(Const.MAX_TAG_SIZE);
    }

    public String getTraceId() {
        return traceId;
    }

    void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentId() {
        return parentId;
    }

    void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getTag() {
        return tag;
    }

    public synchronized void addTag(String name, Object value) {
        boolean lessThanMaxSize = (tag.size() < Const.MAX_TAG_SIZE);
        boolean equalsMaxSizeAndContainsKey = (tag.size() == Const.MAX_TAG_SIZE && tag.containsKey(name));
        if (lessThanMaxSize || equalsMaxSizeAndContainsKey) {
            tag.put(name, value);
        }
    }

    public void removeTag(String name) {
        tag.remove(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Trace)) {
            return false;
        } else {
            Trace other = (Trace) obj;
            return other.getTraceId().equals(this.traceId) && other.getSpanId().equals(this.spanId);
        }
    }

    public static TraceBuilder builder() {
        return new TraceBuilder();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("time:").append(getTimestamp())
                .append(",traceId:").append(getTraceId())
                .append(",spanId:").append(getSpanId())
                .append(",parentId:").append(getParentId())
                .append(",tag:").append("{");
        for (Map.Entry<String, Object> e : tag.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append(";");
        }
        sb.append("}");
        return sb.toString();
    }
}
