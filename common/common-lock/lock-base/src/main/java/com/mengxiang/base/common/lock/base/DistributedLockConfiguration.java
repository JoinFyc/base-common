package com.mengxiang.base.common.lock.base;

import com.mengxiang.base.common.lock.base.api.ILockFactory;
import com.mengxiang.base.common.lock.base.simple.SimpleLockFactory;
//import com.mengxiang.base.common.lock.zookeeper.ZookeeperLockFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author JoinFyc
 * @date 2020-09-22 16:16
 **/
public class DistributedLockConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(ILockFactory.class)
//    @ConditionalOnClass(name = "com.mengxiang.base.common.lock.zookeeper.ZookeeperLockFactory")
//    public ILockFactory getZookeeperLockFactory() {
//        return new ZookeeperLockFactory();
//    }

    @Bean
    @ConditionalOnMissingBean(ILockFactory.class)
    public ILockFactory getSimpleLockFactory(StringRedisTemplate redisTemplate) {
        return new SimpleLockFactory(redisTemplate);
    }
}
