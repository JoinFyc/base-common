package com.mengxiang.base.datatask;

import com.mengxiang.base.datatask.model.CollectData;

import java.util.concurrent.atomic.AtomicLong;

public abstract class DataTaskContext {

    /**
     * 记录处理完成的数据
     * @param data
     */
    public abstract void collectData(String id,boolean isCollection, String exportKey,Object data);

    /**
     * 获取数据
     * @param timeout 毫秒
     * @return
     */
    abstract CollectData getCollectData(Long timeout);

    abstract long getCollectDataCount(String id);

    abstract void cleanCollectDataCount(String id);

    abstract void setTaskCollectFinish(String id, boolean taskFinish);

    abstract boolean isTaskCollectFinish(String id);

    abstract void initCollectDataCount(String id);

    abstract long collectCount();

    abstract long collectCountDecr();
}
