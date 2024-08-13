package com.mengxiang.transaction.framework.executor;

import com.mengxiang.transaction.framework.task.InsurableTask;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;

/**
 * 努力确保型任务执行器
 * @author JoinFyc
 * @Date 2020年12月25日
 *
 */
public interface InsurableTaskExecutor {

	/**
	 * 执行努力确保型任务
	 * 
	 * @param <T>
	 * @param task
	 * @return
	 */
	public <T extends TaskExecuteResult> void execute(InsurableTask<T> task);
	

	/**
	 * 同步执行努力确保型任务
	 * 
	 * @param <T>
	 * @param task
	 * @return
	 */
	public <T extends TaskExecuteResult> T syncExecute(InsurableTask<T> task);
	

	
}
