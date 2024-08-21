package com.mengxiang.base.common.log.log;

import java.io.Serializable;

/**
 * 时间对象
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/25 8:08 PM
 */
public class Instant implements Serializable {

    private Long epochSecond;

    private Long nanoOfSecond;

    public Instant() {
        epochSecond = System.currentTimeMillis();
        nanoOfSecond = System.nanoTime();
    }

    public Long getEpochSecond() {
        return epochSecond;
    }

    public Long getNanoOfSecond() {
        return nanoOfSecond;
    }
}
