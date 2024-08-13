package com.mengxiang.transaction.framework.executor.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import com.mengxiang.base.common.log.Logger;
import com.mengxiang.transaction.framework.autoconfig.TransactionTaskProperties;
import com.mengxiang.transaction.framework.commons.TransactionFrameworkException;
import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;
import com.mengxiang.transaction.framework.enums.TaskExecuteErrorCodeEnum;
import com.mengxiang.transaction.framework.enums.TaskExecuteStatusEnum;
import com.mengxiang.transaction.framework.enums.TaskRetryStatusEnum;
import com.mengxiang.transaction.framework.enums.TaskRetryStrategyEnum;
import com.mengxiang.transaction.framework.enums.TaskReversalStatusEnum;
import com.mengxiang.transaction.framework.enums.TransactionStatusEnum;
import com.mengxiang.transaction.framework.executor.ReversibleTaskExecutor;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.mengxiang.transaction.framework.task.ReversibleTask;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;

/**
 * 异常冲正型任务执行器
 * 
 * @author JoinFyc
 * @Date 2020年11月4日
 *
 */
public class ReversibleTaskExecutorImpl implements ReversibleTaskExecutor {

	private static final ThreadLocal<TransactionContext> context = new ThreadLocal<TransactionContext>();

	private TransactionTaskManager transactionManager;
	private ThreadPoolTaskExecutor sendExecutor;
	private TransactionTaskLogMapper taskMapper;
	private TransactionTaskProperties transactionTaskProperties;

	public ReversibleTaskExecutorImpl(TransactionTaskManager transactionManager, TransactionTaskLogMapper taskMapper,
			ThreadPoolTaskExecutor sendExecutor, TransactionTaskProperties transactionTaskProperties) {
		this.transactionManager = transactionManager;
		this.sendExecutor = sendExecutor;
		this.taskMapper = taskMapper;
		this.transactionTaskProperties = transactionTaskProperties;
	}

	/**
	 * 获取事务上下文，事务上下文保存到当前线程局部变量中
	 * 
	 * @return
	 */
	public TransactionContext getTransactionContext() {
		TransactionContext result = context.get();
		if (result == null) {
			result = new TransactionContext();
			context.set(result);
		}
		return result;
	}

	/**
	 * 执行异常冲正型任务
	 * 
	 * @param task task
	 * @return status
	 */
	@Override
	public <T extends TaskExecuteResult> T execute(ReversibleTask<T> task) {
		// 1. 持久化任务
		TransactionTaskLogDO taskDo = transactionManager.persistTaskForNewTransaction(task);

		// 2. 添加任务到事务上下文中
		this.getTransactionContext().addTransactionTask((ReversibleTask) task, taskDo);

		// 3. 执行任务
		T result = null;
		try {
			// 调用task执行任务
			result = task.doExecute();
			taskDo.setStatus(result.getExecuteStatus().name());
			taskDo.setResultAdditionalInfo(result.serialize());
			taskDo.setErrorMessage(StringUtils.left(result.getErrorMessage(), 200));
			taskDo.setErrorCode(result.getErrorCode());
		} catch (Throwable e) {
			Logger.error("远程任务执行异常{}", task, e);
			taskDo.setStatus(TaskExecuteStatusEnum.EXCEPTION.name());
			taskDo.setErrorCode(TaskExecuteErrorCodeEnum.SYSTEM_ERROR.name());
			taskDo.setErrorMessage(StringUtils.left(e.getMessage(), 200));
		}

		// 3 持久化执行结果
		transactionManager.updateTransactionTaskLog(taskDo);

		return result;
	}

	/**
	 * 提交事务
	 */
	public void doFinally() {
		TransactionContext transContext = context.get();

		try {

			sendExecutor.execute(RunnableWrapper.of(new Runnable() {
				@Override
				public void run() {
					doFinally(transContext);
				}
			}));
		} catch (Throwable e) {
			Logger.warn("提交异常冲正型任务异常，后续系统将会发起自动重试, transactionContext={}", transContext, e);
		} finally {
			context.remove();
		}
	}

	/**
	 * 设置事务状态
	 * 
	 * @param status
	 */
	@Override
	public void setTransactionStatus(TransactionStatusEnum status) {
		if (context.get() == null || CollectionUtils.isEmpty(context.get().getTransactionTaskList())) {
			Logger.warn("事务状态设置无效，当前上下文不存在异常冲正型事务任务，忽略！");
			return;
		}
		context.get().setTransactionStatus(status);
	}

