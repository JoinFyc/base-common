package com.mengxiang.base.common.sequence.spring;

import com.alibaba.fastjson.JSONObject;
import com.mengxiang.base.common.sequence.IGenerator;
import com.mengxiang.base.common.sequence.adapter.SequenceDataSourceGenerate;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.engine.SequenceEngine;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.impl.DefaultSequenceDAO;
import com.mengxiang.base.common.sequence.impl.SequenceDataSourceHolder;
import com.mengxiang.base.common.sequence.impl.SequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author JoinFyc
 */
public class SequenceGenerator implements IGenerator {
    private static final Logger log = LoggerFactory.getLogger(SequenceConstant.SEQUENCE_LOG_NAME);

    @Autowired
    private ApplicationContext applicationContext;
    private SequenceEngine sequenceEngine;
    private ApolloInitializer apolloInitializer;

    @PostConstruct
    public void init() {
        DataSource dataSource = resolveSequenceDataSourceFromApplication();
        dataSource = SequenceDataSourceGenerate.generateDataSource(dataSource);
        if (dataSource == null) {
            throw new SequenceException("can't resolve effective dataSource");
        }
        SequenceDataSourceHolder sequenceDataSourceHolder = new SequenceDataSourceHolder(dataSource);
        apolloInitializer = new ApolloInitializer(applicationContext);
        apolloInitializer.init();
        SequenceFactory sequenceFactory = prepareSequenceFactory(sequenceDataSourceHolder);
        sequenceEngine = new SequenceEngine(sequenceFactory);
        apolloInitializer.initApolloListener(sequenceFactory);
    }

    /**
     * 不需要分区规则的sequence
     *
     * @return 拼接后的sequence
     */
    public String getSequence() {
        return getSequence(0);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @return
     */
    public Long getLongSequence() {
        String sequence = getSequence();
        if (sequence.length() <= 19) {
            return Long.parseLong(sequence);
        }
        throw new SequenceException("can't parse sequence to long,sequence: " + sequence);
    }

    /**
     * @param shardingKey 分库分表键
     * @return 拼接后的sequence
     */
    public String getSequence(int shardingKey) {
        if (shardingKey >= SequenceConstant.ONE_HUNDRED) {
            shardingKey = shardingKey % 100;
        } else if (shardingKey < 0) {
            throw new SequenceException("sharding code must more than 0");
        }
        return getSequence(shardingKey, null);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @return
     */
    public Long getLongSequence(int shardingKey) {
        String sequence = getSequence(shardingKey);
        if (sequence.length() <= 19) {
            return Long.parseLong(sequence);
        }
        throw new SequenceException("can't parse sequence to long,sequence: " + sequence);
    }

    /**
     * @param shardingKey 分库分表键，类似订单号，用户id，商品id等
     * @param failOver    预留字段
     * @return 拼接后的sequence
     */
    public String getSequence(int shardingKey, Integer failOver) {
        if (shardingKey >= SequenceConstant.ONE_HUNDRED) {
            shardingKey = shardingKey % 100;
        } else if (shardingKey < 0) {
            throw new SequenceException("sharding code must more than 0");
        }
        failOver = 0;
        String businessType = apolloInitializer.getBusinessName();
        return sequenceEngine.getSequence(businessType, shardingKey, failOver);
    }

    /**
     * 生成Long形态sequence，只能在19位以内的情况下生效
     *
     * @return
     */
    public Long getLongSequence(int shardingKey, Integer failover) {
        String sequence = getSequence(shardingKey, failover);
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
    public String resolveBusinessCode(String sequence) {
        return sequenceEngine.resolveBusinessCode(apolloInitializer.getBusinessName(), sequence);
    }

    /**
     * 获取该sequence的产生日期
     *
     * @param sequence
     * @return
     */
    public String resolveDate(String sequence) {
        return sequenceEngine.resolveCurrentDate(apolloInitializer.getBusinessName(), sequence);
    }

    /**
     * 获取该sequence的sharding code
     *
     * @param sequence
     * @return
     */
    public String resolveShardingCode(String sequence) {
        return sequenceEngine.resolveShardingCode(apolloInitializer.getBusinessName(), sequence);
    }

    /**
     * 获取该sequence的fail over
     *
     * @param sequence
     * @return
     */
    public String resolveFailOver(String sequence) {
        return sequenceEngine.resolveFailover(apolloInitializer.getBusinessName(), sequence);
    }

    /**
     * 获取中间的序列号部分
     *
     * @param sequence
     * @return
     */
    public String resolveSequenceIndex(String sequence) {
        return sequenceEngine.resolveSequenceIndex(apolloInitializer.getBusinessName(), sequence);
    }

    /**
     * 获取该sequence的version
     *
     * @param sequence
     * @return
     */
    public String resolveVersion(String sequence) {
        return sequenceEngine.resolveVersion(apolloInitializer.getBusinessName(), sequence);
    }

    SequenceEngine getSequenceEngine() {
        return sequenceEngine;
    }

    private DataSource resolveSequenceDataSourceFromApplication() {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(DataSource.class);
        if (beanNamesForType.length > 1) {
            String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(SequenceDataSource.class);
            if (beanNamesForAnnotation.length > 1) {
                throw new SequenceException("too much @SequenceDataSource:" + JSONObject.toJSONString(beanNamesForAnnotation));
            } else if (beanNamesForAnnotation.length == 0) {
                log.warn("can't check effective dataSource,so must check @Primary annotation,change application to GenericApplicationContext for get bean definition");
                GenericApplicationContext applicationContext = (GenericApplicationContext) this.applicationContext;
                for (String beanName : beanNamesForType) {
                    BeanDefinition beanDefinition = applicationContext.getBeanDefinition(beanName);
                    if (beanDefinition.isPrimary()) {
                        return this.applicationContext.getBean(beanName, DataSource.class);
                    }
                }
                throw new SequenceException("need one sequence dataSource,but find more,please use  @Primary or @SequenceDataSource," + JSONObject.toJSONString(beanNamesForType));
            } else {
                return applicationContext.getBean(beanNamesForAnnotation[0], DataSource.class);
            }
        } else if (beanNamesForType.length == 1) {
            String beanName = beanNamesForType[0];
            return applicationContext.getBean(beanName, DataSource.class);
        } else {
            throw new SequenceException("common-sequence need dataSource,but can't find,please check dataSource config");
        }
    }

    private SequenceFactory prepareSequenceFactory(SequenceDataSourceHolder sequenceDataSourceHolder) {
        DefaultSequenceDAO defaultSequenceDAO = new DefaultSequenceDAO();
        defaultSequenceDAO.setTableName(SequenceConstant.DEFAULT_TABLE_NAME);
        defaultSequenceDAO.setSequenceDataSourceHolder(sequenceDataSourceHolder);
        defaultSequenceDAO.init();
        SequenceFactory sequenceFactory = new SequenceFactory(defaultSequenceDAO);
        sequenceFactory.setBusinessTypes(apolloInitializer.getApolloRecords());
        sequenceFactory.setSequenceRule(apolloInitializer.getRuleMap());
        try {
            sequenceFactory.init();
        } catch (SQLException ex) {
            throw new SequenceException(ex);
        }
        return sequenceFactory;
    }
}
