package com.mengxiang.base.common.sequence.engine;

import com.mengxiang.base.common.sequence.Sequence;
import com.mengxiang.base.common.sequence.impl.SequenceFactory;
import com.mengxiang.base.common.sequence.model.Event;
import com.mengxiang.base.common.sequence.rule.Rule;

import java.util.Calendar;

/**
 * sequence rule engine
 *
 * @author JoinFyc
 */
public class SequenceEngine {
    private final SequenceFactory sequenceFactory;

    public SequenceEngine(SequenceFactory sequenceFactory) {
        this.sequenceFactory = sequenceFactory;
    }

    /**
     * 获取拼接后的sequence
     *`
     * @param businessType 业务
     * @param shardingKey  分库分表标识
     * @param failover     预留字段
     * @return
     */
    public String getSequence(String businessType, int shardingKey, Integer failover) {
        Sequence sequence = sequenceFactory.getSequence(businessType);
        Event event = sequence.nextEvent();
        String date = generateCurrentDate();
        return sequenceFactory.getSequenceRule(businessType).getSequence(date, event.getRecord(), sequence.getCode(), shardingKey, failover);
    }


    /**
     * 获取日期 YYYYMMDD
     *
     * @return
     */
    private String generateCurrentDate() {
        //使用默认时区和语言环境获得一个日历。
        Calendar rightNow = Calendar.getInstance();
        //只保留后面两位
        int year = rightNow.get(Calendar.YEAR) - 2000;
        //第一个月从0开始，所以得到月份＋1
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append(month < 10 ? "0" + month : month).append(day < 10 ? "0" + day : day);
        return sb.toString();
    }

    public String resolveVersion(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveVersion(sequence);
    }

    public String resolveFailover(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveFailover(sequence);
    }

    public String resolveShardingCode(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveShardingCode(sequence);
    }

    public String resolveShardingDs(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveShardingDs(sequence);
    }

    public String resolveBusinessCode(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveBusinessCode(sequence);
    }

    public String resolveCurrentDate(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveCurrentDate(sequence);
    }

    public String resolveSequenceIndex(String businessType, String sequence) {
        return sequenceFactory.getSequenceRule(businessType).resolveSequenceIndex(sequence);
    }
}
