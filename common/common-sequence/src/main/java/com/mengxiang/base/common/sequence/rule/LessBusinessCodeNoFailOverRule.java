package com.mengxiang.base.common.sequence.rule;

/**
 * @author JoinFyc
 * @date 2021/2/24
 **/
public class LessBusinessCodeNoFailOverRule extends AbstractSequenceRule {
    private final RuleEnum ruleEnum = RuleEnum.LESS_BUSINESS_CODE_NO_FAIL_OVER;
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

    @Override
    public String resolveFailover(String sequence) {
        checkSequence(sequence);
        return "X";
    }
}
