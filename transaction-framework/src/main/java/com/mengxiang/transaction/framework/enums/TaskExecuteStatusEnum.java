package com.mengxiang.transaction.framework.enums;

/**
 * 事务日志状态
 * 
 * @author JoinFyc
 * @date 2019年6月18日 下午5:53:58
 */
public enum TaskExecuteStatusEnum {

    PROCESSING("处理中"),

    EXCEPTION("异常"),

    FAILED("失败"),

    SUCCESS("成功"),
    
    COMMITED("已提交")
    ;

    private String desc;

    private TaskExecuteStatusEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 是否终结状态
     * @return
     */
    public boolean isEndForInsurableTask() {
        return this == SUCCESS || this == FAILED;
    }

}
	