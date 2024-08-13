package com.mengxiang.base.common.sequence.rule;

/**
 * @author JoinFyc
 * @date 2021/2/3 9:38
 */
public class RuleFactory {
    public static Rule getRule(int model) {
        RuleEnum ruleEnum = RuleEnum.getByVersion(model);
        switch (ruleEnum) {
            case LESS_BUSINESS_CODE:
                return new LessBusinessCodeRule();
            case LESS_BUSINESS_CODE_NO_FAIL_OVER:
                return new LessBusinessCodeNoFailOverRule();
            case SUB_DS_NO_BUSINESS_CODE:
                return new SubDsNoBusinessCodeRule();
            default:
                return new NormalRule();
        }
    }
}
