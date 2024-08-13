package com.mengxiang.transaction.framework.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 事务一致性框架属性
 * @author JoinFyc
 * @Date 2020年11月10日
 *
 */
@ConfigurationProperties(prefix = "com.mengxiang.transaction.task")
public class TransactionTaskProperties {

    private boolean enable                   = false;


    /**
     * 重试获取行数
     */
    private int     retryFetchSize           = 50;

    /**
     * 核心线程数
     */
    private int     insureCorePoolSize             = 2;
    /**
     * 最大线程数
     */
    private int     insureMaxPoolSize              = 10;
    /**
     * 队列大小
     */
    private int     insureQueueCapacity            = 1000;
    
    

    /**
     * 核心线程数
     */
    private int     reversalCorePoolSize             = 2;
    /**
     * 最大线程数
     */
    private int     reversalMaxPoolSize              = 10;
    /**
     * 队列大小
     */
    private int     reversalQueueCapacity            = 1000;

    /**
     * 异常回复定时任务的corn表达式
     */
    private String  recoverCornExpression    = "0 0/5 * * * ?";

    /**
     * 错误日志开始次数
     * 对于努力确保型任务,业务前n-1几次错误，将不会产生错误日志
     * 从第n次开始，开始报错,
     * 默认第一次报错就开始打错误日志
     * 1 表示首次出错误开始打错误日志
     */
    private int errorLogBeginTimes    = 1;

    /**
     * 是否开启任务重试
     */
    private boolean recoverEnable            = false;

    /**
     * 定时删除任务状态类型 以“,”分隔，例：SUCCESS,FAILED
     */
    private String  deleteTaskStatus         = "SUCCESS";

    /**
     * 定时删除多少天以前的任务
     */
    private int     taskExpireTime           = 7;

    /**
     * 是否开启过期删除任务
     */
    private boolean enableExpireDelete = true;
    
    /**
     * 是否开启立即删除执行成功的任务
     */
    private boolean enableImmediatelyDelete = true;



    public String getRecoverCornExpression() {
        return recoverCornExpression;
    }

    public void setRecoverCornExpression(String recoverCornExpression) {
        this.recoverCornExpression = recoverCornExpression;
    }

  
    public int getInsureCorePoolSize() {
		return insureCorePoolSize;
	}

	public void setInsureCorePoolSize(int insureCorePoolSize) {
		this.insureCorePoolSize = insureCorePoolSize;
	}

	public int getInsureMaxPoolSize() {
		return insureMaxPoolSize;
	}

	public void setInsureMaxPoolSize(int insureMaxPoolSize) {
		this.insureMaxPoolSize = insureMaxPoolSize;
	}

	public int getInsureQueueCapacity() {
		return insureQueueCapacity;
	}

	public void setInsureQueueCapacity(int insureQueueCapacity) {
		this.insureQueueCapacity = insureQueueCapacity;
	}

	public int getReversalCorePoolSize() {
		return reversalCorePoolSize;
	}

	public void setReversalCorePoolSize(int reversalCorePoolSize) {
		this.reversalCorePoolSize = reversalCorePoolSize;
	}

	public int getReversalMaxPoolSize() {
		return reversalMaxPoolSize;
	}

	public void setReversalMaxPoolSize(int reversalMaxPoolSize) {
		this.reversalMaxPoolSize = reversalMaxPoolSize;
	}

	public int getReversalQueueCapacity() {
		return reversalQueueCapacity;
	}

	public void setReversalQueueCapacity(int reversalQueueCapacity) {
		this.reversalQueueCapacity = reversalQueueCapacity;
	}

	public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getRetryFetchSize() {
        return retryFetchSize;
    }

    public void setRetryFetchSize(int retryFetchSize) {
        this.retryFetchSize = retryFetchSize;
    }

    public boolean isRecoverEnable() {
        return recoverEnable;
    }

    public void setRecoverEnable(boolean recoverEnable) {
        this.recoverEnable = recoverEnable;
    }

    /**
     * @return the deleteTaskStatus
     */
    public String getDeleteTaskStatus() {
        return deleteTaskStatus;
    }

    /**
     * @param deleteTaskStatus the deleteTaskStatus to set
     */
    public void setDeleteTaskStatus(String deleteTaskStatus) {
        this.deleteTaskStatus = deleteTaskStatus;
    }

    /**
     * @return the taskExpireTime
     */
    public int getTaskExpireTime() {
        return taskExpireTime;
    }

    /**
     * @param taskExpireTime the taskExpireTime to set
     */
    public void setTaskExpireTime(int taskExpireTime) {
        this.taskExpireTime = taskExpireTime;
    }

	public boolean isEnableExpireDelete() {
		return enableExpireDelete;
	}

	public void setEnableExpireDelete(boolean enableExpireDelete) {
		this.enableExpireDelete = enableExpireDelete;
	}

	public boolean isEnableImmediatelyDelete() {
		return enableImmediatelyDelete;
	}

	public void setEnableImmediatelyDelete(boolean enableImmediatelyDelete) {
		this.enableImmediatelyDelete = enableImmediatelyDelete;
	}

    public int getErrorLogBeginTimes() {
        return errorLogBeginTimes;
    }

    public void setErrorLogBeginTimes(int errorLogBeginTimes) {
        this.errorLogBeginTimes = errorLogBeginTimes;
    }
}
