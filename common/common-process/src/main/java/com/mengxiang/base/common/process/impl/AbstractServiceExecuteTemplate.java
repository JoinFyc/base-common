package com.mengxiang.base.common.process.impl;

import com.mengxiang.base.common.model.request.BaseRequest;
import com.mengxiang.base.common.process.model.InnerResult;
import com.mengxiang.base.common.process.model.ServiceResult;
import com.mengxiang.base.common.process.BusinessExecuteEngine;
import com.mengxiang.base.common.process.model.BusinessContext;
import com.mengxiang.base.common.process.model.BusinessModel;

/**
 * 抽象的服务执行模板
 */
public abstract class AbstractServiceExecuteTemplate<B extends BaseRequest, C extends BusinessContext, M extends BusinessModel, R extends ServiceResult> {

    /**
     * 业务执行引擎
     */
    protected BusinessExecuteEngine<C, M> businessExecuteEngine;

    /**
     * 服务执行方法
     *
     * @param request     业务请求
     * @param interceptor 服务拦截器，可以服务执行前检查，执行后构造结果
     * @return
     */
    protected R execute(B request, ServiceInterceptor<B, C, M, R> interceptor) {

        //服务执行前置处理
        interceptor.before(request);

        //构造请求上下文
        C context = interceptor.getBusinessContext(request);

        //业务引擎执行
        InnerResult<M> innerResult = businessExecuteEngine.execute(context);

        //服务执行后置处理
        interceptor.after(request, innerResult, context);

        //获取结果实例
        R result = interceptor.getResultInstance(context, innerResult);

        return result;

    }

    /**
     * 服务拦截器内部抽象类
     *
     * @param <C> 业务上下文
     * @param <M> 业务模型
     * @param <R> 结果
     */
    protected static abstract class ServiceInterceptor<B, C, M, R> {

        /**
         * 服务执行前置处理
         */
        public void before(B request) {

        }

        /**
         * 服务执行后置处理
         */
        public void after(B request, InnerResult<M> innerResult, C context) {

        }

        /**
         * 获取业务上下文实例
         *
         * @param request
         * @return
         */
        abstract public C getBusinessContext(B request);

        /**
         * 获取结果实例
         *
         * @param context
         * @param innerResult
         * @return
         */
        abstract public R getResultInstance(C context, InnerResult<M> innerResult);

    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param businessExecuteEngine value to be assigned to property businessExecuteEngine
     */
    public void setBusinessExecuteEngine(BusinessExecuteEngine<C, M> businessExecuteEngine) {
        this.businessExecuteEngine = businessExecuteEngine;
    }

}
