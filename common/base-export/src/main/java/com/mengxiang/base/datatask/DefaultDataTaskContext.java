package com.mengxiang.base.datatask;

import com.mengxiang.base.datatask.exception.TaskExecuteException;
import com.mengxiang.base.datatask.model.CollectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultDataTaskContext extends DataTaskContext {

    static Logger logger = LoggerFactory.getLogger(DefaultDataTaskContext.class);
    /**
     * input数据队列
     */
    //private Queue<CollectData> collectDataQueue = new ConcurrentLinkedQueue();
    private LinkedBlockingQueue<CollectData> collectDataQueue;//new LinkedBlockingQueue<>(100);

    /**
     * 任务input队列待消费数据量
     */
    private ConcurrentHashMap<String, AtomicLong> collectDataCounts = new ConcurrentHashMap<>();

    /**
     * 任务input结束
     */
    private ConcurrentHashMap<String, Boolean> taskFinishs = new ConcurrentHashMap<>();

    /**
     * input队列待消费数
     */
    private AtomicLong collectCount = new AtomicLong(0);

    public DefaultDataTaskContext(int dataHandleQueueSize) {
        collectDataQueue = new LinkedBlockingQueue<>(dataHandleQueueSize);
    }

    @Override
    public void collectData(String id,boolean isCollection, String exportKey, Object data) {

        if( null == taskFinishs.get(id) ) {
            logger.warn("数据收集失败[无法识别任务] id[{}] exportKey[{}]", id, exportKey);
            return;
        }

        if( taskFinishs.get(id) ) {
            logger.warn("数据收集失败[任务已结束] id[{}] exportKey[{}]", id, exportKey);
            return;
        }

        try {
            collectDataQueue.put(new CollectData(id, isCollection, exportKey, data));//offer
        } catch (Exception e) {
            throw new TaskExecuteException(e);
        }

        initCollectDataCount(id);
        AtomicLong collectDataCount = collectDataCounts.get(id);
        //+数据收集数量
        if(isCollection) {
            collectDataCount.addAndGet(((List)data).size());
        } else {
            collectDataCount.incrementAndGet();
        }
        collectCount.incrementAndGet();
    }

    @Override
    CollectData getCollectData(Long timeout) {
        try {
            if(null == timeout) {
                return collectDataQueue.take();//poll
            } else {
                return collectDataQueue.poll(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            throw new TaskExecuteException(e);
        }
        //return collectDataQueue.poll();
    }

    @Override
    long getCollectDataCount(String id) {
        AtomicLong collectDataCount = collectDataCounts.get(id);
        return null == collectDataCount ? 0 : collectDataCount.get();
    }

    @Override
    void cleanCollectDataCount(String id) {
        collectDataCounts.remove(id);
    }


    @Override
    void setTaskCollectFinish(String id, boolean taskFinish) {
        taskFinishs.put(id, taskFinish);
    }

    @Override
    boolean isTaskCollectFinish(String id) {
        return null == taskFinishs.get(id) ? false : taskFinishs.get(id);
    }


    @Override
    void initCollectDataCount(String id) {
        AtomicLong collectDataCount = collectDataCounts.get(id);
        if(null == collectDataCount) {
            synchronized (this) {
                collectDataCount = collectDataCounts.get(id);
                if(null == collectDataCount) {
                    collectDataCount = new AtomicLong(0);
                    collectDataCounts.put(id, collectDataCount);
                }
            }
        }

        //return collectDataCount;
    }


    @Override
    long collectCount() {
        return collectCount.get();
    }

    @Override
    long collectCountDecr() {
        return collectCount.decrementAndGet();
    }


}
