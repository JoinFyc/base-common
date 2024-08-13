package com.mengxiang.base.common.process.model;

import com.mengxiang.base.common.process.error.ErrorCode;
import com.mengxiang.base.common.process.error.ErrorContext;

/**
 * @author JoinFyc
 * @date 2020-09-22 16:16
 **/
public class InnerResult<T> extends BaseResult {

    /** 业务对象 */
    private T object;

    /**
     * 构造函数
     */
    public InnerResult() {
    }

    /**
     * 构造函数
     *
     * @param isSuccess
     * @param obj
     */
    public InnerResult(boolean isSuccess, T obj) {
        super.setSuccess(isSuccess);
        this.object = obj;
    }

    /**
     * 构造函数
     *
     * @param errorCode
     * @param location
     */
    public InnerResult(ErrorCode errorCode, String location) {
        super(errorCode, location);
    }

    /**
     * 构造函数
     *
     * @param errorCode
     * @param location
     * @param obj
     */
    public InnerResult(ErrorCode errorCode, String location, T obj) {
        super(errorCode, location);
        this.object = obj;
    }

    /**
     * 构造函数
     *
     * @param errorContext
     */
    public InnerResult(ErrorContext errorContext) {
        super(errorContext);
    }

    /**
     * Getter method for property <tt>object</tt>.
     *
     * @return property value of object
     */
    public T getObject() {
        return object;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param object value to be assigned to property object
     */
    public void setObject(T object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}