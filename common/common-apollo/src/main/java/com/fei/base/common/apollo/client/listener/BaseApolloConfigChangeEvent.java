package com.fei.base.common.apollo.client.listener;

import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Apollo配置变更日志
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/22 1:36 PM
 */
@Data
public class BaseApolloConfigChangeEvent {

    private String namespace;

    private Map<String, BaseApolloConfigChange> changes;

    public Set<String> getChangedKeys() {
        return this.changes == null ? Collections.emptySet() : this.changes.keySet();
    }
}
