package com.mengxiang.base.datatask.upload.provider.model;

import java.io.Serializable;

public class Result<T> implements Serializable {
    private static final Integer OK_CODE;
    private static final String OK_STRING = "成功";
    private Integer code;
    private Boolean success;
    private String message;
    private T data;

    public Result() {
    }

    public static Result<Void> success() {
        Result<Void> result = new Result();
        result.setCode(OK_CODE);
        result.setMessage("成功");
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result();
        result.setCode(OK_CODE);
        result.setMessage("成功");
        result.setSuccess(Boolean.TRUE);
        result.setData(data);
        return result;
    }

    public static Result error(Integer code, String message) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public static Result error(Exception ex) {
        Result result = new Result();
        if (ex instanceof IErrorCode) {
            IErrorCode exWithErrorCode = (IErrorCode) ex;
            result.setCode(exWithErrorCode.getCode());
        } else {
            result.setCode(IErrorCode.SYSTEM_ERROR);
        }

        result.setMessage(ex.getMessage());
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    static {
        OK_CODE = IErrorCode.SUCCESS;
    }
}