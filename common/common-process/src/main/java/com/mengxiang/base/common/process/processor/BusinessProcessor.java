package com.mengxiang.base.common.process.processor;

import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;

/**
 * 业务执行器接口
 */
public interface BusinessProcessor<C extends BusinessContext, M extends BusinessModel> {

    /**
     * 业务执行器执行方法
     *
     * @param context 业务上下文
     * @return 业务执行结果
     */
    InnerResult<M> process(C context);

    /**
     * 获取执行器名称
     *
     * @return
     */
    String getProcessorName();

}
