package com.mengxiang.base.common.sequence.rule;

import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.utils.StringUtils;

/**
 * @author JoinFyc
 * @date 2021/2/3 14:12
 */
public abstract class AbstractSequenceRule implements Rule {

    @Override
    public String getSequence(String date, Long sequence, int businessCode, int shardCode, int failover) {
        RuleEnum ruleEnum = getRuleEnum();
        int sequenceLength = 8;
        if (ruleEnum.getSequenceLength() < 0) {
            sequenceLength = getSequenceLength();
        }
        StringBuilder sb = new StringBuilder(getFullSequenceLength());
        //6位
        sb.append(date);
        //8位
        sb.append(StringUtils.alignRight(sequence + "", sequenceLength, "0"));
        //1位
        sb.append(ruleEnum.getVersion());

        int businessSize = ruleEnum.getBigBusinessLength() + ruleEnum.getSmallBusinessLength();
        if (businessSize > 0) {
            //4位
            sb.append(StringUtils.alignRight(businessCode + "", businessSize, "0"));
        }
        //2位
        sb.append(StringUtils.alignRight(shardCode + "", ruleEnum.getShardingCodeLength(), "0"));
        //1位
        if (ruleEnum.getFailoverLength() > 0) {
            sb.append(StringUtils.alignRight(failover + "", ruleEnum.getFailoverLength(), "0"));
        }
        return sb.toString();
    }

    /**
     * 获取该sequence的产生日期
     *
     * @param sequence
     * @return
     */
    @Override
    public String resolveCurrentDate(String sequence) {
        checkSequence(sequence);
        return "20" + sequence.substring(0, getRuleEnum().getDateLength());
    }

    @Override
    public String resolveSequenceIndex(String sequence) {
        checkSequence(sequence);
        return sequence.substring(getRuleEnum().getDateLength(), getRuleEnum().getDateLength() + getSequenceLength());
    }

    /**
     * 获取该sequence的version
     *
     * @param sequence
     * @return
     */
    @Override
    public String resolveVersion(String sequence) {
        checkSequence(sequence);
        return sequence.substring(getRuleEnum().getDateLength() + getSequenceLength(), getRuleEnum().getDateLength() + getSequenceLength() + 1);
    }


    /**
     * 获取business code
     *
     * @param sequence
     * @return
     */
    @Override
    public String resolveBusinessCode(String sequence) {
        checkSequence(sequence);
        return sequence.substring(getRuleEnum().getDateLength() + getSequenceLength() + 1, getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength());
    }

    /**
     * 获取该sequence的sharding code
     *
     * @param sequence
     * @return
     */
    @Override
    public String resolveShardingCode(String sequence) {
        checkSequence(sequence);
        return sequence.substring(getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength(), getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength() + getRuleEnum().getShardingCodeLength());
    }

    @Override
    public String resolveShardingDs(String sequence) {
        return "0";
    }

    /**
     * 获取该sequence的failover
     *
     * @param sequence
     * @return
     */
    @Override
    public String resolveFailover(String sequence) {
        checkSequence(sequence);
        return sequence.substring(getFullSequenceLength() - 1);
    }


    protected int getSequenceLength() {
        return SequenceConstant.DEFAULT_SEQUENCE_LENGTH;
    }

    protected int getFullSequenceLength() {
        return SequenceConstant.FULL_SEQUENCE_LENGTH;
    }
}
