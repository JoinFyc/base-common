package com.mengxiang.base.common.lock.base.simple;

import com.mengxiang.base.common.lock.base.api.ILock;
import com.mengxiang.base.common.lock.base.api.ILockFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author JoinFyc
 * @date 2020-09-22 16:16
 **/
public class SimpleLockFactory implements ILockFactory {
    private StringRedisTemplate stringRedisTemplate;

    public SimpleLockFactory(StringRedisTemplate redisTemplate) {
        this.stringRedisTemplate = redisTemplate;
    }

    @Override
    public ILock getLock(String key) {
        return new SimpleRedisLock(key, stringRedisTemplate);
    }
}
