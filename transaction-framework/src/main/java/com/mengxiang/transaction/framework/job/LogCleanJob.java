package com.mengxiang.transaction.framework.job;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import com.mengxiang.base.common.log.Logger;
import com.mengxiang.transaction.framework.autoconfig.TransactionTaskProperties;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * 日志清除任务
 * 
 * @author JoinFyc
 * @date 2020年3月28日 下午3:59:59
 */
@Component
public class LogCleanJob  {

	private TransactionTaskLogMapper mapper;

	private TransactionTaskProperties taskExecutorProperties;
	
	/**
	 * 定时清楚任务最大循环执行次数，防止数据量大导致的数据库繁忙
	 */
	private int MAX_LOOP_TIMES = 100;

	public LogCleanJob(TransactionTaskLogMapper mapper, TransactionTaskProperties taskExecutorProperties) {
		this.mapper = mapper;
		this.taskExecutorProperties = taskExecutorProperties;
	}
	
    @XxlJob(value = "LogCleanJob" )
	public ReturnT<String>  execute(String arg) {
    	if(!taskExecutorProperties.isEnableExpireDelete())
    	{
    		Logger.info("未启用过期自动删除，不执行自动清理");
    		return ReturnT.SUCCESS;
    	}
		try {
			 
			// 计算时间
			Timestamp expireTime = new Timestamp(
					DateUtils.addDays(new Date(), -taskExecutorProperties.getTaskExpireTime()).getTime());
			List<String> taskStatus = StringUtils.isBlank(taskExecutorProperties.getDeleteTaskStatus()) ? null
					: Arrays.asList(taskExecutorProperties.getDeleteTaskStatus().split(","));

			int count = 0;
			int loopTimes =0; 
			do{
				count = count+ mapper.delete(expireTime,taskStatus);
				loopTimes++;
			}while(count>0 && loopTimes< MAX_LOOP_TIMES);

			Logger.info("task表定时清理任务执行成功，共计删除" + count + "条数据");
			
		} catch (Exception e) {
			Logger.error("定时删除任务job异常", e);
		}
		return ReturnT.SUCCESS;
	}

}
