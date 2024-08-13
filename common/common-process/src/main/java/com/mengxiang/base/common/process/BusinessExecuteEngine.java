package com.mengxiang.base.common.process;

import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;

/**
 * 业务执行引擎接口
 */
public interface BusinessExecuteEngine<C extends BusinessContext, M extends BusinessModel> {

    /**
     * 业务引擎执行方法
     *
     * @param context 业务上下文
     * @return 业务执行结果
     */
    InnerResult<M> execute(C context);

}
