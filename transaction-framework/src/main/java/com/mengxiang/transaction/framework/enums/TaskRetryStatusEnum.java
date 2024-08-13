package com.mengxiang.transaction.framework.enums;


/**
 * 
 * 任务执行状态
 * @author JoinFyc 
 * @date 2019年6月18日 下午5:53:58
 */
public enum TaskRetryStatusEnum {
    
    WAIT_RETRY("待重试"),
    
    RETRY_FINISHED("重试完成"),
    
    RETRY_ULTRALIMIT("重试超过最大限制"), 
    
    NO_RETRY("无需重试");
    
    private String desc;
    
    private TaskRetryStatusEnum( String desc)
    {
        this.desc = desc;
    }
    
    public  String getDesc()
    {
        return desc;
    }
    
    
}