	private void doFinally(TransactionContext tranContext) {
		// 事务上下文未初始化，可能为未执行过异常冲正型任务，忽略
		if (Objects.isNull(tranContext)) {
			return;
		}

		// 无异常冲正型任务，忽略
		List<TransactionTaskLogDO> transactionLogDOList = tranContext.getTransactionLogDOList();
		if (CollectionUtils.isEmpty(transactionLogDOList)) {
			return;
		}

		// 事务状态为提交，更新日志状态为已提交
		if (tranContext.getTransactionStatus() == TransactionStatusEnum.COMMIT) {

			// 获取所有执行成功的异常冲正型任务
			List<Long> successTaskList = transactionLogDOList.stream()
					.filter(item -> StringUtils.equals(TaskExecuteStatusEnum.SUCCESS.name(), item.getStatus()))
					.map(TransactionTaskLogDO::getId).collect(Collectors.toList());
			
			// 如果没有执行成功的异常冲正型任务，忽略. 默认情况下事务状态为Commit, 所以可能存在这种情况，
			if (CollectionUtils.isEmpty(successTaskList)) {
				return;
			}

			// 如果不是所有任务都已执行成功，不应设置事务状态为已提交，检测到此情况后报错
			if (transactionLogDOList.size() != successTaskList.size()) {
				Logger.error("[重要]提交事务异常，请确认所有事务任务是否都为执行成功状态，总任务数量:{},执行成功数量:{}， transactionContext={}",
						transactionLogDOList.size(), successTaskList.size(), tranContext);
				throw new TransactionFrameworkException(TaskExecuteErrorCodeEnum.SYSTEM_ERROR, "提交事务异常");
			}

			taskMapper.batchUpdateStatus(successTaskList, TaskExecuteStatusEnum.COMMITED.name(),
					TaskExecuteStatusEnum.SUCCESS.name());
			
			if (transactionTaskProperties.isEnableImmediatelyDelete()) {
				// 删除已提交的任务
				taskMapper.batchDelete(successTaskList);
			}
			return;
		}

		// 否则事务状态为冲正， 执行冲正
		for (ReversibleTask task : tranContext.getTransactionTaskList()) {
			try {
				TransactionTaskLogDO taskDo = transactionLogDOList.stream()
						.filter(item -> StringUtils.equals(item.getTaskId(), task.getTaskId())
								&& StringUtils.equals(item.getTaskType(), task.getTaskType()))
						.findFirst().get();
				// 如果任务执行状态为失败直接忽略
				if (StringUtils.equals(taskDo.getStatus(), TaskExecuteStatusEnum.FAILED.name())) {
					return;
				}

				doReversal(task, taskDo);
			} catch (Throwable e) {
				Logger.warn("执行冲正异常,系统稍后将发起重试，task={}", task, e);
			}
		}

	}

	private void doReversal(ReversibleTask task, TransactionTaskLogDO taskDo) {
		TaskExecuteResult result = null;
		try {
			taskDo.setReversalStatus(TaskReversalStatusEnum.REVERSALING.name());
			taskMapper.updateResvalStatus(taskDo);

			// 2.1 调用task执行任务
			result = task.doReversal();

			// 解析任务结果
			TaskReversalStatusEnum status = TaskReversalStatusEnum.convert(result.getExecuteStatus());
			taskDo.setReversalStatus(status.name());
			taskDo.setResultAdditionalInfo(result.serialize());
			taskDo.setErrorCode(result.getErrorCode());
			taskDo.setErrorMessage(StringUtils.left(result.getErrorMessage(), 200));

			if (status == TaskReversalStatusEnum.REVERSAL_EXCEPTION) {
				Logger.error("冲正执行异常{}", result);
				taskDo.setRetryStatus(TaskRetryStatusEnum.WAIT_RETRY.name());
				taskDo.setNextExecuteTime(
						new Timestamp(task.getRetryStrategy().calNextExecuteTime(taskDo.getTimes()).getTime()));
			}

		} catch (Throwable e) {
			Logger.error("远程任务执行异常{}", task, e);
			taskDo.setReversalStatus(TaskReversalStatusEnum.REVERSAL_EXCEPTION.name());
			taskDo.setErrorCode(TaskExecuteErrorCodeEnum.SYSTEM_ERROR.name());
			taskDo.setErrorMessage(StringUtils.left(e.getMessage(), 200));
			taskDo.setRetryStatus(TaskRetryStatusEnum.WAIT_RETRY.name());
			taskDo.setNextExecuteTime(
					new Timestamp(task.getRetryStrategy().calNextExecuteTime(taskDo.getTimes()).getTime()));
		}

		// 2.2 持久化执行结果
		taskMapper.updateResvalStatus(taskDo);
	}

}
