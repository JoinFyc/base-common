package com.mengxiang.base.common.process.executor;

import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;
import com.mengxiang.base.common.process.model.BusinessType;
import com.mengxiang.base.common.process.processor.BusinessProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务执行器抽象类
 */
public class BusinessExecutorImpl implements BusinessExecutor {


    /**
     * 日志打印器
     */
    private final static Logger logger = LoggerFactory
            .getLogger(BusinessExecutorImpl.class);
    /**
     * 业务类型
     */
    private BusinessType businessType;

    /**
     * 业务处理器列表
     */
    private List<BusinessProcessor<BusinessContext, BusinessModel>> businessProcessors = new ArrayList<BusinessProcessor<BusinessContext, BusinessModel>>();

    /**
     * 业务执行方法
     *
     * @param context
     * @return
     */
    @Override
    public InnerResult execute(BusinessContext context) {

        Assert.isTrue(!CollectionUtils.isEmpty(businessProcessors),
                "没有获取到执行器。context = " + context);

        InnerResult result = null;
        for (BusinessProcessor processor : businessProcessors) {
            if (logger.isDebugEnabled()) {
                logger.debug("开始执行processor={}", processor.getProcessorName());
            }

            result = processor.process(context);

            if (!result.getSuccess()) {

                logger.warn("processor={}执行失败。result={}",
                        processor.getProcessorName(), result);
                return result;

            } else {

                if (context.getNeedInterrupt()) {
                    logger.warn("processor={}执行成功，并中断执行后续处理器。result={}",
                            processor.getProcessorName(), result);
                    return result;
                }

            }

        }
        return result;

    }

    /**
     * 获取业务类型
     *
     * @return
     */
    @Override
    public BusinessType getBusinessType() {
        return businessType;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param businessType value to be assigned to property businessType
     */
    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param businessProcessors value to be assigned to property businessProcessors
     */
    public void setBusinessProcessors(List<BusinessProcessor<BusinessContext, BusinessModel>> businessProcessors) {
        this.businessProcessors = businessProcessors;
    }
}
