package com.mengxiang.base.common.process.model;

/**
 * 业务上下文
 */
public class BusinessContext {

    /**
     * 业务类型
     */
    private BusinessType businessType;

    /**
     * 业务类型
     */
    private boolean      needInterrupt = false;

    /**
     * Getter method for property <tt>businessType</tt>.
     *
     * @return property value of businessType
     */
    public BusinessType getBusinessType() {
        return businessType;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param businessType value to be assigned to property businessType
     */
    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    /**
     * Getter method for property <tt>needInterrupt</tt>.
     *
     * @return property value of needInterrupt
     */
    public boolean getNeedInterrupt() {
        return needInterrupt;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param needInterrupt value to be assigned to property needInterrupt
     */
    public void setNeedInterrupt(boolean needInterrupt) {
        this.needInterrupt = needInterrupt;
    }

}
