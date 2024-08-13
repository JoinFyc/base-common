package com.mengxiang.base.common.lock.base.api;

/**
 * @author JoinFyc
 * @date 2020-09-22 16:16
 **/
public interface ILockFactory {
    /**
     * 获取lock
     *
     * @param key
     * @return
     */
    ILock getLock(String key);
}
