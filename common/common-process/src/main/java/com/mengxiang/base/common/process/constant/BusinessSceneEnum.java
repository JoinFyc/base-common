package com.mengxiang.base.common.process.constant;

/**
 * 业务场景枚举
 */
public enum BusinessSceneEnum {
    //
    UNKNOWN("9999", "未知业务"),

    //==================== 登录 ====================
    WECHAT_AUTH("0000", "微信授权登录"),

    USER_REGISTER("0100", "用户注册"),

    USER_IDENTIFY("0200", "用户识别"),

    USER_USERINFO("0202", "用户集管理"),

    CREDIT_USERINFO("2200", "信用卡用户"),

    CREDIT_ORDERINFO("2210", "申请订单"),

    FILE_UPLOAD("0300", "上传文件"),

    CONTENT_KNOWLEDGE_STORE("0400", "内容集管理"),

    CONTENT_SET_MANAGE("0500", "内容集管理"),

    CONTENT_MANAGE("0600", "内容集管理"),

    VERIFICATION_INFO("1400", "核销码"),

    REDEMPTION_CODE("2200", "兑换码"),

    //==================== 交易 ====================
    TRADE("0700", "交易"),

    FUND("0800", "交易"),
    //==================== 支付 ====================

    PAYMENT("0900", "支付"),
    //==================== 账务 ====================
    ACCOUNTTRANS("1000", "账务"),

    //==================== BASE组件  ====================
    MESSAGE("1100", "消息中心"),

    CHARGING("1101", "流量计费"),

    FLOW_FEE("1102", "用户流量费计算"),

    PROTOCOL("1103", "用户流量费计算"),

    //==================== 组件  ====================
    COMPONENT("9900", "流程"),

    TRAFFIC("9901", "限流"),

    WECHAT_SMALL_PROGRAM("1400", "微信小程序"),

    LOGIN_TIMEOUT("1300", "登录超时"),

    CHOSEN_MALL_SHOW("2100", "精选商城展示"),

    CASHIER("2000", "店铺收银"),

    DATA_QUERY("1500", "数据中心查询"),
    //==================== 营销平台  ====================
    ACTIVITY("1600", "活动"),

    AWARD("1700", "奖品"),

    VOUCHER("1800", "优惠券"),

    BUDGET("1900", "预算"),

    TRUST_LOGIN("2300", "信任登录"),

    AQUA_APP("2400", "AQUA app接口"),

    PRESENTS_RECEIVE("2500", "礼包领取"),

    COLLECT_INFO("2600", "信息采集");

    /**
     * 枚举值
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 构造函数
     *
     * @param code
     * @param desc
     */
    BusinessSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据枚举值获取枚举
     *
     * @param code
     * @return BusinessSceneEnum
     */
    public static BusinessSceneEnum getByCode(String code) {
        for (BusinessSceneEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
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
     * Getter method for property <tt>desc</tt>.
     *
     * @return property value of desc
     */
    public String getDesc() {

        return desc;
    }
}
