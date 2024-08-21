package com.fei.base.common.apollo.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通过{@link ConfigurationProperties}注入的bean的集合
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/4/23 2:02 PM
 */
@Component
public class BaseConfigurationPropertiesBeans
        implements BeanPostProcessor, ApplicationContextAware {

    private Map<String, Object> beans = new HashMap<>();

    private BaseConfigurationPropertiesBeans parent;

    /**
     * postProcessBeforeInitialization方法会在setApplicationContext方法之前执行
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        ConfigurationProperties annotation = AnnotationUtils.findAnnotation(
                bean.getClass(), ConfigurationProperties.class);
        if (annotation != null) {
            this.beans.put(beanName, bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    public Set<String> getBeanNames() {
        return beans.keySet();
    }

    /**
     * 检查父级上下文中是否存在{@link BaseConfigurationPropertiesBeans}，
     * 如果存在，使用父级的bean覆盖子上下文中重复的bean
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext.getParent() != null) {
            ApplicationContext parent = applicationContext.getParent();
            if (parent.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory) {
                ConfigurableListableBeanFactory listable = (ConfigurableListableBeanFactory) applicationContext
                        .getParent().getAutowireCapableBeanFactory();
                String[] names = listable.getBeanNamesForType(BaseConfigurationPropertiesBeans.class);
                if (names.length == 1) {
                    this.parent = (BaseConfigurationPropertiesBeans) listable.getBean(names[0]);
                    this.beans.putAll(this.parent.beans);
                }
            }
        }
    }
}
