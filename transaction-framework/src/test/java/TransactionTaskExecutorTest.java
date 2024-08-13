
import com.mengxiang.transaction.framework.dao.TransactionTaskLogDO;
import com.mengxiang.transaction.framework.enums.*;
import com.mengxiang.transaction.framework.executor.InsurableTaskExecutor;
import com.mengxiang.transaction.framework.executor.ReversibleTaskExecutor;
import com.mengxiang.transaction.framework.job.InsurableTaskExceptionRecoverJob;
import com.mengxiang.transaction.framework.job.ReversibleTaskExceptionRecoverJob;
import com.mengxiang.transaction.framework.mapper.TransactionTaskLogMapper;
import com.mengxiang.transaction.framework.task.InsurableTask;
import com.mengxiang.transaction.framework.task.ReversibleTask;
import com.mengxiang.transaction.framework.task.TaskExecuteResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

/**
 * * 一致性框架测试用例
 * *
 * * @author JoinFyc
 * * @Date 2020年12月12日
 * *
 */
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application.class)
public class    TransactionTaskExecutorTest {

    @Autowired
    private TransactionTaskLogMapper taskMapper;

    @Autowired
    private InsurableTaskExecutor insurableTaskExecutor;

    @Autowired
    private ReversibleTaskExecutor reversibleTaskExecutor;

    @Autowired
    private InsurableTaskExceptionRecoverJob exceptionRecoverJob;


    @Autowired
    private ReversibleTaskExceptionRecoverJob reversibleExceptionRecoverJob;


