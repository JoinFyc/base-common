package com.mengxiang.transaction.framework.executor.impl;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mengxiang.base.common.log.Logger;
import com.mengxiang.transaction.framework.autoconfig.TransactionTaskProperties;
import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;
import com.mengxiang.transaction.framework.enums.TaskExecuteErrorCodeEnum;
import com.mengxiang.transaction.framework.enums.TaskExecuteStatusEnum;
import com.mengxiang.transaction.framework.enums.TaskRetryStatusEnum;
import com.mengxiang.transaction.framework.executor.InsurableTaskExecutor;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.mengxiang.transaction.framework.task.InsurableTask;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;

/**
 * 努力确保型任务执行器实现
 * 
 * @author JoinFyc
 * @Date 2020年11月4日
 *
 */
public class InsurableTaskExecutorImpl implements InsurableTaskExecutor {

	private TransactionTaskManager transactionManager;
	private ThreadPoolTaskExecutor workExecutor;
	private TransactionTaskLogMapper taskMapper;
	private ApplicationEventPublisher eventPublisher;
	private TransactionTaskProperties transactionTaskProperties;

	public InsurableTaskExecutorImpl(TransactionTaskManager transactionManager, TransactionTaskLogMapper taskMapper,
			ThreadPoolTaskExecutor workExecutor, ApplicationEventPublisher eventPublisher,
			TransactionTaskProperties transactionTaskProperties) {
		this.transactionManager = transactionManager;
		this.workExecutor = workExecutor;
		this.taskMapper = taskMapper;
		this.eventPublisher = eventPublisher;
		this.transactionTaskProperties = transactionTaskProperties;
	}

	@Override
	public <T extends TaskExecuteResult> void execute(InsurableTask<T> task) {
		// 1. 持久化任务
		TransactionTaskLogDO taskDo = transactionManager.persistTask(task);

		try {
			// 如果任务被包装到本地事务中，则在事务提交后再执行远程任务，否则立即执行
			if (TransactionSynchronizationManager.isActualTransactionActive()) {
				eventPublisher.publishEvent(new TaskPersistEvent(new TaskHolder(taskDo, task)));
			} else {
				this.doAsyncExecute(task, taskDo);
			}
		} catch (Throwable e) {
			Logger.warn("任务执行异常，忽略，系统后面将自动发起重试: taskId={}, taskType={}", task.getTaskId(), task.getTaskType(), e);
		}
	}

	@Override
	public <T extends TaskExecuteResult> T syncExecute(InsurableTask<T> task) {
		// 1. 持久化任务
		TransactionTaskLogDO taskDo = transactionManager.persistTask(task);

		// 执行任务
		return this.doSyncExecute(task, taskDo);

	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true)
	private void listenTransactionCommited(TaskPersistEvent taskPersistEvent) {
		TaskHolder source = (TaskHolder) taskPersistEvent.getSource();
		try {
			this.doAsyncExecute(source.getTask(), source.getTaskDo());
		} catch (Throwable e) {
			Logger.warn("任务执行异常忽略，系统后面将自动发起重试: taskId={}, taskType={}", source.getTask().getTaskId(), source.getTask().getTaskType(), e);
		}
	}

	private <T extends TaskExecuteResult> void doAsyncExecute(InsurableTask<T> task, TransactionTaskLogDO taskDo) {
		workExecutor.execute(RunnableWrapper.of(new Runnable() {
			@Override
			public void run() {
				doSyncExecute(task, taskDo);
			}
		}));

	}

	@SuppressWarnings("unchecked")
	private <T extends TaskExecuteResult> T doSyncExecute(InsurableTask<T> task, TransactionTaskLogDO taskDo) {
		T result = null;

		// 2.1 执行任务
		try {
			result = task.doExecute();
		} catch (Throwable e) {
			Logger.error("任务执行异常{}", task, e);
			result = (T) new TaskExecuteResult();
			result.setExecuteStatus(TaskExecuteStatusEnum.EXCEPTION);
			result.setErrorCode(TaskExecuteErrorCodeEnum.SYSTEM_ERROR.name());
			result.setErrorMessage(StringUtils.left(e.getMessage(), 200));
		}

		// 2.2 持久化执行结果
		try {
			taskDo.setStatus(result.getExecuteStatus().name());
			taskDo.setResultAdditionalInfo(result.serialize());
			if (!result.getExecuteStatus().isEndForInsurableTask()) {
				// 业务状态异常,（非抛出异常）重试次数超过错误配置次数，才开始处理错误日志
				if (taskDo.getTimes() >= transactionTaskProperties.getErrorLogBeginTimes()) {
					Logger.error("任务执行业务状态异常，当前重试次数{},  {}", taskDo.getTimes(), result);
				}

				taskDo.setErrorCode(result.getErrorCode());
				taskDo.setErrorMessage(StringUtils.left(result.getErrorMessage(), 200));
				taskDo.setRetryStatus(TaskRetryStatusEnum.WAIT_RETRY.name());
				taskDo.setNextExecuteTime(
						new Timestamp(task.getRetryStrategy().calNextExecuteTime(taskDo.getTimes()).getTime()));
			}

			taskMapper.update(taskDo);
		} catch (Throwable e) {
			Logger.error("持久化任务执行状态异常task={}, result={}", task, result, e);
			result = (T) new TaskExecuteResult();
			result.setExecuteStatus(TaskExecuteStatusEnum.EXCEPTION);
			result.setErrorCode(TaskExecuteErrorCodeEnum.SYSTEM_ERROR.name());
			result.setErrorMessage(StringUtils.left(e.getMessage(), 200));
			return result;
		}

		// 2.3 如果任务执行成功执行回调和清理工作
		if (result.getExecuteStatus() == TaskExecuteStatusEnum.SUCCESS) {
			try {
				task.callback(result);
			} catch (Throwable e) {
				// 此处报错后忽略，回调异常暂不支持重试
				Logger.error("重试成功后回调业务执行异常， result={}, task={}", result, task, e);
			}
			
			// 如果任务执行成功且开启了立即删除日志，则删除任务日志
			try {
				if (transactionTaskProperties.isEnableImmediatelyDelete()) {
					taskMapper.deleteById(taskDo.getId());
				}
			} catch (Throwable e) {
				Logger.warn("删除任务执行日志异常忽略， result={}, task={}", result, task, e);
			}
		}



		return result;
	}

	private static final class TaskPersistEvent extends ApplicationEvent {
		private static final long serialVersionUID = 4636423716577502766L;

		private TaskPersistEvent(TaskHolder holder) {
			super(holder);
		}
	}

	private static final class TaskHolder {
		private TransactionTaskLogDO taskDo;
		private InsurableTask task;

		private TaskHolder(TransactionTaskLogDO taskDo, InsurableTask task) {
			super();
			this.taskDo = taskDo;
			this.task = task;
		}

		private TransactionTaskLogDO getTaskDo() {
			return taskDo;
		}

		private InsurableTask getTask() {
			return task;
		}
	}

}
