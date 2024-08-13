package com.mengxiang.base.common.process.model;

/**
 * 业务类型接口，对应的业务类型Enum可继承该接口
 */
public interface BusinessType {

    /**
     * 获取业务编码
     *
     * @return
     */
    String getCode();

    /**
     * 获取业务描述
     *
     * @return
     */
    String getDesc();

}
