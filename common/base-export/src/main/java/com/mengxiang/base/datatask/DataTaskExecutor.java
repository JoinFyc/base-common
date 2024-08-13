package com.mengxiang.base.datatask;

import com.mengxiang.base.datatask.model.DataTaskResult;

import java.util.concurrent.Future;


public interface DataTaskExecutor {


    /**
     * 提交任务
     * @return
     */
    Future<DataTaskResult> submit(DataTask task) throws Exception;

    void close();

}
