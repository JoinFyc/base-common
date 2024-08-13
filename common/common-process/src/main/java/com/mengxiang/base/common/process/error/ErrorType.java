package com.mengxiang.base.common.process.error;

/**
 * 错误类型常量池
 *
 * <p>对应于标准错误码的第5位
 */
public interface ErrorType {

    /**
     * 系统错误
     */
    public static final String SYSTEM = "0";

    /**
     * 业务错误
     */
    public static final String BIZ = "1";

    /**
     * 第三方错误
     */
    public static final String THIRD_PARTY = "2";

}
