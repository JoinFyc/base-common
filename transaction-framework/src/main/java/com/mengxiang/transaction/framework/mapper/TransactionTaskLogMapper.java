package com.mengxiang.transaction.framework.mapper;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;

/**
 * transaction operate mapper
 * @author yuyong01
 * @author JoinFyc
 */
public interface TransactionTaskLogMapper {

    /**
     * save with db server time create time = now(), update = now()
     * @param taskDo taskDo
     * @return id
     */
    int save(TransactionTaskLogDO taskDo);

    /**
     * spec create time and update save
     * use for test unit data generate
     * @param taskDo taskDo
     * @return id
     */
    int saveCustomTime(TransactionTaskLogDO taskDo);

    /**
     * update with db server time update = now()
     * @param taskDo task
     */
    void update(TransactionTaskLogDO taskDo);

    /**
     * query task with spec task id
     * @param taskId task id
     * @param taskType task type
     * @return exist return task or null if not found
     */
    TransactionTaskLogDO selectByTaskId(@Param("taskId") String taskId, @Param("taskType") String taskType);
    
    /**
     * 批量更新事务日志状态
     * @param taskIdList
     * @param targetStatus
     * @param srouceStatus
     * @return
     */
    int batchUpdateStatus(@Param("idList") List<Long> idList,@Param("targetStatus") String targetStatus, @Param("sourceStatus") String sourceStatus);
    
    
    /**
     * 更新冲正状态
     * @param id
     * @param status
     * @return
     */
    int updateResvalStatus(TransactionTaskLogDO transactionLog);

   
    /**
     * 获取努力确保型待重试任务列表
     * @param limitCount 一次取多少量
     * @param hour 往前推多长小时
     * @return 任务列表
     */
    List<TransactionTaskLogDO> selectRetryTaskForInsure(@Param("limitCount") int limitCount, @Param("hour") int hour);


    /**
     * 获取努力确保型长期处理中任务列表
     * @param limitCount 一次取多少量
     * @param hour 往前推多长小时
     * @return 任务列表
     */
    List<TransactionTaskLogDO> selectLongtimeProcessingTaskForInsure(@Param("limitCount") int limitCount, @Param("hour") int hour);
    
    
    /**
     * 获取异常冲正型待重试任务列表
     * @param limitCount 一次取多少量
     * @return 任务列表
     */
    List<TransactionTaskLogDO> selectLongtimeProcessingTaskForReversal(@Param("limitCount") int limitCount, @Param("hour") int hour);
    
    /**
     * 获取异常冲正型待重试任务列表
     * @param limitCount
     * @return
     */
    List<TransactionTaskLogDO> selectRetryTaskTaskForReversal(@Param("limitCount") int limitCount, @Param("hour") int hour);
    
    
    
    /**
     * @param ids
     * @return
     */
    int delete(@Param("expireTime") Timestamp expireTime,
            @Param("taskStatus") List<String> taskStatus);
    
    int deleteById(@Param("id")Long id);
    
    int batchDelete(@Param("idList") List<Long> idList);

}
