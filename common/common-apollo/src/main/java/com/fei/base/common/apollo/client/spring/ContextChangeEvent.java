package com.fei.base.common.apollo.client.spring;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * 上下文更新事件
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/23 1:45 PM
 */
public class ContextChangeEvent extends ApplicationEvent {

    private Set<String> keys;

    public ContextChangeEvent(Object context, Set<String> keys) {
        super(context);
        this.keys = keys;
    }

    /**
     * @return the keys
     */
    public Set<String> getKeys() {
        return keys;
    }
}
