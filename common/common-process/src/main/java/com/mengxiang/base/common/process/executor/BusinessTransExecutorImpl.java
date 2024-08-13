package com.mengxiang.base.common.process.executor;

import com.mengxiang.base.common.process.exception.ProcessException;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;
import com.mengxiang.base.common.process.model.InnerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.MessageFormat;

/**
 * 业务执行器，支持事务模板
 */
public class BusinessTransExecutorImpl extends BusinessExecutorImpl {

    /**
     * 日志打印器
     */
    private final static Logger logger = LoggerFactory.getLogger(BusinessTransExecutorImpl.class);

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
    public InnerResult execute(final BusinessContext context) {
        InnerResult<BusinessModel> result = transactionTemplate.execute(transactionStatus -> {
            InnerResult innerResult = null;
            try {
                innerResult = BusinessTransExecutorImpl.super.execute(context);
                return innerResult;
            } catch (ProcessException e) {
                logger.warn(MessageFormat.format("执行业务出现业务异常，context={0}", context), e);
                transactionStatus.setRollbackOnly();
                innerResult = new InnerResult(e.getErrorContext());
            } catch (Exception e) {
                logger.warn(MessageFormat.format("执行业务出现未知异常，context={0}", context), e);
                transactionStatus.setRollbackOnly();
                throw e;
            }
            return innerResult;
        });
        return result;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param transactionTemplate value to be assigned to property transactionTemplate
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
}
