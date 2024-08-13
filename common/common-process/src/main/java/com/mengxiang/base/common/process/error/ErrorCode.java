package com.mengxiang.base.common.process.error;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 标准错误码
 * <p>此错误码是全站推行的标准错误码。
 */
public class ErrorCode implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 5144645768812859240L;

    /**
     * 具体错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 展示错误信息
     */
    private String displayMsg;

    // ~~~ 构造方法

    /**
     * 默认构造方法
     */
    public ErrorCode() {
    }

    /**
     * 构造方法
     */
    public ErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode(String errorCode,
                     String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 构造方法
     */
    public ErrorCode(String errorCode, String errorMsg, String displayMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.displayMsg = displayMsg;
    }

    // ~~~ 重写方法

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        //组装错误码
        StringBuffer sb = new StringBuffer();
        sb.append(errorCode);
        if (StringUtils.isNotBlank(errorMsg)) {
            sb.append("|");
            sb.append(errorMsg);
        }
        if (StringUtils.isNotBlank(displayMsg)) {
            sb.append("|");
            sb.append(displayMsg);
        }
        return sb.toString();
    }

    // ~~~ 内部方法

    /**
     * 字符串长度检查
     *
     * @param str
     * @param length
     */
    private void checkString(String str, int length) {

        if (str == null || str.length() != length) {

            throw new IllegalArgumentException();
        }
    }

    /**
     * Getter method for property <tt>errorSpecific</tt>.
     *
     * @return property value of errorSpecific
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Setter method for property <tt>errorSpecific</tt>.
     *
     * @param errorCode value to be assigned to property errorSpecific
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Getter method for property <tt>errorMsg</tt>.
     *
     * @return property value of errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param errorMsg value to be assigned to property errorMsg
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * Getter method for property <tt>displayMsg</tt>.
     *
     * @return property value of displayMsg
     */
    public String getDisplayMsg() {
        return displayMsg;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param displayMsg value to be assigned to property displayMsg
     */
    public void setDisplayMsg(String displayMsg) {
        this.displayMsg = displayMsg;
    }
}
