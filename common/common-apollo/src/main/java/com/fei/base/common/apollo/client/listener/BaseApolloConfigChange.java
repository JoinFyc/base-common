package com.fei.base.common.apollo.client.listener;

import lombok.Data;

/**
 * Apollo配置变更对象
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/22 1:38 PM
 */

@Data
public class BaseApolloConfigChange {

    private String namespace;

    private String key;

    private String oldValue;

    private String newValue;

    private BaseApolloConfigChangeType changeType;

}
