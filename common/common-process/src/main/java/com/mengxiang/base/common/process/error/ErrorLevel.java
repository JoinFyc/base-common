package com.mengxiang.base.common.process.error;

/**
 * 错误级别常量
 *
 * <p>对应于标准错误码的第4位
 */
public interface ErrorLevel {

    /** INFO级别 */
    public static final String INFO  = "0";

    /** WARN级别 */
    public static final String WARN  = "2";

    /** ERROR级别 */
    public static final String ERROR = "4";

    /** FATAL级别 */
    public static final String FATAL = "6";

}