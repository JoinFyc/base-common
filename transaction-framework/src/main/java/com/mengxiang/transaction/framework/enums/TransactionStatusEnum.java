package com.mengxiang.transaction.framework.enums;

/**
 * 事务状态
 * 
 * @author JoinFyc
 * @Date 2020年11月9日
 *
 */
public enum TransactionStatusEnum {

	COMMIT("提交"),

	ROLLBACK("回滚");

	private String desc;

	private TransactionStatusEnum(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
