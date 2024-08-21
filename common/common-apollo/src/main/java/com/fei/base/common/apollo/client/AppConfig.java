package com.fei.base.common.apollo.client;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.AbstractConfig;

import java.util.Set;

/**
 * 配置对象
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/19 9:36 PM
 */
public class AppConfig extends AbstractConfig {

    private Config config;

    public AppConfig(Config config) {
        this.config = config;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    @Override
    public Set<String> getPropertyNames() {
        return config.getPropertyNames();
    }

    @Override
    public ConfigSourceType getSourceType() {
        return config.getSourceType();
    }
}
