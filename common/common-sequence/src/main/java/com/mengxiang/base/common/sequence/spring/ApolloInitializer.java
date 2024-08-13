package com.mengxiang.base.common.sequence.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.impl.SequenceFactory;
import com.mengxiang.base.common.sequence.model.Record;
import com.mengxiang.base.common.sequence.rule.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JoinFyc
 * @date 2020/12/18 13:50
 */
class ApolloInitializer {
    private static final Logger log = LoggerFactory.getLogger(SequenceConstant.SEQUENCE_LOG_NAME);
    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(",").omitEmptyStrings().trimResults();
    private final Config config = ConfigService.getConfig("BASE.SEQUENCE");
    private static final String DEFAULT_APOLLO_STEP_KEY = "sequence.step";

    /**
     * 默认版本，22位
     */
    private final String sequenceConfigV1Suffix = "";
    /**
     * 支持多种模式
     */
    private final String sequenceConfigV2Suffix = ".desc";
    /**
     * 支持多个sequence
     */
    private final String sequenceConfigV3Suffix = ".v3.desc";
    private ApplicationContext applicationContext;

    private List<Record> apolloRecords = Lists.newArrayList();

    private final Map<String, Rule> ruleMap = Maps.newConcurrentMap();
    private String appId;

    ApolloInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        appId = Foundation.app().getAppId();
        if (resolveV3ApolloConfig(appId)) {
            return;
        }
        if (resolveV2ApolloConfig(appId)) {
            return;
        }
        resolveV1ApolloConfig(appId);
    }

    private boolean resolveV3ApolloConfig(String appId) {
        String desc = config.getProperty(appId + sequenceConfigV3Suffix, "");
        if (StringUtils.isBlank(desc)) {
            return false;
        }
        List<ApolloV3SequenceDesc> apolloSequenceDescs = JSON.parseObject(desc, new TypeReference<List<ApolloV3SequenceDesc>>() {
        });
        if (CollectionUtils.isEmpty(apolloSequenceDescs)) {
            return false;
        }
        for (ApolloV3SequenceDesc apolloSequenceDesc : apolloSequenceDescs) {

            checkV3SequenceConfig(apolloSequenceDesc);

            String sequenceName = apolloSequenceDesc.getSequenceName();
            String code = apolloSequenceDesc.getCode();
            Integer model = apolloSequenceDesc.getModel();
            Long max = apolloSequenceDesc.getMax();
            max = max == null ? SequenceConstant.DEFAULT_MAX : max;
            Integer step = apolloSequenceDesc.getStep();
            Record record = resolveRecord(sequenceName, code, max, step);
            Rule rule = RuleFactory.getRule(model);
            RuleEnum ruleEnum = rule.getRuleEnum();
            if (code.length() > (ruleEnum.getBigBusinessLength() + ruleEnum.getSmallBusinessLength())) {
                throw new SequenceException("business code长度大于sequence要求，请检查配置");
            }
            int length = max.toString().length();
            Long abs = getMaxSequenceLength(length);
            rule.init(abs.equals(max) ? length - 1 : length);
            ruleMap.put(sequenceName, rule);
            apolloRecords.add(record);
        }
        return true;
    }

    private void checkV3SequenceConfig(ApolloV3SequenceDesc apolloSequenceDesc) {
        String code = apolloSequenceDesc.getCode();
        String sequenceName = apolloSequenceDesc.getSequenceName();
        if (StringUtils.isBlank(code) || StringUtils.isBlank(sequenceName)) {
            throw new SequenceException("sequence config is not standard:" + JSONObject.toJSONString(apolloSequenceDesc));
        }
    }

    private boolean resolveV2ApolloConfig(String appId) {
        String desc = config.getProperty(appId + sequenceConfigV2Suffix, "");
        if (StringUtils.isBlank(desc)) {
            return false;
        }
        ApolloV3SequenceDesc apolloSequenceDesc = JSON.parseObject(desc, new TypeReference<ApolloV3SequenceDesc>() {
        });
        if (apolloSequenceDesc == null) {
            return false;
        }
        String code = apolloSequenceDesc.getCode();
        Integer model = apolloSequenceDesc.getModel();
        Long max = apolloSequenceDesc.getMax();
        max = max == null ? SequenceConstant.DEFAULT_MAX : max;
        Integer step = apolloSequenceDesc.getStep();
        Record record = resolveRecord(appId, code, max, step);

        Rule rule = RuleFactory.getRule(model);
        RuleEnum ruleEnum = rule.getRuleEnum();
        if (code.length() > (ruleEnum.getBigBusinessLength() + ruleEnum.getSmallBusinessLength())) {
            throw new SequenceException("business code长度大于sequence要求，请检查配置");
        }
        int length = max.toString().length();
        Long abs = getMaxSequenceLength(length);
        rule.init(abs.equals(max) ? length - 1 : length);
        ruleMap.put(appId, rule);
        apolloRecords = Lists.newArrayList(record);
        return true;
    }

    private boolean resolveV1ApolloConfig(String appId) {
        String desc = config.getProperty(appId + sequenceConfigV1Suffix, "");
        if (desc.length() == 0) {
            apolloRecords = Lists.newArrayList();
            return false;
        }
        Integer step = getStep();
        Record record = resolveRecord(appId, desc, SequenceConstant.DEFAULT_MAX, step);
        ruleMap.put(appId, new NormalRule());
        apolloRecords = Lists.newArrayList(record);
        return true;
    }

    private Long getMaxSequenceLength(int length) {
        Long abs = 1L;
        for (int i = 1; i < length; i++) {
            abs *= 10;
        }
        return abs;
    }

    private Record resolveRecord(String sequenceName, String code, Long max, Integer step) {
        Record record = new Record();
        record.setCode(Integer.parseInt(code));
        record.setStep(step);
        record.setMin(SequenceConstant.DEFAULT_MIN);
        record.setMax(max);
        record.setName(sequenceName);
        //默认一个比较小的值，初始化之后，会再第一次获取range时更新这个值
        record.setEffectiveTime(1L);
        return record;
    }

    public String getBusinessName() {
        return appId;
    }

    public void initApolloListener(SequenceFactory sequenceFactory) {
        List<String> namespaces = resolveApolloNamespace();
        for (String namespace : namespaces) {
            Config config = ConfigService.getConfig(namespace);
            String property = config.getProperty(DEFAULT_APOLLO_STEP_KEY, "");
            if (StringUtils.isNotBlank(property)) {
                try {
                    int i = Integer.parseInt(property);
                    addApolloListener(config, sequenceFactory);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取用户自己配置的step值
     *
     * @return
     */
    private Integer getStep() {
        List<String> namespaces = resolveApolloNamespace();
        for (String namespace : namespaces) {
            Config config = ConfigService.getConfig(namespace);
            String property = config.getProperty(DEFAULT_APOLLO_STEP_KEY, "");
            if (StringUtils.isNotBlank(property)) {
                try {
                    return Integer.parseInt(property);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return SequenceConstant.DEFAULT_STEP;
    }

    private void addApolloListener(Config config, SequenceFactory sequenceFactory) {
        Set<String> set = new HashSet<>();

        set.add(DEFAULT_APOLLO_STEP_KEY);
        config.addChangeListener((changeEvent) -> {
            ConfigChange change = changeEvent.getChange(DEFAULT_APOLLO_STEP_KEY);
            String newValue = change.getNewValue();
            int newStep = Integer.parseInt(newValue);
            log.info("sequence refresh step,new step is " + newStep);
            sequenceFactory.refreshStep(getBusinessName(), newStep);
        }, set);
    }

    private List<String> resolveApolloNamespace() {
        Environment environment = applicationContext.getEnvironment();
        String namespaces = environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);
        return namespaceList;
    }

    public List<Record> getApolloRecords() {
        return apolloRecords;
    }

    public Map<String, Rule> getRuleMap() {
        return ruleMap;
    }

    static class ApolloV3SequenceDesc {
        private Long max;
        private Integer model = 0;
        private String code = "0";
        private String sequenceName;
        private Integer step = SequenceConstant.DEFAULT_STEP;

        public Long getMax() {
            return max;
        }

        public void setMax(Long max) {
            this.max = max;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getModel() {
            return model;
        }

        public void setModel(Integer model) {
            this.model = model;
        }

        public String getSequenceName() {
            return sequenceName;
        }

        public void setSequenceName(String sequenceName) {
            this.sequenceName = sequenceName;
        }

        public Integer getStep() {
            return step;
        }

        public void setStep(Integer step) {
            this.step = step;
        }
    }
}
