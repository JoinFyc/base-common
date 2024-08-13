package com.mengxiang.transaction.framework.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.mengxiang.base.common.log.Logger;
import com.mengxiang.transaction.framework.autoconfig.TransactionTaskProperties;
import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;
import com.mengxiang.transaction.framework.enums.TaskExecuteErrorCodeEnum;
import com.mengxiang.transaction.framework.enums.TaskExecuteStatusEnum;
import com.mengxiang.transaction.framework.enums.TaskRetryStatusEnum;
import com.mengxiang.transaction.framework.enums.TaskRetryStrategyEnum;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.mengxiang.transaction.framework.task.InsurableTask;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * 努力确保型任务异常恢复job
 * 
 * @author JoinFyc
 * @date 2019年6月18日 下午9:40:38
 */
public class InsurableTaskExceptionRecoverJob {

	/**
	 * 最大循环次数， 防止系统数据量较大， 导致无限循环
	 */
	private static final int MAX_LOOP_TIMES = 50;

	private TransactionTaskLogMapper mapper;

	private TransactionTaskProperties taskExecutorProperties;

	public InsurableTaskExceptionRecoverJob(TransactionTaskLogMapper mapper,
			TransactionTaskProperties taskExecutorProperties) {
		this.mapper = mapper;
		this.taskExecutorProperties = taskExecutorProperties;
	}

	@XxlJob(value = "InsurableTaskExceptionRecoverJob")
	public ReturnT<String> execute(String args) {
		Logger.info("努力确保型异常恢复任务开始执行,args={}",args);
		int hour = 1;
		if (!StringUtils.isEmpty(args)) {
			try {
				hour = Integer.parseInt(args);
				// min value 1 hour
				if (hour < 1) {
					hour = 1;
				}
			} catch (Exception e) {
				Logger.warn("一致性任务处理xxljob传入参数异常， 将使用1小时作为时间线处理");
			}
		}

		boolean hasLongtimeProcessingTask = true;
		int loopTimes = 0;
		while (++loopTimes <= MAX_LOOP_TIMES) {
			List<TransactionTaskLogDO> taskList = new ArrayList<>();

			// 先捞长期处理中的任务-》任务状态为处理中且时间超过5分钟
			if (hasLongtimeProcessingTask) {
				List<TransactionTaskLogDO> longTimeTaskList = mapper
						.selectLongtimeProcessingTaskForInsure(taskExecutorProperties.getRetryFetchSize(), hour);
				if (!CollectionUtils.isEmpty(longTimeTaskList)) {
					taskList.addAll(longTimeTaskList);
				}
			}

			// 如果长期处理中的任务是最后一次处理或者无长期处理中的任务，本次将同时处理重试任务
			if (CollectionUtils.isEmpty(taskList) || taskList.size() < taskExecutorProperties.getRetryFetchSize()) {
				hasLongtimeProcessingTask = false;
				List<TransactionTaskLogDO> retryList = mapper
						.selectRetryTaskForInsure(taskExecutorProperties.getRetryFetchSize(), hour);
				if (!CollectionUtils.isEmpty(retryList)) {
					taskList.addAll(retryList);
				}
			}

			if (CollectionUtils.isEmpty(taskList)) {
				return ReturnT.SUCCESS;
			}

			// 待重试任务不为空，执行重试
			for (TransactionTaskLogDO item : taskList) {
				doRecover(item);
			}

			// last page
			if (taskList.size() < taskExecutorProperties.getRetryFetchSize()) {
				return ReturnT.SUCCESS;
			}

		}
		return ReturnT.SUCCESS;

	}

	private void doRecover(TransactionTaskLogDO item) {

		InsurableTask task = null;

		try {
			// 1. 重建任务对象
			task = (InsurableTask) Class.forName(item.getTaskClassName()).newInstance();
			task.rebuild(item);

			// 2. 更新任务状态为处理中
			item.setStatus(TaskExecuteStatusEnum.PROCESSING.name());
			item.setTimes(item.getTimes() + 1);
			mapper.update(item);

			// 3. 执行任务
			TaskExecuteResult result = task.doRecover();

			// 4. 设置执行结果
			item.setStatus(result.getExecuteStatus().name());
			item.setResultAdditionalInfo(result.serialize());
			// 设置重试状态
			if (result.getExecuteStatus().isEndForInsurableTask()) {
				item.setRetryStatus(TaskRetryStatusEnum.RETRY_FINISHED.name());
			} else {
				Logger.error("任务执行异常{}", result);
				item.setErrorCode(result.getErrorCode());
				item.setErrorMessage(StringUtils.left(result.getErrorMessage(), 200));
				if (item.getTimes() + 1 > task.getRetryStrategy().getMaxRetryTimes()) {
					item.setRetryStatus(TaskRetryStatusEnum.RETRY_ULTRALIMIT.name());
				} else {
					item.setRetryStatus(TaskRetryStatusEnum.WAIT_RETRY.name());
					item.setNextExecuteTime(
							new Timestamp(task.getRetryStrategy().calNextExecuteTime(item.getTimes()).getTime()));
				}
			}

			// 5. 持久化任务执行状态
			mapper.update(item);

			// 6. 执行成功回调业务处理，忽略所有异常
			if (result.getExecuteStatus() == TaskExecuteStatusEnum.SUCCESS) {
				try {
					task.callback(result);
				} catch (Throwable e) {
					Logger.error("重试成功后回调业务逻辑执行异常忽略， result={}, task={}", result, item, e);
				}

				// 如果任务执行成功且开启了立即删除日志，则删除任务日志
				try {
					if (taskExecutorProperties.isEnableImmediatelyDelete()) {
						mapper.deleteById(item.getId());
					}
				} catch (Throwable e) {
					Logger.warn("删除任务执行日志异常忽略， result={}, task={}", result, task, e);
				}
			}

		} catch (Throwable e) {
			// 恢复出现系统异常，更新当前任务重试状态和时间，防止出异常的任务不间断重试
			Logger.error("执行异常恢复出现系统异常, task={}", item, e);
			item.setTimes(item.getTimes() + 1);
			item.setErrorCode(TaskExecuteErrorCodeEnum.SYSTEM_ERROR.name());
			item.setErrorMessage(StringUtils.left("异常恢复出现系统异常:" + e.getMessage(), 200));
			item.setStatus(TaskExecuteStatusEnum.EXCEPTION.name());
			// 如果因为异常原因，task未重建成功，则取默认的重试策略
			TaskRetryStrategyEnum retryStrategy = (task == null) ? TaskRetryStrategyEnum.INCREASING_INTERVAL
					: task.getRetryStrategy();
			if (item.getTimes() + 1 > retryStrategy.getMaxRetryTimes()) {
				item.setRetryStatus(TaskRetryStatusEnum.RETRY_ULTRALIMIT.name());
			} else {
				item.setRetryStatus(TaskRetryStatusEnum.WAIT_RETRY.name());
				item.setNextExecuteTime(new Timestamp(retryStrategy.calNextExecuteTime(item.getTimes()).getTime()));
			}
			mapper.update(item);
		}

	}

}
