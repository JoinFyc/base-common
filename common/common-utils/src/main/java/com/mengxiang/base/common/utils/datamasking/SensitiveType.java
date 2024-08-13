package com.mengxiang.base.common.utils.datamasking;

import com.mengxiang.base.common.utils.datamasking.strategy.EmailMask;
import com.mengxiang.base.common.utils.datamasking.strategy.HashMask;
import com.mengxiang.base.common.utils.datamasking.strategy.NameMask;
import com.mengxiang.base.common.utils.datamasking.strategy.PartMask;

/**
 * 脱敏类型
 */
public enum SensitiveType {
    //中文名
    Name(new NameMask()),
    //电话
    Phone(new PartMask(), 3),
    //身份证号
    IDCard(new PartMask(), 5, 2),
    //银行卡号
    BankCard(new PartMask(), 4, 2),
    //地址
    Address(new PartMask(), 9, 0),
    //电子邮件
    Email(new EmailMask()),
    //验证码
    Captcha(new PartMask(), 1),
    //护照/军官证
    Passport(new PartMask(), 2),
    //账号
    Account(new PartMask(), 1),
    //密码
    Password(new PartMask(), 0),
    /**
     * 散列，这种掩码方式，用户可以手工计算Hash值来精确查询日志。
     */
    Hash(new HashMask()),
    //缺省,只显示第一个字符串
    Default(new PartMask(), 1, 0);

    private MaskStrategy strategy;
    private int[] params;

    SensitiveType(MaskStrategy strategy, int... params) {
        this.strategy = strategy;
        this.params = params;
    }

    public MaskStrategy getStrategy() {
        return strategy;
    }


    public int[] getParams() {
        return params;
    }
}
