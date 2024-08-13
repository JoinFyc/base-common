package com.mengxiang.base.common.sequence.rule;

/**
 * @author JoinFyc
 * @date 2021/2/3 9:47
 */
public interface Rule {

    RuleEnum getRuleEnum();

    /**
     * 获取序号中的年月日  yymmdd
     *
     * @param sequence
     * @return
     */
    String resolveCurrentDate(String sequence);

    /**
     * 获取其中的序号
     *
     * @param sequence
     * @return
     */
    String resolveSequenceIndex(String sequence);

    /**
     * 获取业务类型
     *
     * @param sequence
     * @return
     */
    String resolveBusinessCode(String sequence);

    /**
     * 获取自定义的sharding code
     *
     * @param sequence
     * @return
     */
    String resolveShardingCode(String sequence);

    /**
     * 获取failover标识，没有的返回X
     *
     * @param sequence
     * @return
     */
    String resolveFailover(String sequence);

    /**
     * 获取版本
     *
     * @param sequence
     * @return
     */
    String resolveVersion(String sequence);

    /**
     * 获取版本
     *
     * @param sequence
     * @return
     */
    String resolveShardingDs(String sequence);

    /**
     * 校验sequence是否符合规则
     *
     * @param sequence
     * @return
     */
    boolean checkSequence(String sequence);

    /**
     * 获取sequence
     *
     * @param date
     * @param sequence
     * @param businessCode
     * @param shardCode
     * @param failover
     * @return
     */
    String getSequence(String date, Long sequence, int businessCode, int shardCode, int failover);


    void init(int sequenceLength);
}
