package com.mengxiang.base.common.log.spring;

import com.fei.base.common.apollo.client.listener.BaseApolloConfigListener;
import com.fei.base.common.apollo.client.spring.BaseSpringApolloConfigListenerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置日志组件
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/8/15 1:10 PM
 */
@Configuration
@ConditionalOnClass(BaseApolloConfigListener.class)
@ConditionalOnProperty(prefix = "app.logging", name = "level", matchIfMissing = true)
public class LoggingListenerAutoConfiguration {

    /**
     * 初始化Apollo日志配置监听器
     *
     * @return
     */
    @ConditionalOnClass(BaseApolloConfigListener.class)
    @ConditionalOnBean(BaseSpringApolloConfigListenerRegistry.class)
    @ConditionalOnMissingBean
    @Bean
    public LoggingApolloListener loggingApolloListener(LoggingSystem loggingSystem) {
        return new LoggingApolloListener(loggingSystem);
    }
}