    @Test
    @Rollback(true)
    public void testForSuccess() throws InterruptedException, ExecutionException {
        TaskExecuteResult mockResult = new TaskExecuteResult();
        mockResult.setExecuteStatus(TaskExecuteStatusEnum.SUCCESS);
        TestTransactionTask task = new TestTransactionTask(mockResult, null);
        insurableTaskExecutor.execute(task);
        Thread.sleep(200);
        TransactionTaskLogDO dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(task.getTaskId(), dbtask.getTaskId());
        Assert.assertEquals(task.getTaskType(), dbtask.getTaskType());
        Assert.assertEquals(dbtask.getRetryStatus(), null);
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.SUCCESS.name());
        Assert.assertEquals(1, dbtask.getTimes());
    }


    @Test
    @Rollback(true)
    public void testForFailed() throws InterruptedException, ExecutionException {
        TaskExecuteResult mockResult = new TaskExecuteResult();
        mockResult.setExecuteStatus(TaskExecuteStatusEnum.FAILED);
        TestTransactionTask task = new TestTransactionTask(mockResult, null);
        insurableTaskExecutor.execute(task);
        Thread.sleep(200);
        TransactionTaskLogDO dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(task.getTaskId(), dbtask.getTaskId());
        Assert.assertEquals(task.getTaskType(), dbtask.getTaskType());
        Assert.assertEquals(dbtask.getRetryStatus(), null);
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.FAILED.name());
        Assert.assertEquals(1, dbtask.getTimes());
    }


    @Test
    @Rollback(true)
    public void testForExceptionRecover() throws InterruptedException, ExecutionException {
        TaskExecuteResult mockResult = new TaskExecuteResult();
        mockResult.setExecuteStatus(TaskExecuteStatusEnum.EXCEPTION);
        TestTransactionTask task = new TestTransactionTask(mockResult, null);
        insurableTaskExecutor.execute(task);
        Thread.sleep(200);
        TransactionTaskLogDO dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(task.getTaskId(), dbtask.getTaskId());
        Assert.assertEquals(task.getTaskType(), dbtask.getTaskType());
        Assert.assertEquals(dbtask.getRetryStatus(), TaskRetryStatusEnum.WAIT_RETRY.name());
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.EXCEPTION.name());
        Assert.assertEquals(1, dbtask.getTimes());
    }


    @Test
    public void testRecoverForInsurableTask() {
//执行恢复
        exceptionRecoverJob.execute("");
    }

    // 异常冲正型
    @Test
    @Rollback(true)
    public void testSuccessForReversibleTask() throws InterruptedException, ExecutionException {
        TaskExecuteResult mockResult = new TaskExecuteResult();
        mockResult.setExecuteStatus(TaskExecuteStatusEnum.SUCCESS);
        TestReversibleTask task = new TestReversibleTask(mockResult, null);
        TaskExecuteResult result = reversibleTaskExecutor.execute(task);
        Assert.assertEquals(result.getExecuteStatus(), TaskExecuteStatusEnum.SUCCESS);
        TransactionTaskLogDO dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(task.getTaskId(), dbtask.getTaskId());
        Assert.assertEquals(task.getTaskType(), dbtask.getTaskType());
        Assert.assertEquals(dbtask.getRetryStatus(), null);
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.SUCCESS.name());
        Assert.assertEquals(1, dbtask.getTimes());
        reversibleTaskExecutor.setTransactionStatus(TransactionStatusEnum.COMMIT);
        reversibleTaskExecutor.doFinally();

        Thread.sleep(200);
        dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.COMMITED.name());
    }


    @Test
    @Rollback(true)
    public void testExceptionForReversibleTask() throws InterruptedException, ExecutionException {
        TaskExecuteResult mockResult = new TaskExecuteResult();
        mockResult.setExecuteStatus(TaskExecuteStatusEnum.EXCEPTION);
        TestReversibleTask task = new TestReversibleTask(mockResult, null);
        TaskExecuteResult result = reversibleTaskExecutor.execute(task);
        Assert.assertEquals(result.getExecuteStatus(), TaskExecuteStatusEnum.EXCEPTION);
        TransactionTaskLogDO dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(task.getTaskId(), dbtask.getTaskId());
        Assert.assertEquals(task.getTaskType(), dbtask.getTaskType());
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.EXCEPTION.name());
        Assert.assertEquals(1, dbtask.getTimes());
        reversibleTaskExecutor.setTransactionStatus(TransactionStatusEnum.ROLLBACK);
        reversibleTaskExecutor.doFinally();

        Thread.sleep(200);
        dbtask = taskMapper.selectByTaskId(task.getTaskId(), task.getTaskType());
        Assert.assertEquals(dbtask.getStatus(), TaskExecuteStatusEnum.EXCEPTION.name());
        Assert.assertEquals(dbtask.getReversalStatus(), TaskReversalStatusEnum.REVERSAL_SUCCESS.name());
    }


    public static class TestTransactionTask extends InsurableTask<TaskExecuteResult> {

        private TaskExecuteResult result;

        private String taskId = "TEST-" + RandomStringUtils.randomAlphanumeric(9);


        public TestTransactionTask() {
        }


        public TestTransactionTask(TaskExecuteResult result, String taskId) {
            this.result = result;
            if (StringUtils.isNotBlank(taskId)) {
                this.taskId = taskId;
            }
        }


        public TaskExecuteResult getResult() {
            return result;
        }


        public void setResult(TaskExecuteResult result) {
            this.result = result;
        }


        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        @Override


        public String getTaskType() {
            return "TEST";
        }

        @Override


        public String getTaskId() {
            return taskId;
        }


        @Override


        public TaskExecuteResult doExecute() {
            return result;
        }

        @Override


        public void rebuild(TransactionTaskLogDO taskDo) {
            this.taskId = taskDo.getTaskId();
            TaskExecuteResult result = new TaskExecuteResult();
            result.setExecuteStatus(TaskExecuteStatusEnum.SUCCESS);
            this.result = result;
        }

        @Override


        public String serializeAdditionalInfo() {
// TODO Auto-generated method stub
            return null;
        }

    }


    public static class TestReversibleTask extends ReversibleTask<TaskExecuteResult> {

        private TaskExecuteResult result;

        private String taskId = "TEST-" + RandomStringUtils.randomAlphanumeric(9);


        public TestReversibleTask() {
        }


        public TestReversibleTask(TaskExecuteResult result, String taskId) {
            this.result = result;
            if (StringUtils.isNotBlank(taskId)) {
                this.taskId = taskId;
            }
        }

        @Override


        public String getTaskId() {
// TODO Auto-generated method stub
            return taskId;
        }

        @Override


        public String getTaskType() {
            return "TEST";
        }

        @Override


        public TaskRetryStrategyEnum getRetryStrategy() {
            return TaskRetryStrategyEnum.INCREASING_INTERVAL;
        }

        @Override


        public TaskExecuteResult doExecute() {
            return result;
        }

        @Override


        public String serializeAdditionalInfo() {
            return null;
        }

        @Override


        public void rebuild(TransactionTaskLogDO taskDo) {
            this.taskId = taskDo.getTaskId();
            TaskExecuteResult result = new TaskExecuteResult();
            result.setExecuteStatus(TaskExecuteStatusEnum.SUCCESS);
            this.result = result;
        }

        @Override


        public TaskExecuteResult doReversal() {
            result.setExecuteStatus(TaskExecuteStatusEnum.SUCCESS);
            return result;
        }

        @Override


        public TransactionStatusEnum queryBizStatus() {
            return TransactionStatusEnum.COMMIT;
        }

    }

}