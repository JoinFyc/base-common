package com.mengxiang.transaction.framework.enums;

public enum TaskExecuteErrorCodeEnum {

	PERSIST_FAILED("任务持久化失败"), 
	
	REPEATED_REQUEST("重复的请求"),

	SYSTEM_ERROR("系统异常"), 
	
	SUCCESS("操作成功");

	private String desc;

	private TaskExecuteErrorCodeEnum(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
