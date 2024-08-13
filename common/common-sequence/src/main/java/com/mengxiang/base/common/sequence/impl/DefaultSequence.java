package com.mengxiang.base.common.sequence.impl;

import com.mengxiang.base.common.sequence.Sequence;
import com.mengxiang.base.common.sequence.SequenceRange;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 序列默认实现
 *
 * @author JoinFyc
 */
public class DefaultSequence implements Sequence {

    private static final Logger logger = LoggerFactory
            .getLogger(SequenceConstant.SEQUENCE_LOG_NAME);

    private final Lock lock = new ReentrantLock();

    /**
     * 序列DAO
     */
    private DefaultSequenceDAO sequenceDAO;

    /**
     * 默认步长
     */
    private static final int DEFAULT_STEP = 1000;
    /**
     * 默认sequence的最小值
     */
    private static final long DEFAULT_MIN_VALUE = 0;
    /**
     * 默认sequence的最大值
     */
    private static final long DEFAULT_MAX_VALUE = Long.MAX_VALUE;

    /**
     * 内步长
     */
    private int innerStep = DEFAULT_STEP;

    /**
     * 最小值
     */
    private long minValue = DEFAULT_MIN_VALUE;

    /**
     * 最大值
     */
    private long maxValue = DEFAULT_MAX_VALUE;

    /**
     * 序列名称
     */
    private String sequenceName;

    /**
     * 业务code
     */
    private final Integer sequenceCode;
    /**
     * sequence 段
     */
    private volatile SequenceRange currentRange;

    private volatile long expireTime = -1L;

    /**
     * 在db里如果存在记录的情况下，调用的构造函数,此时不需要再初始化
     *
     * @param sequenceDAO
     * @param sequenceName 序列名称
     * @param innerStep    内步长
     * @param minValue     最小值
     * @param maxValue     最大值
     */
    public DefaultSequence(DefaultSequenceDAO sequenceDAO, String sequenceName, int sequenceCode, long minValue,
                           long maxValue, int innerStep) {
        this.sequenceDAO = sequenceDAO;
        this.sequenceName = sequenceName;
        this.sequenceCode = sequenceCode;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.innerStep = innerStep;
    }

    /**
     * 取得序列下一个值
     *
     * @return
     * @throws SequenceException
     */
    @Override
    public Event nextEvent() throws SequenceException {

        if (currentRange == null) {
            currentRange = null;
            lock.lock();
            try {
                if (currentRange == null) {
                    long start = System.currentTimeMillis();
                    logger.info("sequence range initializer....");
                    //重新获取range
                    currentRange = sequenceDAO.nextRange(sequenceName);
                    logger.info("sequence range initializer finish....,cost " + (System.currentTimeMillis() - start) + " ms");
                }
            } finally {
                lock.unlock();
            }
        }
        Event event = currentRange.getEvent();
        if (!event.isEffective()) {
            lock.lock();
            try {
                for (; ; ) {
                    if (currentRange.isOver()) {
                        long start = System.currentTimeMillis();
                        logger.info("old sequence range over,apply new range");
                        currentRange = sequenceDAO.nextRange(sequenceName);
                        logger.info("refresh sequence range success,sequence name " + sequenceName + ", now range min " + currentRange.getMin() + "- " + currentRange.getMax() + ",cost " + (System.currentTimeMillis() - start) + " ms");
                    }

                    event = currentRange.getEvent();
                    if (!event.isEffective()) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
        if (event.getRecord() < 0) {
            throw new SequenceException("Sequence value overflow, value = " + event.getRecord());
        }

        return event;
    }

    /**
     * 获取配置的序列最小值
     *
     * @return
     */
    @Override
    public long getMinValue() {
        return this.minValue;
    }

    /**
     * 获取配置的序列最大值
     *
     * @return
     */
    @Override
    public long getMaxValue() {
        return this.maxValue;
    }

    @Override
    public int getCode() {
        return this.sequenceCode;
    }

    /**
     * Getter method for property <tt>sequenceDAO</tt>.
     *
     * @return property value of sequenceDAO
     */
    public DefaultSequenceDAO getSequenceDAO() {
        return sequenceDAO;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceDAO value to be assigned to property sequenceDAO
     */
    public void setSequenceDAO(DefaultSequenceDAO sequenceDAO) {
        this.sequenceDAO = sequenceDAO;
    }

    /**
     * Getter method for property <tt>sequenceName</tt>.
     *
     * @return property value of sequenceName
     */
    public String getSequenceName() {
        return sequenceName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceName value to be assigned to property sequenceName
     */
    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
