package com.mengxiang.base.common.sequence.rule;

import com.mengxiang.base.common.sequence.constant.SequenceConstant;

/**
 * @author JoinFyc
 * @date 2021/2/3 9:50
 */
public enum RuleEnum {
    //6+8+1+2+2+2+1
    NORMAL(0, "business占4位", SequenceConstant.DEFAULT_DATE_LENGTH, SequenceConstant.DEFAULT_SEQUENCE_LENGTH, SequenceConstant.DEFAULT_BIG_BUSINESS_LENGTH, SequenceConstant.DEFAULT_SMALL_BUSINESS_LENGTH, SequenceConstant.DEFAULT_SHARD_LENGTH, 1),
    //6+X+1+1+1+2+1
    LESS_BUSINESS_CODE(1, "business只占2位", SequenceConstant.DEFAULT_DATE_LENGTH, -1, SequenceConstant.DEFAULT_BIG_BUSINESS_LENGTH, 0, SequenceConstant.DEFAULT_SHARD_LENGTH, 1),
    //6+X+1+1+2+0
    LESS_BUSINESS_CODE_NO_FAIL_OVER(2, "business只占2位", SequenceConstant.DEFAULT_DATE_LENGTH, -1, SequenceConstant.DEFAULT_BIG_BUSINESS_LENGTH, 0, SequenceConstant.DEFAULT_SHARD_LENGTH, 0),
    SUB_DS_NO_BUSINESS_CODE(3, "有分库键", SequenceConstant.DEFAULT_DATE_LENGTH, -1, SequenceConstant.DEFAULT_NO_BIG_BUSINESS_LENGTH, 2, 5, 0);

    private int version;
    private String desc;
    private int dateLength;
    private int sequenceLength;
    private int bigBusinessLength;
    private int smallBusinessLength;
    private int shardingCodeLength;
    private int failoverLength;

    RuleEnum(int version, String desc, int dateLength, int sequenceLength, int bigBusinessLength, int smallBusinessLength, int shardingCodeLength, int failoverLength) {
        this.version = version;
        this.desc = desc;
        this.dateLength = dateLength;
        this.sequenceLength = sequenceLength;
        this.bigBusinessLength = bigBusinessLength;
        this.smallBusinessLength = smallBusinessLength;
        this.shardingCodeLength = shardingCodeLength;
        this.failoverLength = failoverLength;
    }

    public static RuleEnum getByVersion(int model) {
        RuleEnum[] enums = values();
        for (RuleEnum value : enums) {
            if (value.getVersion() == model) {
                return value;
            }
        }
        return RuleEnum.NORMAL;
    }

    public int getVersion() {
        return version;
    }

    public String getDesc() {
        return desc;
    }

    public int getDateLength() {
        return dateLength;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    public int getBigBusinessLength() {
        return bigBusinessLength;
    }

    public int getSmallBusinessLength() {
        return smallBusinessLength;
    }

    public int getShardingCodeLength() {
        return shardingCodeLength;
    }

    public int getFailoverLength() {
        return failoverLength;
    }
}
