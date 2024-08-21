package com.fei.base.common.apollo.client.spring;

import com.fei.base.common.apollo.client.listener.BaseApolloConfigChangeEvent;
import com.fei.base.common.apollo.client.listener.BaseApolloConfigListener;
import com.fei.base.common.apollo.client.listener.BaseApolloConfigListenerRegistry;
import com.fei.base.common.apollo.client.listener.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Apollo配置变更监听器注册，该类实现spring的{@link ApplicationContextAware}，
 * 会在启动时自动扫描{@link BaseApolloConfigListener}接口的实现bean并进行注册，
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/19 2:52 PM
 */
@Component
public class BaseSpringApolloConfigListenerRegistry implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BaseSpringApolloConfigListenerRegistry.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        autoRegisterApolloConfigListener();
    }

    /**
     * 扫描{@link BaseApolloConfigListener}实现，注册Apollo配置变化的监听器
     */
    protected void autoRegisterApolloConfigListener() {

        BaseApolloConfigListenerRegistry registry = BaseApolloConfigListenerRegistry.getInstance();

        // 实现通过@ConfigurationProperties配置生成的bean的自动刷新，要先注册刷新Bean的监听器
        setAutoRefreshListeners(registry);

        // 扫描全部用户自定义监听器
        Map<String, BaseApolloConfigListener> beanMap =
                applicationContext.getBeansOfType(BaseApolloConfigListener.class);
        for (BaseApolloConfigListener listener : beanMap.values()) {
            registry.addListener(listener);
        }
        LOGGER.info("Auto register listeners: {}", beanMap.size());
    }

    /**
     * 根据配置构造bean刷新事件
     *
     * @param registry
     */
    private void setAutoRefreshListeners(BaseApolloConfigListenerRegistry registry) {
        for (String namespace : registry.getNamespaces()) {
            registry.addListener(new BaseApolloConfigListener() {
                @Override
                public String namespace() {
                    return namespace;
                }
                @Override
                public void onConfigChange(BaseApolloConfigChangeEvent event) {
                    ApplicationEvent changeEvent = new ContextChangeEvent(
                            applicationContext, event.getChangedKeys());
                    applicationContext.publishEvent(changeEvent);
                }
            });
            LOGGER.info("Add apollo configuration change listener: {}", namespace);
        }
    }
}
