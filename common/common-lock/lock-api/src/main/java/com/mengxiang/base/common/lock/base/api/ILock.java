package com.mengxiang.base.common.lock.base.api;

/**
 * @author fyc on 2020/1/6 4:09 下午.
 */
public interface ILock {
    /**
     * 默认的锁等待超时时间 单位：毫秒
     */
    long DEFAULT_TIMEOUT = 5 * 1000;

    /**
     * 默认的锁过期时间 单位：毫秒
     */
    long DEFAULT_EXPIRED_TIME = 30 * 1000;

    boolean lock();

    boolean lock(long timeout);

    boolean lock(long timeout, long expire);

    /**
     * 解锁，务必在finally中执行此模块
     *
     * @throws Exception
     */
    boolean releaseLock();
}
