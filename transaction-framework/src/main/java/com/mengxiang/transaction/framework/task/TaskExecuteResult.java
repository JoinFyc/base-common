package com.mengxiang.transaction.framework.task;

import com.mengxiang.transaction.framework.commons.Printable;
import com.mengxiang.transaction.framework.enums.TaskExecuteStatusEnum;

/**
 * 任务执行结果，业务可扩展定义个性化的Result对象
 * 
 * @author JoinFyc
 * @date 2019年6月18日 下午5:42:42
 */
public class TaskExecuteResult extends Printable {

	private static final long serialVersionUID = 7219686598944917201L;

	protected TaskExecuteStatusEnum executeStatus;

	protected String errorCode;

	protected String errorMessage;

	/**
	 * 序列化
	 * 
	 * @return
	 */
	public String serialize() {
		return null;
	}

	public TaskExecuteStatusEnum getExecuteStatus() {
		return executeStatus;
	}

	public void setExecuteStatus(TaskExecuteStatusEnum executeStatus) {
		this.executeStatus = executeStatus;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
