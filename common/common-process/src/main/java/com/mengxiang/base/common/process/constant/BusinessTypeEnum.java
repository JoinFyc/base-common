package com.mengxiang.base.common.process.constant;


import org.apache.commons.lang3.StringUtils;

/**
 * 业务类型
 *
 * @author admin
 */
public enum BusinessTypeEnum {
    //
    TRANS("00", "账务业务"),

    CUSTOMER("01", "会员"),

    PAYMENT("02", "支付"),

    TRADE("03", "交易"),

    FUND("04", "交易"),

    CONTENT("05", "内容"),

    CHARGING("06", "计费"),

    MESSAGE("07", "消息"),

    PROTOCOL("08", "协议"),

    VOUCHER("09", "优惠券"),

    ACTIVITY("10", "活动"),

    AWARD("11", "奖品"), BUDGET("12", "预算"),

    MISSION("13", "任务记录"),

    PT_TRANS("14", "积分账务"),
    ;

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
    BusinessTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据枚举值获取枚举
     *
     * @param code
     * @return BusinessSceneEnum
     */
    public static BusinessTypeEnum getByCode(String code) {
        for (BusinessTypeEnum item : values()) {
            if (StringUtils.equals(item.code, code)) {
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
