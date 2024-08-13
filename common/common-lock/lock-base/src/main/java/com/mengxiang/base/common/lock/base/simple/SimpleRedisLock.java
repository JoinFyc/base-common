package com.mengxiang.base.common.lock.base.simple;

import com.mengxiang.base.common.lock.base.api.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 简单的redis分布式锁，只提供简单的自旋抢占锁及释放功能，适配大部分场景，如需要复杂的分布式锁逻辑请使用更复杂的实现(lock-zookeeper)
 *
 * @author JoinFyc
 * @date 2020-09-22 16:16
 **/
public class SimpleRedisLock implements ILock {

    private final static Logger logger = LoggerFactory.getLogger(SimpleRedisLock.class);
    private String lockKey;
    private String keyValue;

    /**
     * 锁的后缀
     */
    private static final String LOCK_PREFIX = "_LOCK_";

    /**
     * 是否锁定标志
     */
    private volatile boolean locked = false;

    private RedisTemplate redisTemplate;

    SimpleRedisLock(String lockKey, RedisTemplate redisTemplate) {
        this.lockKey = LOCK_PREFIX + lockKey;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 获取锁
     *
     * @return 获取锁成功返回ture，超时返回false
     */
    @Override
    public boolean lock() {
        return lock(DEFAULT_TIMEOUT);
    }

    /**
     * 获取锁
     *
     * @param timeout 锁超时时间
     * @return 获取锁成功返回ture，超时返回false
     */
    @Override
    public boolean lock(long timeout) {
        return lock(timeout, DEFAULT_EXPIRED_TIME);
    }

    /**
     * 获取锁
     *
     * @param timeout 锁超时时间 单位：毫秒
     * @param expire  锁过期时间 单位：毫秒
     * @return 获取锁成功返回ture，超时返回false
     */
    @Override
    public boolean lock(long timeout, long expire) {
        long nano = System.nanoTime();
        timeout *= 1000000;
        try {
            //自旋锁
            while ((System.nanoTime() - nano) < timeout) {
                keyValue = getKeyValue(expire);
                if (redisTemplate.opsForValue().setIfAbsent(lockKey, keyValue, expire, TimeUnit.MILLISECONDS)) {
                    locked = true;
                    logger.debug("add RedisLock[" + lockKey + "].");
                    break;
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            logger.warn("获取锁异常", e);
        }
        return locked;
    }

    /**
     * 释放获取到的锁
     */
    @Override
    public boolean releaseLock() {
        if (locked) {
            String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
            if (currentValue == null) {
                return true;
            }
            //假如值与自己的值不同，该key是其他人设置的，不取消
            //极限情况，可能会出错，判断完lock的value后,正好过期，其他线程抢占成功
            if (currentValue.equals(keyValue)) {
                return redisTemplate.delete(lockKey);
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * 获取keyValue
     *
     * @param expire
     * @return
     */
    private String getKeyValue(long expire) {
        return System.currentTimeMillis() + expire * 1000 + 1 + "";
    }
}
