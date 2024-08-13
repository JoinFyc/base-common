package com.mengxiang.base.common.sequence.model;

/**
 * @author JoinFyc
 * @date 2020/12/15 14:05
 */
public class Event {
    private long record;
    private long time;
    private int code;
    private boolean effective = true;

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isEffective() {
        return effective;
    }

    public void setEffective(boolean effective) {
        this.effective = effective;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
