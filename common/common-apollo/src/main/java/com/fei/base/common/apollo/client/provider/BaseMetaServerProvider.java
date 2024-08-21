package com.fei.base.common.apollo.client.provider;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 自定义MetaServer地址服务
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/5/17 4:56 PM
 */
public class BaseMetaServerProvider implements MetaServerProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMetaServerProvider.class);
    /**
     * 默认apollo地址为DEV环境
     */
    private static final String DEFAULT_META_URL = "http://localhost:8080";

    private Map<String, String> metaServers = new HashMap<>();

    public BaseMetaServerProvider() {
        initialize();
    }

    /**
     * 初始化配置文件中个环境的meta server地址
     */
    private void initialize() {
        Properties prop = new Properties();
        prop = ResourceUtils.readConfigFile("META-INF/apollo.properties", prop);
        for (String key : prop.stringPropertyNames()) {
            if (key.contains(".meta")) {
                metaServers.put(key, prop.getProperty(key));
            }
        }
    }

    @Override
    public String getMetaServerAddress(Env targetEnv) {
        String metaServer = getEnvMetaServer(targetEnv);
        LOGGER.info("Env: {}. Meta server url: {}", targetEnv.toString(), metaServer);
        return metaServer;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 读取当前环境配置
     *
     * @param targetEnv 环境
     * @return 环境对应的key
     */
    private String getEnvMetaServer(Env targetEnv) {
        String env = getMetaServerEnvString(targetEnv);
        if (env == null) {
            env = fallbackMetaServerEnvString();
        }
        if (StringUtils.isBlank(env) || !metaServers.containsKey(env + ".apollo.meta")) {
            LOGGER.warn("未能匹配任何环境，使用默认地址: {}", DEFAULT_META_URL);
            return DEFAULT_META_URL;
        }
        return metaServers.get(env + ".apollo.meta");
    }

    private String getMetaServerEnvString(Env targetEnv) {
        String env = null;
        switch (targetEnv) {
            case LOCAL:
            case DEV:
                env = "dev";
                break;
            case FAT:
                env = "test";
                break;
            case LPT:
            case FWS:
                env = "test-idc";
                break;
            case UAT:
                env = "pre";
                break;
            case PRO:
                env = "prod";
                break;
            case TOOLS:
            case UNKNOWN:
            default:
                break;
        }
        return env;
    }

    private String fallbackMetaServerEnvString() {
        LOGGER.info("No environment variables, fallback to spring.profiles.active");
        String env = System.getProperty("spring.profiles.active");
        if ("test1".equals(env) || "test2".equals(env) || "release".equals(env)) {
            return "test-idc";
        } else {
            return env;
        }
    }
}
