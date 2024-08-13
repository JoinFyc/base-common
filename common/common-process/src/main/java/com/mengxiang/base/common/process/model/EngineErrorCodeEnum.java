package com.mengxiang.base.common.process.model;

import com.mengxiang.base.common.model.exception.constant.IErrorCode;
import com.mengxiang.base.common.process.constant.BusinessSceneEnum;
import com.mengxiang.base.common.process.error.BusinessErrorCode;
import com.mengxiang.base.common.process.error.ErrorCode;

/**
 * @author JoinFyc
 */
public enum EngineErrorCodeEnum implements BusinessErrorCode {
    //
    SYSTEM_ERROR(BusinessSceneEnum.COMPONENT,
            new ErrorCode(
                    IErrorCode.PROCESSING_ERROR).toString(),
            "未知系统异常",
            "网络繁忙，稍后再试试看吧:)"),

    UNKNOWN_ERROR(BusinessSceneEnum.COMPONENT,
            new ErrorCode("998").toString(),
            "未知异常",
            "网络繁忙，稍后再试试看吧:)");

    /**
     * 错误发生场景
     */
    private final BusinessSceneEnum errorScene;

    /**
     * 枚举编码
     */
    private final String code;

    /**
     * 错误描述
     */
    private final String errorMsg;

    /**
     * 错误提示，用于展现给用户
     */
    private final String displayMsg;

    /**
     * 构造方法
     *
     * @param errorScene
     * @param errorCode
     * @param errorMsg
     * @param displayMsg
     */
    EngineErrorCodeEnum(BusinessSceneEnum errorScene, String errorCode, String errorMsg, String displayMsg) {
        this.errorScene = errorScene;
        this.code = errorCode;
        this.errorMsg = errorMsg;
        this.displayMsg = displayMsg;
    }

    /**
     * 获取标准错误码对象
     *
     * @return
     */
    @Override
    public ErrorCode getErrorCode() {

        ErrorCode errorCode = new ErrorCode(code);
        errorCode.setErrorMsg(errorMsg);
        errorCode.setDisplayMsg(displayMsg);
        return errorCode;
    }

    /**
     * 获取标准错误码对象
     *
     * @return
     */
    @Override
    public String getErrorCodeStr() {

        return getErrorCode().toString();
    }

    /**
     * Getter method for property <tt>errorScene</tt>.
     *
     * @return property value of errorScene
     */
    public BusinessSceneEnum getErrorScene() {
        return errorScene;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>errorMsg</tt>.
     *
     * @return property value of errorMsg
     */
    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Getter method for property <tt>displayMsg</tt>.
     *
     * @return property value of displayMsg
     */
    @Override
    public String getDisplayMsg() {
        return displayMsg;
    }
}
