package com.mengxiang.base.common.process.exception;

import com.mengxiang.base.common.process.error.ErrorCode;
import com.mengxiang.base.common.process.error.ErrorContext;

/**
 * 业务引擎异常，允许自定义定义错误编码和错误信息
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/5/9 12:25 AM
 */
public class ProcessException extends RuntimeException {

    /**
     * 序列号
     */
    private static final long serialVersionUID = -4633704019528910925L;

    /**
     * 错误上下文对象
     */
    private ErrorContext errorContext;

    /**
     * 构造函数
     */
    public ProcessException() {
    }

    /**
     * 构造函数
     */
    public ProcessException(String msg) {
        super(msg);
    }

    /**
     * 构造函数
     */
    public ProcessException(ErrorContext errorCode) {
        super(errorCode.toString());
        this.errorContext = errorCode;
    }

    /**
     * Getter method for property <tt>errorContext</tt>.
     *
     * @return property value of errorContext
     */
    public ErrorContext getErrorContext() {
        return errorContext;
    }

    /**
     * 获取当前错误码
     *
     * @return
     */
    public ErrorCode getCurrentErrorCode() {
        return errorContext.fetchCurrentError().getErrorCode();
    }

    /**
     * 设置当前错误码
     *
     * @return
     */
    public void setCurrentErrorCode(ErrorCode errorCode) {
        this.errorContext.fetchCurrentError().setErrorCode(errorCode);
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param errorContext value to be assigned to property errorContext
     */
    public void setErrorContext(ErrorContext errorContext) {
        this.errorContext = errorContext;
    }

    @Override
    public String toString() {
        return "BusinessException{" + "errorContext=" + errorContext != null
                ? errorContext.toString() : "null" + "}";
    }
}
