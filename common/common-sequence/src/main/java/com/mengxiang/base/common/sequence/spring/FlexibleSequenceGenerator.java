package com.mengxiang.base.common.sequence.spring;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.mengxiang.base.common.sequence.IGenerator;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author JoinFyc
 * @date 2021/3/5
 **/
public class FlexibleSequenceGenerator implements IGenerator {
    @Autowired
    private SequenceGenerator sequenceGenerator;

    /**
     * 生成sequence
     *
     * @param businessType
     * @return
     */
    public String flexibleGenerateSequence(String businessType) {
        return flexibleGenerateSequence(businessType, 0);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @param businessType
     * @return
     */
    public Long flexibleGenerateLongSequence(String businessType) {
        String sequence = flexibleGenerateSequence(businessType, 0);
        if (sequence.length() <= 19) {
            return Long.parseLong(sequence);
        }
        throw new SequenceException("can't parse sequence to long,sequence: " + sequence);
    }

    /**
     * 生成sequence
     *
     * @param businessType
     * @param shardingKey
     * @return
     */
    public String flexibleGenerateSequence(String businessType, Integer shardingKey) {
        if (shardingKey >= SequenceConstant.ONE_THOUSAND) {
            shardingKey = shardingKey % 1000;
        } else if (shardingKey < 0) {
            throw new SequenceException("sharding code must more than 0");
        }
        return flexibleGenerateSequence(businessType, shardingKey, 0);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @param businessType
     * @return
     */
    public Long flexibleGenerateLongSequence(String businessType, Integer shardingKey) {
        String sequence = flexibleGenerateSequence(businessType, shardingKey);
        if (sequence.length() <= 19) {
            return Long.parseLong(sequence);
        }
        throw new SequenceException("can't parse sequence to long,sequence: " + sequence);
    }

    /**
     * 生成sequence
     *
     * @param businessType
     * @param shardingKey  只有末尾3位有效
     * @param shardingDs   只有末尾2位有效
     * @return
     */
    public String flexibleGenerateSequence(String businessType, Integer shardingKey, Integer shardingDs) {
        if (shardingKey >= SequenceConstant.ONE_THOUSAND) {
            shardingKey = shardingKey % 1000;
        } else if (shardingKey < 0) {
            throw new SequenceException("sharding code must more than 0");
        }
        if (shardingDs >= SequenceConstant.ONE_HUNDRED) {
            shardingDs = shardingDs % 100;
        } else if (shardingDs < 0) {
            throw new SequenceException("sharding ds must more than 0");
        }
        //如果没有传businessType,直接使用默认的businessType
        if (StringUtils.isBlank(businessType)) {
            return sequenceGenerator.getSequence(shardingKey, 0);
        }
        String shardingKeyStr = com.mengxiang.base.common.sequence.utils.StringUtils.alignRight(shardingKey + "", 3, "0");
        String shardingDsStr = com.mengxiang.base.common.sequence.utils.StringUtils.alignRight(shardingDs + "", 2, "0");
        return sequenceGenerator.getSequenceEngine().getSequence(businessType, Integer.parseInt(shardingDsStr + shardingKeyStr), 0);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @param businessType
     * @return
     */
    public Long flexibleGenerateLongSequence(String businessType, Integer shardingKey, Integer shardingDs) {
        String sequence = flexibleGenerateSequence(businessType, shardingKey, shardingDs);
        if (sequence.length() <= 19) {
            return Long.parseLong(sequence);
        }
        throw new SequenceException("can't parse sequence to long,sequence: " + sequence);
    }

    /**
     * 获取business code
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleBusinessCode(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveBusinessCode(businessType, sequence);
    }

    /**
     * 获取该sequence的产生日期
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleDate(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveCurrentDate(businessType, sequence);
    }

    /**
     * 获取该sequence的sharding code
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleShardingCode(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveShardingCode(businessType, sequence);
    }

    /**
     * 获取该sequence的sharding ds
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleShardingDs(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveShardingDs(businessType, sequence);
    }

    /**
     * 获取该sequence的failover
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleFailover(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveFailover(businessType, sequence);
    }

    /**
     * 获取中间的序列号部分
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleSequenceIndex(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveSequenceIndex(businessType, sequence);
    }

    /**
     * 获取该sequence的version
     *
     * @param sequence
     * @return
     */
    public String resolveFlexibleVersion(String businessType, String sequence) {
        return sequenceGenerator.getSequenceEngine().resolveVersion(businessType, sequence);
    }
}
