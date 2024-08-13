package com.mengxiang.base.common.apollo.client.listener;

/**
 * Apollo配置监听器
 *
 * @author ice
 * @version 1.0
 * @date 2019/4/22 1:29 PM
 */
public interface BaseApolloConfigListener {

    /**
     * 获取监听的namespace，可以有多个，以<code>,</code>分隔
     * 如果需要监听全部namespace，可以使用<code>*</code>表示
     *
     * @return
     */
    String namespace();

    /**
     * 配置发生变更时调用
     *
     * @param event 配置变更事件
     */
    void onConfigChange(BaseApolloConfigChangeEvent event);
}
