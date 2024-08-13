package com.mengxiang.base.common.process.processor;

import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象的业务处理器
 */
public abstract class AbstractBusinessProcessor<C extends BusinessContext, M extends BusinessModel>
        implements BusinessProcessor<C, M> {

    /**
     * 日志打印器
     */
    private final static Logger logger = LoggerFactory.getLogger(AbstractBusinessProcessor.class);

    /**
     * 业务执行前处理
     *
     * @param context 业务上下文
     * @return 业务执行结果
     */
    protected abstract InnerResult<M> beforeProcess(C context);

    /**
     * 业务执行后处理
     *
     * @param context 业务上下文
     * @return 业务执行结果
     */
    protected abstract InnerResult<M> doProcess(C context);

    /**
     * 业务执行器执行方法
     *
     * @param context 业务上下文
     * @return 业务执行结果
     */
    @Override
    public InnerResult<M> process(C context) {

        //业务前置执行
        InnerResult innerResult = beforeProcess(context);

        if (!innerResult.getSuccess() || context.getNeedInterrupt()) {
            logger.warn("业务执行前前置执行失败。result = {}", innerResult);
            return innerResult;
        }

        //执行业务
        innerResult = doProcess(context);

        if (!innerResult.getSuccess()) {
            logger.warn("业务处理失败。result = {}", innerResult);
        }

        return innerResult;
    }

}
