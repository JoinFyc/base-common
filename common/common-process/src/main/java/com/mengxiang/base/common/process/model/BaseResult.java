package com.mengxiang.base.common.process.model;

import com.mengxiang.base.common.model.exception.constant.IErrorCode;
import com.mengxiang.base.common.process.error.BusinessErrorCode;
import com.mengxiang.base.common.process.error.CommonError;
import com.mengxiang.base.common.process.error.ErrorCode;
import com.mengxiang.base.common.process.error.ErrorContext;

import java.io.Serializable;

/**
 * @author JoinFyc
 * @date 2020/10/19
 **/
public class BaseResult implements Serializable {
    /**
     * 成功标志
     */
    private boolean success = false;

    /**
     * 错误码
     */
    private ErrorCode errorCode;

    /**
     * 错误上下文
     */
    private ErrorContext errorContext;

    /**
     * 构造函数W
     */

    public BaseResult() {
    }

    /**
     * 构造函数
     *
     * @param errorCode
     * @param location
     */
    public BaseResult(ErrorCode errorCode, String location) {
        this.errorCode = errorCode;
        CommonError commonError = new CommonError(errorCode, errorCode.getErrorMsg(), location);

        if (this.errorContext == null) {
            this.errorContext = new ErrorContext();
        }
        this.errorContext.addError(commonError);

    }

    /**
     * 构造函数
     *
     * @param errorContext
     */
    public BaseResult(ErrorContext errorContext) {

        if (this.errorContext == null) {
            this.errorContext = new ErrorContext();
        }
        CommonError commonError = this.errorContext.fetchCurrentError();
        if (commonError == null) {
            this.errorCode = new ErrorCode(IErrorCode.PROCESSING_ERROR);
        } else {
            this.errorCode = commonError.getErrorCode();
        }

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
     * Getter method for property <tt>errorCode</tt>.
     *
     * @return property value of errorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param errorCode value to be assigned to property errorCode
     */
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
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

    @Override
    public String toString() {
        return "BaseResult{" + "success=" + success + ", errorCode=" + errorCode + ", errorContext="
                + errorContext + '}';
    }
}
