package com.mengxiang.base.common.sequence.rule;

/**
 * @author JoinFyc
 * @date 2021/2/3 9:54
 */
public class LessBusinessCodeRule extends AbstractSequenceRule {
    private final RuleEnum ruleEnum = RuleEnum.LESS_BUSINESS_CODE;
    private int sequenceLength;
    private int fullSequenceLength;

    @Override
    public RuleEnum getRuleEnum() {
        return ruleEnum;
    }

    @Override
    public boolean checkSequence(String sequence) {
        return sequence.length() == fullSequenceLength;
    }

    @Override
    public void init(int sequenceLength) {
        this.sequenceLength = sequenceLength;
        this.fullSequenceLength = ruleEnum.getDateLength() + ruleEnum.getSequenceLength() + 1 + ruleEnum.getBigBusinessLength() + ruleEnum.getSmallBusinessLength() + ruleEnum.getShardingCodeLength() + ruleEnum.getFailoverLength();
    }

    @Override
    public int getSequenceLength() {
        return sequenceLength;
    }

    @Override
    protected int getFullSequenceLength() {
        return fullSequenceLength;
    }
}
