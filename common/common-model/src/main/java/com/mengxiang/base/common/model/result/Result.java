package com.mengxiang.base.common.model.result;

import com.mengxiang.base.common.model.exception.constant.IErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回对象统一封包，尽量使用类的静态方法创建对象
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/5/9 12:13 AM
 */
@Data
public class Result<T> implements Serializable {

    private static final String OK_CODE = IErrorCode.SUCCESS;

    private static final String OK_STRING = "成功";

    private String code;

    private boolean success = true;

    private String message;

    private T data;

    public Result() {

    }


    protected Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    protected Result(String code, T data, String message) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(OK_CODE);
        result.setMessage(OK_STRING);
        result.setSuccess(Boolean.TRUE);
        result.setData(data);
        return result;
    }

    public static Result<Void> success() {
        Result<Void> result = new Result<>();
        result.setCode(OK_CODE);
        result.setMessage(OK_STRING);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<T>(OK_CODE, data, message);
    }

    public static <T> Result<T> error() {
        return error(IErrorCode.SYSTEM_ERROR, "未知异常，请联系管理员");
    }

    public static <T> Result<T> error(String code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setMessage(msg);
        result.setCode(IErrorCode.SYSTEM_ERROR);
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public static <T> Result<T> error(Exception ex) {
        Result<T> result = new Result<>();
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

}
