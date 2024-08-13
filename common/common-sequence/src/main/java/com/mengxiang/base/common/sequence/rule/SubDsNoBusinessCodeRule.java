package com.mengxiang.base.common.sequence.rule;

public class SubDsNoBusinessCodeRule extends AbstractSequenceRule {
    private RuleEnum ruleEnum = RuleEnum.SUB_DS_NO_BUSINESS_CODE;
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
    public int getSequenceLength() {
        return sequenceLength;
    }

    @Override
    public void init(int sequenceLength) {
        this.sequenceLength = sequenceLength;
        this.fullSequenceLength = ruleEnum.getDateLength() + ruleEnum.getSequenceLength() + 1 + ruleEnum.getBigBusinessLength() + ruleEnum.getSmallBusinessLength() + ruleEnum.getShardingCodeLength() + ruleEnum.getFailoverLength();
    }

    @Override
    public String resolveShardingCode(String sequence) {
        checkSequence(sequence);
        String substring = sequence.substring(getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength(), getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength() + getRuleEnum().getShardingCodeLength());
        return substring.substring(2);
    }

    @Override
    public String resolveShardingDs(String sequence) {
        checkSequence(sequence);
        String substring = sequence.substring(getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength(), getRuleEnum().getDateLength() + getSequenceLength() + 1 + getRuleEnum().getBigBusinessLength() + getRuleEnum().getSmallBusinessLength() + getRuleEnum().getShardingCodeLength());
        return substring.substring(0, 2);
    }
}
