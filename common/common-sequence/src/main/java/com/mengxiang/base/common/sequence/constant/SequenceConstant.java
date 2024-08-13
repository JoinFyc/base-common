package com.mengxiang.base.common.sequence.constant;

/**
 * @author JoinFyc
 * @date 2020/12/10 16:10
 */
public class SequenceConstant {
    public static final String DEFAULT_TABLE_NAME = "worker_sequence";
    public static final String SEQUENCE_LOG_NAME = "dal-sequence";
    public static final Integer DEFAULT_STEP = 1000;
    public static final Integer ONE_HUNDRED = 100;
    public static final Integer ONE_THOUSAND = 1000;
    public static final Long DEFAULT_MIN = 1L;
    public static final Long DEFAULT_MAX = 100000000L;
    public static final Integer FULL_SEQUENCE_LENGTH = 22;

    public static final Integer DEFAULT_SEQUENCE_LENGTH = 8;
    public static final Integer DEFAULT_DATE_LENGTH = 6;
    public static final Integer DEFAULT_BIG_BUSINESS_LENGTH = 2;
    public static final Integer DEFAULT_NO_BIG_BUSINESS_LENGTH = 0;
    /**
     * 2位的业务id
     */
    public static final Integer DEFAULT_SMALL_BUSINESS_LENGTH = 2;
    /**
     * 默认sharding 位数
     */
    public static final Integer DEFAULT_SHARD_LENGTH = 2;
}
