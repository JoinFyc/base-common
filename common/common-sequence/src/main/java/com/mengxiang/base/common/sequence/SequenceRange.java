package com.mengxiang.base.common.sequence;

import com.mengxiang.base.common.sequence.model.Event;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 序列区间
 *
 * @author JoinFyc
 */
public class SequenceRange {
    private final long min;
    private final long max;
    private final long effectiveTime;
    private final AtomicLong value;
    private volatile boolean over = false;

    public SequenceRange(long min, long max, long effectiveTime) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
        this.effectiveTime = effectiveTime;
    }

    /**
     * 获取Seq并自增
     *
     * @return
     */
    public Event getEvent() {
        long currentValue = value.getAndIncrement();
        Event event = new Event();
        event.setRecord(currentValue);
        event.setTime(effectiveTime);
        if (currentValue > max) {
            over = true;
            event.setEffective(false);
            return event;
        }
        return event;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public boolean isOver() {
        return over;
    }

    public long getEffectiveTime() {
        return effectiveTime;
    }

    public AtomicLong getValue() {
        return value;
    }
}
