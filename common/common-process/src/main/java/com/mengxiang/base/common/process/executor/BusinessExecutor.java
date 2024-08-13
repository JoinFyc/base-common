package com.mengxiang.base.common.process.executor;


import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;
import com.mengxiang.base.common.process.model.BusinessType;

/**
 * 业务执行器
 */
public interface BusinessExecutor<C extends BusinessContext, M extends BusinessModel> {

    /**
     * 业务执行方法
     *
     * @param context
     * @return
     */
    InnerResult<M> execute(C context);

    /**
     * 获取业务类型
     *
     * @return
     */
    BusinessType getBusinessType();

}
