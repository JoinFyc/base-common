package com.mengxiang.base.common.sequence.impl;

import com.mengxiang.base.common.sequence.Sequence;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.model.Record;
import com.mengxiang.base.common.sequence.rule.Rule;
import com.mengxiang.base.common.sequence.rule.RuleEnum;
import com.mengxiang.base.common.sequence.rule.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sequence工厂实现类
 *
 * @author JoinFyc
 */
public class SequenceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SequenceConstant.SEQUENCE_LOG_NAME);

    private DefaultSequenceDAO sequenceDAO;

    /**
     * Sequence MAP
     */
    private Map<String, Sequence> sequenceMap = new ConcurrentHashMap<>(0);
    private Map<String, Rule> sequenceRuleMap = new ConcurrentHashMap<>(0);

    /**
     * 业务组
     */
    private List<Record> businessTypes;

    /**
     * 构造函数
     *
     * @param sequenceDAO
     */
    public SequenceFactory(DefaultSequenceDAO sequenceDAO) {
        this.sequenceDAO = sequenceDAO;
    }

    /**
     * 初始化Sequence的工厂
     * 从数据源里获取sequence的记录，对每一条记录进行处理，生成对应的multipleSequence对象，加载到内存中
     *
     * @throws Exception
     */
    public void init() throws SQLException {
        if (sequenceDAO == null) {
            throw new IllegalArgumentException("The sequenceDao is null!");
        }
        if (businessTypes != null || businessTypes.size() > 0) {
            initBusinessRecord(businessTypes);
        }
        initAllSequence();
    }

    private void initBusinessRecord(List<Record> businessTypes) throws SQLException {
        sequenceDAO.initBusinessRecord(businessTypes);
    }

    /**
     * 根据sequenceName初始化Sequence
     */
    private void initAllSequence() {
        Map<String, Record> sequenceRecords = null;
        //获取全部的sequence记录
        try {
            sequenceRecords = sequenceDAO.getAllSequenceNameRecord();
            if (sequenceRecords == null) {
                throw new IllegalArgumentException("ERROR ## The sequenceRecord is null!");
            }
            for (Map.Entry<String, Record> sequenceRecord : sequenceRecords
                    .entrySet()) {
                String seqName = sequenceRecord.getKey().trim();
                Record record = sequenceRecord.getValue();
                long min = record.getMin();
                long max = record.getMax();
                int step = record.getStep();
                int code = record.getCode();
                DefaultSequence sequence = new DefaultSequence(sequenceDAO, seqName, code, min, max,
                        step);
                try {
                    sequenceMap.put(seqName, sequence);
                } catch (Exception e) {
                    logger.error("ERROR ## init the sequenceName = " + seqName + " has an error:",
                            e);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR ## init the multiple-Sequence-Map failed!", e);
        }

    }


    /**
     * 获取Sequence对象
     *
     * @param sequenceName Sequence名称
     * @return
     */
    public Sequence getSequence(String sequenceName) {
        Sequence sequence = sequenceMap.get(sequenceName);
        if (sequence == null) {
            throw new SequenceException("找不到对应的sequence对象。sequenceName=" + sequenceName);
        }

        return sequence;
    }

    public Rule getSequenceRule(String sequenceName) {
        Rule rule = sequenceRuleMap.get(sequenceName);
        if (rule == null) {
            rule = RuleFactory.getRule(RuleEnum.NORMAL.getVersion());
            sequenceRuleMap.put(sequenceName, rule);
        }
        return rule;
    }

    /**
     * 刷新step
     *
     * @param businessName
     * @param newStep
     */
    public void refreshStep(String businessName, int newStep) {
        try {
            sequenceDAO.refreshStep(businessName, newStep);
        } catch (SQLException ex) {
            logger.error("refresh step failed", ex);
        }
    }

    /**
     * Getter method for property <tt>sequenceDAO</tt>.
     *
     * @return property value of sequenceDAO
     */
    public DefaultSequenceDAO getSequenceDAO() {
        return sequenceDAO;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceDAO value to be assigned to property sequenceDAO
     */
    public void setSequenceDAO(DefaultSequenceDAO sequenceDAO) {
        this.sequenceDAO = sequenceDAO;
    }

    public List<Record> getBusinessTypes() {
        return businessTypes;
    }

    public void setBusinessTypes(List<Record> businessTypes) {
        this.businessTypes = businessTypes;
    }

    public void setSequenceRule(Map<String, Rule> ruleMap) {
        sequenceRuleMap.putAll(ruleMap);
    }
}
