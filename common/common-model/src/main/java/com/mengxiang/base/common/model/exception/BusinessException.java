package com.mengxiang.base.common.model.exception;

import com.mengxiang.base.common.model.exception.constant.IErrorCode;

/**
 * 运行时业务异常，可以不进行catch
 *
 * @author ice
 * @version 1.0
 * @date 2019/5/9 12:34 AM
 */
public class BusinessException extends RuntimeException implements IErrorCode {

    private String code;

    public BusinessException() {
        this.code = UNDEFINED_ERROR;
    }

    public BusinessException(String message) {
        super(message);
        this.code = UNDEFINED_ERROR;
    }

    public BusinessException(String code, String message) {
        this(message);
        this.code = code;
    }

    public BusinessException(Throwable throwable) {
        super(throwable);
        this.code = SYSTEM_ERROR;
    }

    public BusinessException(String code, Throwable throwable) {
        this(throwable);
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
