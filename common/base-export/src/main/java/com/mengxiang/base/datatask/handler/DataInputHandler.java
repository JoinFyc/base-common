package com.mengxiang.base.datatask.handler;

import com.mengxiang.base.datatask.DataTaskContext;

import java.util.List;
import java.util.UUID;

public abstract class DataInputHandler<T> {

    private volatile String genId;

    /**
     * 处理逻辑
     *
     * @param ctx
     * @return
     */
    public abstract void handler(DataTaskContext ctx);

    public abstract String id();

    public String genId() {
        if(null == genId) {
            synchronized (DataInputHandler.class) {
                if(null == genId) {
                    genId = id() + "-" + UUID.randomUUID().toString().replaceAll("-","");
                }
            }
        }
        return genId;
    }

    /**
    public void collectData(DataTaskContext ctx, String exportKey, T data) {
        if(null == data) {
            return;
        }
        ctx.collectData(genId(),false,exportKey,data);
    }
    **/

    public void collectData(DataTaskContext ctx, String exportKey, List<T> dataList) {
        if(null == dataList || dataList.isEmpty()) {
            return;
        }
        ctx.collectData(genId(),true,exportKey,dataList);
    }

}
