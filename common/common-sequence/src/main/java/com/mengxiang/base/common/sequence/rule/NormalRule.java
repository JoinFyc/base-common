package com.mengxiang.base.common.sequence.rule;

import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;

/**
 * 22位标准sequence
 *
 * @author JoinFyc
 * @date 2021/2/3 9:47
 */
public class NormalRule extends AbstractSequenceRule {
    private RuleEnum ruleEnum = RuleEnum.NORMAL;

    @Override
    public RuleEnum getRuleEnum() {
        return ruleEnum;
    }

    @Override
    public boolean checkSequence(String sequence) {
        boolean standard = sequence.length() == SequenceConstant.FULL_SEQUENCE_LENGTH;
        if (!standard) {
            throw new SequenceException("this sequence is not standard:" + sequence);
        }
        return Boolean.TRUE;
    }

    @Override
    public void init(int sequenceLength) {

    }
}
