package com.mengxiang.base.common.process.executor;

import com.mengxiang.base.common.process.exception.ProcessException;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;
import com.mengxiang.base.common.process.model.BusinessType;
import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.processor.BusinessProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JoinFyc
 * @date 2020/11/4
 **/
public class BusinessSubTransExecutorImpl implements BusinessExecutor {
    /**
     * 日志打印器
     */
    private final static Logger logger = LoggerFactory.getLogger(BusinessSubTransExecutorImpl.class);

    /**
     * 业务类型
     */
    private BusinessType businessType;

    /**
     * 业务处理器列表
     */
    private List<SubTranHolder> subTranHolders = new ArrayList<>();

    /**
     * 事务模板
     */
    protected TransactionTemplate transactionTemplate;

    /**
     * 业务执行方法
     *
     * @param context
     * @return
     */
    @Override
    public InnerResult execute(BusinessContext context) {

        Assert.isTrue(!CollectionUtils.isEmpty(subTranHolders),
                "没有获取到执行器。context = " + context);

        InnerResult result = null;

        BusinessExecutorImpl businessExecutor = new BusinessExecutorImpl();
        BusinessTransExecutorImpl businessTransExecutor = new BusinessTransExecutorImpl();
        businessTransExecutor.setTransactionTemplate(transactionTemplate);
        for (int i = 0; i < subTranHolders.size(); i++) {
            SubTranHolder subTranHolder = subTranHolders.get(i);
            BusinessExecutorImpl executor = subTranHolder.isTrans() ? businessTransExecutor : businessExecutor;
            executor.setBusinessProcessors(subTranHolder.getBusinessProcessors());
            try {
                result = executor.execute(context);
            } catch (Exception exception) {
                logger.warn(MessageFormat.format("执行业务出现业务异常，context={0}", context), exception);
                if (exception instanceof ProcessException) {
                    ProcessException processException = (ProcessException) exception;
                    result = new InnerResult(processException.getErrorContext());
                }
                if (subTranHolder.isNeedInterrupt()) {
                    logger.info(MessageFormat.format("executor finish,subTranHolder failed,index {0},context {1}", i, context));
                    throw exception;
                }
            }
            if (!result.getSuccess() && subTranHolder.isNeedInterrupt()) {
                logger.info(MessageFormat.format("executor finish,subTranHolder failed,index {0},context {1}", i, context));
                return result;
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

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public void setSubTranHolders(List<SubTranHolder> subTranHolders) {
        this.subTranHolders = subTranHolders;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public static class SubTranHolder {
        private List<BusinessProcessor<BusinessContext, BusinessModel>> businessProcessors;
        private boolean trans = false;
        private boolean needInterrupt = true;

        public boolean isTrans() {
            return trans;
        }

        public void setTrans(boolean trans) {
            this.trans = trans;
        }

        public void setBusinessProcessors(List<BusinessProcessor<BusinessContext, BusinessModel>> businessProcessors) {
            this.businessProcessors = businessProcessors;
        }

        public List<BusinessProcessor<BusinessContext, BusinessModel>> getBusinessProcessors() {
            return businessProcessors;
        }

        public boolean isNeedInterrupt() {
            return needInterrupt;
        }

        public void setNeedInterrupt(boolean needInterrupt) {
            this.needInterrupt = needInterrupt;
        }
    }
}
