package com.mengxiang.base.common.sequence.model;

/**
 * @author JoinFyc
 * @date 2020/12/14 15:20
 */
public class Record {
    private Long min;
    private Long max;
    private Integer step;
    private Long effectiveTime;
    private String name;
    private Integer code;

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }


    public Long getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}
