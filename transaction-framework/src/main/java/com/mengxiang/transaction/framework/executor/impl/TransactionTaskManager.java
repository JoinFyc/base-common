package com.mengxiang.transaction.framework.executor.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mengxiang.base.common.log.Logger;
import com.mengxiang.transaction.framework.commons.TransactionFrameworkException;
import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;
import com.mengxiang.transaction.framework.enums.TaskExecuteErrorCodeEnum;
import com.mengxiang.transaction.framework.enums.TaskExecuteStatusEnum;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;
import com.mengxiang.transaction.framework.task.TransactionTask;

/**
 * 
 * @author JoinFyc
 * @Date 2020年11月10日
 *
 */
public class TransactionTaskManager {
	
	
	public TransactionTaskLogMapper enterpriseTaskMapper;

	public TransactionTaskManager(TransactionTaskLogMapper enterpriseTaskMapper) {
		this.enterpriseTaskMapper = enterpriseTaskMapper;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateTransactionTaskLog(TransactionTaskLogDO taskDo)
	{
		// 3 持久化执行结果
		enterpriseTaskMapper.update(taskDo);
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T extends TaskExecuteResult> TransactionTaskLogDO persistTaskForNewTransaction(TransactionTask<T> task) {
		return this.persistTask(task);
	}
	
	
	/**
	 * @param <T>
	 * @param task
	 * @return
	 */
	public <T extends TaskExecuteResult> TransactionTaskLogDO persistTask(TransactionTask<T> task) {
		TransactionTaskLogDO logDo = new TransactionTaskLogDO();
		logDo.setTaskId(task.getTaskId());
		logDo.setTaskType(task.getTaskType());
		logDo.setTaskClassName(task.getClass().getTypeName());
		logDo.setStatus(TaskExecuteStatusEnum.PROCESSING.name());
		logDo.setTransactionType(task.getTransactionType().name());
		logDo.setTimes(1);
		logDo.setRequestAdditionalInfo(task.serializeAdditionalInfo());

		try {
			enterpriseTaskMapper.save(logDo);
		} catch (DuplicateKeyException e) {
			Logger.error("任务已存在，请不要重复提交={}", logDo);
			throw new TransactionFrameworkException(TaskExecuteErrorCodeEnum.REPEATED_REQUEST, "任务重复，请勿重复提交");
		} catch (Exception e) {
			Logger.error("任务持久化失败={}", logDo, e);
			throw new TransactionFrameworkException(TaskExecuteErrorCodeEnum.PERSIST_FAILED,
					TaskExecuteErrorCodeEnum.PERSIST_FAILED.getDesc());
		}
		return logDo;
	}
	
}
