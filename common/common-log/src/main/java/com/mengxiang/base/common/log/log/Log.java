package com.mengxiang.base.common.log.log;

import com.mengxiang.base.common.log.Const;
import com.mengxiang.base.common.log.LogType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志类内容
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/6/22 8:16 PM
 */
public class Log extends BaseLog {

    /**
     * 日志级别
     */
    private LogLevel level;

    /**
     * 日志内容
     */
    private String message;

    /**
     * 自定义tag，最多可以添加5个，超出后调用{@link #addTag(String, Object)}方法将不起作用
     */
    private Map<String, Object> tag = new ConcurrentHashMap<>(Const.MAX_TAG_SIZE);

    Log() {
        setType(LogType.log);
        tag = new ConcurrentHashMap<>(Const.MAX_TAG_SIZE);
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getTag() {
        return tag;
    }

    void setLevel(LogLevel level) {
        this.level = level;
    }

    void setMessage(String message) {
        this.message = message;
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

    public static LogBuilder builder() {
        return new LogBuilder();
    }
}
