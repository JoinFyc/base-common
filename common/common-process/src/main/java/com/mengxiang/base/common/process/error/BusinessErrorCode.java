package com.mengxiang.base.common.process.error;

/**
 * 业务错误码接口，用于各业务错误码枚举实现
 */
public interface BusinessErrorCode {

    /**
     * 获取枚举编码
     *
     * @return 异常code
     */
    ErrorCode getErrorCode();

    /**
     * 获取枚举编码
     */
    String getErrorCodeStr();

    /**
     * 获取错误描述
     */
    String getErrorMsg();

    /**
     * 获取错误提示，用于展现给用户
     */
    String getDisplayMsg();

}
