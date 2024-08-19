package com.fei.base.common.apollo.client.spring;

import com.google.common.base.Joiner;
import com.fei.base.common.apollo.client.listener.BaseApolloConfigListenerRegistry;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ResourceUtils;
import com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.google.common.base.Splitter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Apollo初始化
 * 扩展Apollo上线文初始化过程，插入特殊的namespace
 *
 * @author ice
 * @version 1.0
 * @date 2019/5/22 1:12 AM
 */
public class BaseApolloInitializer extends ApolloApplicationContextInitializer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BaseApolloInitializer.class);
    private final static String LOCAL_CONFIG = "META-INF/apollo.properties";
    private final static String CRITICAL_NAMESPACE = "critical.namespace";
    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(",").omitEmptyStrings().trimResults();
    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    @Override
    protected void initialize(ConfigurableEnvironment environment) {

        // 读取环境配置，设置对应的环境额和集群参数
        String[] actives = environment.getActiveProfiles();
        fillDefaultCluster(actives);

        super.initialize(environment);
        // 获取全部namespace
        String namespaces = environment.getProperty(
                PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
        LOGGER.debug("Apollo bootstrap namespaces: {}", namespaces);
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);
        BaseApolloConfigListenerRegistry registry = BaseApolloConfigListenerRegistry.getInstance();
        registry.setNamespaces(namespaceList);

        // 读取敏感配置的namespace，插入到namespace的列表中
        Properties prop = new Properties();
        prop = ResourceUtils.readConfigFile(LOCAL_CONFIG, prop);
        String criticalNamespace = prop.getProperty(CRITICAL_NAMESPACE);

        Config config = ConfigService.getConfig(criticalNamespace);
        CompositePropertySource propertySource = (CompositePropertySource) environment.getPropertySources()
                .get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        propertySource.addFirstPropertySource(configPropertySourceFactory
                .getConfigPropertySource(criticalNamespace, config));
    }

    private static final Set<String> TEST_ACTIVE_SET = new HashSet<String>() {{
        add("test1");
        add("test2");
        add("release");
        add("stable");
    }};

    private static final Set<String> OTHER_ACTIVE_SET = new HashSet<String>() {{
        add("local");
        add("dev");
        add("test");
        add("pre");
        add("prod");
    }};

    private static final String TEST_ENV = "test";
    private static final String PRE_ENV = "pre";

    /**
     * 初始化时依据当前环境设置系统变量
     *
     * @param actives
     */
    private void fillDefaultCluster(String[] actives) {
        Set<String> clusterSet = new HashSet<>();
        for (String active : actives) {
            if (TEST_ACTIVE_SET.contains(active)) {
                clusterSet.add(active);
            }
            String env = transferEnv(active);
            if (env != null) {
                System.setProperty("env", env);
            }
        }
        if (CollectionUtils.isNotEmpty(clusterSet)) {
            System.setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, Joiner.on(",").join(clusterSet));
        }
    }

    /**
     * 读取当前环境配置
     *
     * @param active 环境参数
     * @return 环境对应的key
     */
    private String transferEnv(String active) {
        if (StringUtils.isEmpty(active)) {
            return null;
        }
        if (TEST_ACTIVE_SET.contains(active) || OTHER_ACTIVE_SET.contains(active)) {
            if (PRE_ENV.equals(active)) {
                return "uat";
            } else if (TEST_ENV.equals(active)) {
                return "fat";
            } else if (TEST_ACTIVE_SET.contains(active)) {
                return "lpt";
            } else {
                return active;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 5;
    }
}
