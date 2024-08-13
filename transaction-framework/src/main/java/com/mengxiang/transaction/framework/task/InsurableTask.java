package com.mengxiang.transaction.framework.task;

import com.mengxiang.transaction.framework.enums.TaskRetryStrategyEnum;
import com.mengxiang.transaction.framework.enums.TransactionTypeEnum;

/**
 * 努力确保型任务
 * @author JoinFyc 
 * @date 2019年6月18日 下午5:31:30
 */
public abstract class InsurableTask<T extends TaskExecuteResult> implements TransactionTask<T> {

    @Override
    public String getTaskType() {
        return this.getClass().getSimpleName();
    }
    
   
    
    @Override
	public final TransactionTypeEnum getTransactionType() {
		return TransactionTypeEnum.INSURE;
	}



	@Override
    public TaskRetryStrategyEnum getRetryStrategy() {
        return TaskRetryStrategyEnum.INCREASING_INTERVAL;
    }
	
	@Override
	public String serializeAdditionalInfo() {
		return null;
	}
	
	
    
	
	/**
	 * 当任务首次执行异常，系统执行重试恢复时，框架会调用此方法执行重试恢复，默认情况下直接调用doExecute方法
	 * 当远程服务不支持幂等调用时，子类可通过重写此方法来定制重试恢复逻辑
	 * @return
	 */
	public  T doRecover()
	{
		return doExecute();
	}

	/**
	 * 异步回调
	 * 注意：如果回调时业务代码发生异常，框架不会发起重试
	 * @param result
	 */
	public void callback(T result)
	{
		
	}


    
}
