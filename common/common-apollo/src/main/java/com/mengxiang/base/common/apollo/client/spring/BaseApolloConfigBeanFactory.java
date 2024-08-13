package com.mengxiang.base.common.apollo.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 初始化组件
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/23 2:53 PM
 */
@Component
public class BaseApolloConfigBeanFactory implements
        ApplicationContextAware, SmartInitializingSingleton {

    private ApplicationContext context;

    @Override
    public void afterSingletonsInstantiated() {
        if (context != null && context.getParent() != null) {
            SpringPropertiesLoader loader = context.getBean(SpringPropertiesLoader.class);
            for (String name : context.getParent().getBeanDefinitionNames()) {
                loader.reload(name);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
