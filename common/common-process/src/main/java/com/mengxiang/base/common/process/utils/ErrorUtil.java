package com.mengxiang.base.common.process.utils;

import com.mengxiang.base.common.process.error.CommonError;
import com.mengxiang.base.common.process.error.ErrorCode;
import com.mengxiang.base.common.process.error.ErrorContext;

/**
 * 标准错误码工具类。
 *
 * <p>使用标准错误码时，必须配置ErrorUtil的bean。否则获取不到错误位置信息。
 */
public class ErrorUtil {

    /**
     * 系统名称
     */
    private static String appName;

    // ~~~ 构造方法

    private ErrorUtil() {
    }

    /**
     * 创建一个CommonError
     *
     * @param errorCode
     * @param message
     * @return
     */
    public static CommonError makeError(ErrorCode errorCode, String message) {

        CommonError error = new CommonError();
        error.setErrorCode(errorCode);
        error.setErrorMsg(message);
        error.setLocation(getAppName());
        return error;
    }

    /**
     * 创建一个CommonError
     *
     * @param errorCode
     * @param message
     * @param location
     * @return
     */
    public static CommonError makeError(ErrorCode errorCode, String message, String location) {

        CommonError error = new CommonError();
        error.setErrorCode(errorCode);
        error.setErrorMsg(message);
        error.setLocation(location);
        return error;
    }

    /**
     * 增加一个error到errorContext中
     *
     * @param error
     * @return
     */
    public static ErrorContext addError(CommonError error) {
        return addError(null, error);
    }

    /**
     * 增加一个error到errorContext中
     *
     * @param context
     * @param error
     * @return
     */
    public static ErrorContext addError(ErrorContext context, CommonError error) {

        if (context == null) {
            context = new ErrorContext();
        }

        if (error == null) {

            return context;
        }

        context.addError(error);

        return context;
    }

    /**
     * 创建并且增加一个Error到errorContext中
     *
     * @param context
     * @param errorCode
     * @param message
     * @return
     */
    public static ErrorContext makeAndAddError(ErrorContext context, ErrorCode errorCode,
                                               String message) {

        CommonError error = makeError(errorCode, message);
        context = addError(context, error);

        return context;
    }

    /**
     * 创建并且增加一个Error到新的errorContext中
     *
     * @param errorCode
     * @param message
     * @return
     */
    public static ErrorContext makeAndAddError(ErrorCode errorCode, String message) {

        CommonError error = makeError(errorCode, message);
        ErrorContext context = addError(error);

        return context;
    }

    /**
     * 获取系统名称
     *
     * @return
     */
    public static String getAppName() {

        if (null == appName || "".equals(appName)) {
            return "unknown";
        }

        return appName;
    }

    /**
     * Setter method for property <tt>appName</tt>.
     *
     * @param appName value to be assigned to property appName
     */
    public static void setAppName(String appName) {
        ErrorUtil.appName = appName;
    }

}
