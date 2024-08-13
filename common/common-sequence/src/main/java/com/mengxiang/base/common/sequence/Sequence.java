package com.mengxiang.base.common.sequence;


import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.model.Event;

/**
 * 序列接口
 *
 * @author JoinFyc
 */
public interface Sequence {

    /**
     * 取得序列下一个值
     *
     * @return 返回序列下一个值
     * @throws SequenceException
     */
    Event nextEvent() throws SequenceException;

    /**
     * 获取配置的序列最小值
     *
     * @return
     */
    long getMinValue();

    /**
     * 获取配置的序列最大值
     *
     * @return
     */
    long getMaxValue();

    /**
     * 获取code
     *
     * @return
     */
    int getCode();
}
