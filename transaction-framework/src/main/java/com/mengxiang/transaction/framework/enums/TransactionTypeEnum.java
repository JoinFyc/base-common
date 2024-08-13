package com.mengxiang.transaction.framework.enums;

/**
 * 事务类型枚举
 * @author JoinFyc
 * @Date 2020年11月4日
 *
 */
public enum TransactionTypeEnum {

	REVERSAL("异常冲正型"),
	
	INSURE("努力确保型");
	
    private String desc;
    
    private TransactionTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
