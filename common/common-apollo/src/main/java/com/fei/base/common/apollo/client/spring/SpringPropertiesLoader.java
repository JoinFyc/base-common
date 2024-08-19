package com.fei.base.common.apollo.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 属性bean加载器，当收到配置发生变化的通知时，
 * 重新加载通过{@link org.springframework.boot.context.properties.ConfigurationProperties}自动注入的bean
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/23 1:44 PM
 */
@Component
public class SpringPropertiesLoader implements
        ApplicationContextAware, ApplicationListener<ContextChangeEvent> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SpringPropertiesLoader.class);

    private ApplicationContext context;

    private ConfigurableListableBeanFactory beanFactory;

    /**
     * {@code @ConfigurationProperties}标注的bean集合对象
     */
    private BaseConfigurationPropertiesBeans beans;

    public SpringPropertiesLoader(BaseConfigurationPropertiesBeans beans) {
        this.beans = beans;
    }

    /**
     * 重新加载所有{@link org.springframework.boot.context.properties.ConfigurationProperties}标注的bean
     */
    public void reloadAll(Set<String> keys) {
        if (keys.isEmpty()) {
            LOGGER.warn("Changed keys for reloading are empty.");
            return;
        }
        LOGGER.info("Start to reload with annotation @ConfigurationProperties beans. ");
        for (String beanName : beans.getBeanNames()) {
            try {
                Object bean = context.getBean(beanName);
                ConfigurationProperties cp = AnnotationUtils.findAnnotation(
                        bean.getClass(), ConfigurationProperties.class);
                if (cp != null && containsPrefix(keys, cp.prefix())) {
                    reload(beanName);
                }
            } catch (NoSuchBeanDefinitionException ex) {
                LOGGER.error("No such bean: {}", beanName);
            }
        }
    }

    private boolean containsPrefix(Set<String> keys, String prefix) {
        for (String key : keys) {
            if (key.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新加载bean
     *
     * @param beanName bean名称
     */
    public void reload(String beanName) {
        if (context != null && beanFactory != null) {
            try {
                Object bean = context.getBean(beanName);
                if (AopUtils.isAopProxy(bean)) {
                    bean = AopUtils.getTargetClass(bean);
                }
                beanFactory.destroyBean(bean);
                beanFactory.initializeBean(bean, beanName);
                LOGGER.info("Reload @ConfigurationProperties bean: {}", beanName);

            } catch (RuntimeException e) {
                LOGGER.error("Reload bean error: {}", beanName, e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        if (context.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory();
        }
    }

    @Override
    public void onApplicationEvent(ContextChangeEvent event) {
        // 判断是否在同一个上下文中
        if (context.equals(event.getSource())) {
            reloadAll(event.getKeys());
        }
    }
}
