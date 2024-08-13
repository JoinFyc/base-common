package com.mengxiang.base.common.process.model;

import com.mengxiang.base.common.process.error.CommonError;
import com.mengxiang.base.common.process.error.ErrorCode;
import com.mengxiang.base.common.process.error.ErrorContext;

import java.io.Serializable;
import java.util.List;

/**
 * 服务结果
 */
public class ServiceResult implements Serializable {

    private static final long serialVersionUID = 7245490617365221031L;

    /**
     * 成功标志
     */
    private boolean success = false;

    /**
     * 错误上下文
     */
    private ErrorContext errorContext;

    /**
     * 构造函数
     */

    public ServiceResult() {
        super();
        this.success = false;
    }

    public ServiceResult(ErrorCode errorCode, String location) {
        CommonError commonError = new CommonError(errorCode, errorCode.getErrorMsg(), location);
        if (this.errorContext == null) {
            this.errorContext = new ErrorContext();
        }

        this.errorContext.addError(commonError);
    }

    /**
     * 构造函数
     *
     * @param success
     */
    public ServiceResult(boolean success) {
        this.success = success;
    }

    /**
     * 构造函数
     *
     * @param errorContext
     */
    public ServiceResult(ErrorContext errorContext) {
        super();
        this.errorContext = new ErrorContext();
        this.success = false;
    }

    /**
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param success value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
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
     * Setter method for property <tt>counterType</tt>.
     *
     * @param errorContext value to be assigned to property errorContext
     */
    public void setErrorContext(ErrorContext errorContext) {
        this.errorContext = errorContext;
    }

    public ErrorCode getErrorCode() {
        if (errorContext == null) {
            return null;
        }
        List<CommonError> commonErrorList = errorContext.getErrorStack();
        CommonError commonError = commonErrorList.get(0);
        return commonError.getErrorCode();
    }

    public void setErrorCode(ErrorCode errorCode, String location) {
        CommonError commonError = new CommonError(errorCode, errorCode.getErrorMsg(), location);
        if (this.errorContext == null) {
            this.errorContext = new ErrorContext();
        }

        this.errorContext.addError(commonError);
    }

    @Override
    public String toString() {
        return "ServiceResult{" + "success=" + success + ", errorContext=" + errorContext + '}';
    }

}
