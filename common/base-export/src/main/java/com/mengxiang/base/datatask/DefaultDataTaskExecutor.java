package com.mengxiang.base.datatask;

import com.mengxiang.base.datatask.export.DataExport;
import com.mengxiang.base.datatask.export.ExportType;
import com.mengxiang.base.datatask.export.ExportWriteConfig;
import com.mengxiang.base.datatask.export.impl.DefaultDataExport;
import com.mengxiang.base.datatask.export.provider.ExcelExportWriteConfig;
import com.mengxiang.base.datatask.model.CollectData;
import com.mengxiang.base.datatask.model.DataTaskResult;
import com.mengxiang.base.datatask.model.SheetConfig;
import com.mengxiang.base.datatask.model.UploadResult;
import com.mengxiang.base.datatask.upload.DataUpload;
import com.mengxiang.base.datatask.upload.DestType;
import com.mengxiang.base.datatask.upload.exception.FileUploadException;
import com.mengxiang.base.datatask.upload.provider.OBSUploadConfig;
import com.mengxiang.base.datatask.util.ConsistentHashingWithoutVirtualNode;
import com.mengxiang.base.datatask.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DefaultDataTaskExecutor extends Collecter implements DataTaskExecutor {

    static Logger logger = LoggerFactory.getLogger(DefaultDataTaskExecutor.class);

    /**
    private static volatile DataTaskExecutor instance ;

    public static DataTaskExecutor newInstance(
            int dataInputHandleThreadPoolSize,
            int dataInputHandleQueueSize,
            int fileBatchWriteSize,
            int fileWriteThreadPoolSize,
            int fileWriteQueueSize
    ) {

        if (null == instance) {
            synchronized (DefaultDataTaskExecutor.class) {
                if(null == instance) {
                    instance = new DefaultDataTaskExecutor(
                            dataInputHandleThreadPoolSize,
                            dataInputHandleQueueSize,
                            fileBatchWriteSize,
                            fileWriteThreadPoolSize,
                            fileWriteQueueSize
                    );
                }
            }
        }
        return instance;
    }
    **/

    private int dataInputHandleThreadPoolSize = 5 ;

    private int dataInputHandleQueueSize = 10 ;

    private int fileBatchWriteSize = 5000;

    private int fileWriteThreadPoolSize = 5;

    private int fileWriteThreadPoolQueueSize = 20;

    /**
     * 消费队列状态信息输出
     */
    private Thread dataTaskInfoPrintThread;

    /**
     * 数据收集队列消费线程
     */
    private Thread collectInputDataProcessThread;

    /**
     * 数据输入器线程池
     */
    private ThreadPoolExecutor threadPoolCollectInputDataHandler;// = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    /**
     * 写文件线程池
     */
    private ConcurrentHashMap<String, ThreadPoolExecutor> fileWriteThreadPoolMap = new ConcurrentHashMap<>();

    /**
     * 写文件线程环形hash
     */
    private ConsistentHashingWithoutVirtualNode fileWriteThreadPoolConsistentHashing;

    /**
     * flush、close文件、压缩、上传文件线程池
     */
    private ThreadPoolExecutor threadPoolFileWriteFlushClose;

    /**
     * 数据任务上下文
     */
    private DataTaskContext dataTaskContext;

    /**
     * 数据任务信息缓存
     */
    private ConcurrentHashMap<String, DataTask> dataTaskMap = new ConcurrentHashMap<>();

    /**
     * dataExport 数据导出实例缓存
     */
    private ConcurrentHashMap<String, DataExport> dataExportMap = new ConcurrentHashMap<>();

    /**
     * 已处理(消费)数据统计
     */
    private ConcurrentHashMap<String, AtomicLong> collectDataWriteCount = new ConcurrentHashMap<>();

    /**
     * 任务数据输入器异常记录
     */
    private ConcurrentHashMap<String, Exception> failedDataInputHandelExecIds = new ConcurrentHashMap<>();

    /**
     * 任务写文件异常记录
     */
    private ConcurrentHashMap<String, Exception> failedWriteFileIds = new ConcurrentHashMap<>();

    /**
     * flus、关闭逻辑执行中的任务id
     */
    private ConcurrentHashMap<String, Long> flushCloseFileRuningTasks = new ConcurrentHashMap<>();

    /**
     * 任务结束id队列
     */
    private Queue<String> taskDataInputFinishQueue = new ConcurrentLinkedQueue();

    /**
     * 任务结束Future
     */
    private ConcurrentHashMap<String,CompletableFuture> completFutures = new ConcurrentHashMap<>();

    /**
     * 服务关闭标记
     */
    private volatile boolean stop = false;

    /**
     * 开启任务状态信息输出
     */
    private boolean dataTaskStatusPrint = true;
    /**
     * 任务状态信息输出间隔，毫秒
     */
    private int dataTaskStatusPrintInterval = 10 * 1000;

    public DefaultDataTaskExecutor(
            int dataInputHandleThreadPoolSize,
            int dataInputHandleQueueSize,
            int fileBatchWriteSize,
            int fileWriteThreadPoolSize,
            int fileWriteThreadPoolQueueSize
    ) {
        this(
                dataInputHandleThreadPoolSize,
                dataInputHandleQueueSize,
                fileBatchWriteSize,
                fileWriteThreadPoolSize,
                fileWriteThreadPoolQueueSize,
                true,
                10 * 1000
        );
    }

    public DefaultDataTaskExecutor(
            int dataInputHandleThreadPoolSize,
            int dataInputHandleQueueSize,
            int fileBatchWriteSize,
            int fileWriteThreadPoolSize,
            int fileWriteThreadPoolQueueSize,
            boolean dataTaskStatusPrint,
            int dataTaskStatusPrintInterval
    ) {

        this.dataInputHandleThreadPoolSize = dataInputHandleThreadPoolSize;
        this.dataInputHandleQueueSize = dataInputHandleQueueSize;
        this.fileBatchWriteSize = fileBatchWriteSize;
        this.fileWriteThreadPoolSize = fileWriteThreadPoolSize;
        this.fileWriteThreadPoolQueueSize = fileWriteThreadPoolQueueSize;
        this.dataTaskStatusPrint = dataTaskStatusPrint;
        this.dataTaskStatusPrintInterval = dataTaskStatusPrintInterval;

        threadPoolCollectInputDataHandler = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.dataInputHandleThreadPoolSize);//数据处理器处理线程
        dataTaskContext = new DefaultDataTaskContext(this.dataInputHandleQueueSize);

        String[] nodes = new String[this.fileWriteThreadPoolSize];
        for (int i = 0; i < this.fileWriteThreadPoolSize; i++) {
            ThreadPoolExecutor fileWriteThreadPool = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(this.fileWriteThreadPoolQueueSize));
            fileWriteThreadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    if (!executor.isShutdown()) {
                        try {
                            executor.getQueue().put(r);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
            fileWriteThreadPoolMap.put(Integer.toString(i), fileWriteThreadPool);
            nodes[i] = Integer.toString(i);
        }

        fileWriteThreadPoolConsistentHashing = new ConsistentHashingWithoutVirtualNode(nodes, 5);
        threadPoolFileWriteFlushClose = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.fileWriteThreadPoolSize);//写文件刷入、关闭文件
        start();
    }

    private DataTaskExecutor start() {

        //检测写文件结束线程 调整为通知执行
        //writeFinishTask();

        //collectData消费线程
        collectInputDataProcessTask();

        if(dataTaskStatusPrint) {
            //任务状态信息输出打印
            dataTaskStatusPrint();
        }


        return this;
    }

    /**
     * 任务状态信息输出打印
     */
    private void dataTaskStatusPrint() {
        dataTaskInfoPrintThread = new Thread("dataTaskStatusPrint-" + this.hashCode()){
            @Override
            public void run() {
                while(!stop) {
                    logger.info("任务input队列待消费数: " + dataTaskContext.collectCount());
                    Enumeration<String> taskKeys = dataTaskMap.keys();
                    while (taskKeys.hasMoreElements()) {
                        String id = taskKeys.nextElement();
                        long collectDataCount = dataTaskContext.getCollectDataCount(id);
                        long writeCollectDataCount = collectDataWriteCount.get(id).get();
                        logger.info("任务ID: " + id + " input队列待消费数据量: " + collectDataCount + " 已处理(消费)数据量: " + writeCollectDataCount);
                    }

                    Set<java.util.Map.Entry<String, ThreadPoolExecutor>> sets = fileWriteThreadPoolMap.entrySet();
                    sets.forEach(new Consumer<Map.Entry<String, ThreadPoolExecutor>>() {
                        @Override
                        public void accept(Map.Entry<String, ThreadPoolExecutor> entry) {
                            logger.info("写文件线程[:" + entry.getKey() + "] 待处理任务数:" + entry.getValue().getQueue().size());
                        }
                    });

                    Set<java.util.Map.Entry<String, Long>> flushCloseFileRuningTasksInfo = flushCloseFileRuningTasks.entrySet();
                    flushCloseFileRuningTasksInfo.forEach(new Consumer<Map.Entry<String, Long>>() {
                        @Override
                        public void accept(Map.Entry<String, Long> entry) {
                            logger.info("文件flush\\close任务ID[:" + entry.getKey() + "] 已执行时间:" + ((System.currentTimeMillis()-entry.getValue())/1000.0) + "s");
                        }
                    });

                    logger.info("");
                    try {
                        Thread.sleep(dataTaskStatusPrintInterval);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
        dataTaskInfoPrintThread.start();
    }

    /**
     * 检测写文件结束任务，写文件结束后flush、close文件、压缩、上传文件
     */
    private void flushCloseFile(String id) {

        try {
                if(null != flushCloseFileRuningTasks.get(id)) {
                    return;
                }

                long collectDataCount = dataTaskContext.getCollectDataCount(id);
                long writeCollectDataCount = collectDataWriteCount.get(id).get();
                if (
                        dataTaskContext.isTaskCollectFinish(id)//数据input收集已结束
                        &&
                        writeCollectDataCount >= collectDataCount//已执行写入数据总量 等于 收集数据总量
                ) {
                    logger.info("taskId {} flushCloseFile start",id);
                    flushCloseFileRuningTasks.put(id,System.currentTimeMillis());//标记开始执行的任务id
                    try {
                        //写文件关闭操作耗时长，使用线程池处理
                        threadPoolFileWriteFlushClose.submit(new Runnable() {
                            @Override
                            public void run() {

                                DataTask dataTask = dataTaskMap.get(id);
                                DataTaskResult dataTaskResult = new DataTaskResult();

                                Exception writeFileException = failedWriteFileIds.get(id);
                                Exception dataInputHandlerException = failedDataInputHandelExecIds.get(id);

                                DataExport dataExport = dataExportMap.get(id);

                                //dataInputHandler 执行异常
                                if(null != dataInputHandlerException) {
                                    dataTaskResult.setSuccess(false);
                                    dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.DATA_INPUT);
                                    dataTaskResult.setError(dataInputHandlerException);
                                    try {
                                        dataExport.close();
                                    } catch (Exception e) {
                                        logger.error("DataTask id:{} dataExport关闭异常", id, e);
                                    }
                                } else if(null != writeFileException) {
                                    //写文件异常
                                    dataTaskResult.setSuccess(false);
                                    dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.WRITE_FILE);
                                    dataTaskResult.setError(writeFileException);
                                    try {
                                        dataExport.close();
                                    } catch (Exception e) {
                                        logger.error("DataTask id:{} dataExport关闭异常", id, e);
                                    }
                                } else {
                                    //正常情况，flus、关闭文件后上传文件
                                    boolean continueAfterProcess = true;
                                    try {
                                        //flush、关闭文件
                                        long ts = System.currentTimeMillis();
                                        dataExport.close();
                                        //dataTaskResult.setWriteFileResult(dataTask.getFileDir() + File.separator + dataTask.getFileName());//currentFile
                                        dataTaskResult.setWriteFileResult(dataExport.files());
                                        logger.info("taskId:" + id + " write file finish 耗时:" + ((System.currentTimeMillis()-ts)/1000.0));
                                    } catch (Exception e) {
                                        dataTaskResult.setSuccess(false);
                                        dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.FLUSH_FILE);
                                        dataTaskResult.setError(e);
                                        logger.error("DataTask id:{} dataExport关闭异常", id, e);
                                        continueAfterProcess = false;//flush、关闭文件异常，不继续上传文件
                                    }

                                    //压缩文件、写完文件上传
                                    if(continueAfterProcess) {

                                        //压缩文件
                                        if(dataTask.isCompressExportFile()) {
                                            File zipFile = new File(dataTask.getFileDir() + File.separator + dataExport.getFileNameNoSuffix() + ".zip");
                                            try {
                                                long ts = System.currentTimeMillis();
                                                ZipUtil.zipFiles(
                                                        dataTaskResult.getWriteFileResult(),
                                                        zipFile,
                                                        dataTask.isCompressAfterDeleteFile());
                                                dataTaskResult.setCompressedFile(zipFile);
                                                logger.info("taskId:" + id + " compress file finish 耗时:" + ((System.currentTimeMillis()-ts)/1000.0));
                                            } catch (Exception e) {
                                                dataTaskResult.setSuccess(false);
                                                dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.COMPRESS_FILE);
                                                dataTaskResult.setError(e);
                                                logger.error("DataTask id:{} 压缩文件异常", id, e);
                                                continueAfterProcess = false;//压缩文件异常，不继续上传文件
                                            }
                                        }
                                        //上传
                                        if(continueAfterProcess && dataTask.isUpload()) {
                                            //待上传文件列表
                                            List<File> fileList = null;
                                            if(dataTask.isCompressExportFile()) {
                                                fileList = Arrays.asList(dataTaskResult.getCompressedFile());
                                            } else {
                                                fileList = dataTaskResult.getWriteFileResult();
                                            }
                                            //已上传完毕列表
                                            List<UploadResult> uploadFileResultList = new ArrayList<>(fileList.size());
                                            try {
                                                if(DestType.HUAWEI_OBS.equals(dataTask.getUploadDestType())) {

                                                    long ts = System.currentTimeMillis();
                                                    for(int i = 0; i < fileList.size(); i++) {
                                                        /**
                                                         DataUpload dataUpload = new DefaultDataUpload(
                                                         dataTask.getFileDir(),
                                                         dataTask.getFileName(),
                                                         dataTask.getUploadDestType(),
                                                         dataTask.getUploadConfig());
                                                         String uploadFile = dataUpload.uploadFile();
                                                         **/
                                                        File file = fileList.get(i);
                                                        /**
                                                        UploadResult uploadFileRst = DataUpload.uploadFile(
                                                                file,
                                                                DestType.HUAWEI_OBS,
                                                                new OBSUploadConfig(
                                                                        ((OBSUploadConfig)dataTask.getUploadConfig()).getUrl(),
                                                                        ((OBSUploadConfig)dataTask.getUploadConfig()).getAppid(),
                                                                        ((OBSUploadConfig)dataTask.getUploadConfig()).getSecret(),
                                                                        ((OBSUploadConfig)dataTask.getUploadConfig()).getParentDirectory())
                                                        );**/
                                                        UploadResult uploadFileRst = DataUpload.uploadFile(
                                                                file,
                                                                dataTask.getUploadDestType(),
                                                                dataTask.getUploadConfig()
                                                        );
                                                        uploadFileResultList.add(uploadFileRst);//成功或失败都存入返回值中
                                                        if( !uploadFileRst.isSuccess() ) { //当前文件上传失败，抛出异常
                                                            throw uploadFileRst.getErr();
                                                        }
                                                    }
                                                    logger.info("taskId:" + id + " upload file finish 耗时:" + ((System.currentTimeMillis()-ts)/1000.0));
                                                } else {
                                                    dataTaskResult.setSuccess(false);
                                                    dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.UPLOAD_FILE);
                                                    dataTaskResult.setError(new FileUploadException("不支持的上传类型:" + dataTask.getUploadDestType()));
                                                }
                                            } catch (Throwable e) {
                                                //TODO 文件部分上传成功部分失败处理

                                                dataTaskResult.setSuccess(false);
                                                dataTaskResult.setErrorType(DataTaskResult.DataTaskErrorType.UPLOAD_FILE);
                                                dataTaskResult.setError(e);
                                                logger.error("DataTask id:{} 文件总数:{} 成功数:{} 未处理数:{} 上传文件异常",
                                                        id,
                                                        fileList.size(),
                                                        uploadFileResultList.size() - 1,
                                                        fileList.size() - uploadFileResultList.size(),
                                                        e);
                                            }
                                            dataTaskResult.setUploadFileResult(uploadFileResultList);
                                        }

                                    }

                                }

                                //任务结束后自动清理文件
                                if(dataTask.isTaskFinishAfterCleanFiles()) {
                                    dataTaskResult.deleteFiles();
                                }

                                logger.info("taskId {} flushCloseFile finish",id);
                                completFutures.get(id).complete(dataTaskResult);

                                //任务完成清理缓存信息
                                finishDataTaskClean(id);
                                logger.info("taskId {} complete",id);
                            }
                        });
                        //flushCloseFileRuningTasks.put(id,System.currentTimeMillis());//标记开始执行的任务id
                        //TODO delete taskDataInputFinishQueue
                        taskDataInputFinishQueue.remove(id);//从queue中删除任务id
                    } catch (Exception e) {
                        logger.error("DataTask 写文件关闭操作任务提交失败", e);
                    }

                }
        } catch (Exception e) {
            logger.error("DataTask 检测写文件结束任务异常", e);
        }

    }

    /**
     * collectInputData消费线程，消费输入器写入、业务灌入的数据写文件
     */
    private void collectInputDataProcessTask() {


        collectInputDataProcessThread = new Thread("collectInputDataProcessTask-" + this.hashCode()){
            @Override
            public void run() {

                while(!stop) {

                    try {

                        if(dataTaskContext.collectCount() > 0) { //队列还有数据待处理
                            Map<String,List<Object>> collectDataMap = new HashMap<>();
                            //int batchSize = 5000;//批处理大小
                            CollectData data = null;
                            int batchCount = 0;//当次已处理数据大小
                            //TODO 从阻塞队列读取数据， 使用timeout获取数据，防止Input结束灌入数据后部分数据卡在阻塞
                            while( null != (data = dataTaskContext.getCollectData(100L)) ) {
                                dataTaskContext.collectCountDecr();
                                //System.out.println("id:" + data.getId() + " exportKey:" + data.getExportKey() + " getCollectKey:" + data.getCollectKey());
                                List<Object> collectDataList = collectDataMap.get(data.getCollectKey());
                                if(null == collectDataList) {
                                    collectDataList = new ArrayList<>();
                                    collectDataMap.put(data.getCollectKey(),collectDataList);
                                }
                                if(data.isCollection()) {
                                    List dataList = (List)data.getValue();
                                    collectDataList.addAll(dataList);
                                    batchCount = batchCount + dataList.size();
                                } else {
                                    collectDataList.add(data.getValue());
                                    batchCount = batchCount + 1;
                                }

                                if(batchCount >= fileBatchWriteSize) {
                                    break;
                                }

                            }

                            if(!collectDataMap.isEmpty()) {
                                Set<Map.Entry<String, List<Object>>> collectDataEntry = collectDataMap.entrySet();
                                collectDataEntry.forEach(new Consumer<Map.Entry<String, List<Object>>>() {
                                    @Override
                                    public void accept(Map.Entry<String, List<Object>> entry) {

                                        String[] idExportKey = CollectData.parseIdExportKey(entry.getKey());
                                        String id = idExportKey[0];
                                        String exportKey = idExportKey[1];
                                        String nodeKey = fileWriteThreadPoolConsistentHashing.getNode(id);


                                        //TODO
                                        //提交到线程池达到上限会阻塞,存在有的线程池繁忙阻塞影响其它线程池提交的情况
                                        fileWriteThreadPoolMap.get(nodeKey).submit(new Runnable() {
                                            @Override
                                            public void run() {

                                                DataTask dataTask = dataTaskMap.get(id);
                                                if(null != dataTask) {
                                                    List datas = entry.getValue();
                                                    if(null == failedWriteFileIds.get(id) && null == failedDataInputHandelExecIds.get(id)) {
                                                        DataExport dataExport = dataExportMap.get(id);
                                                        SheetConfig sheetConfig = dataTask.getSheetConfig(exportKey);
                                                        if(null != sheetConfig) {
                                                            try {
                                                                long ts = System.currentTimeMillis();
                                                                ExportWriteConfig writeConfig = null;
                                                                if(ExportType.EXCEL.equals(dataExport.exportType())) {
                                                                    writeConfig = new ExcelExportWriteConfig(
                                                                            sheetConfig.getSheetName(),
                                                                            sheetConfig.getHead(),
                                                                            sheetConfig.getExportClass(),
                                                                            datas);
                                                                }
                                                                if(null != writeConfig) {
                                                                    dataExport.write(writeConfig);
                                                                }

                                                                //System.out.println("taskId:" + id + " exportKey:" + exportKey + " write 耗时:" + ((System.currentTimeMillis()-ts)/1000.0));
                                                            } catch (Exception e) {
                                                                failedWriteFileIds.put(id,e);//记录写文件异常taskid
                                                                try {
                                                                    dataExport.close();//关闭写文件异常的task
                                                                } catch (Exception e2) {
                                                                    logger.error("task id:{} 关闭写文件流异常" ,id ,e);
                                                                }
                                                                logger.warn("task id:{} 写文件异常" ,id ,e);
                                                            }
                                                        } else {
                                                            //无法识别exportKey，数据丢弃不处理
                                                            logger.warn("task id:{} exportKey:{} 不存在" ,id , exportKey);
                                                        }
                                                    }
                                                    collectDataWriteCount.get(id).addAndGet(datas.size());//+处理数据数量
                                                    //TODO 调整为数据消费写入完毕通知执行
                                                    //TODO writeFinishTask(taskId)

                                                    //已执行写入数据总量 等于 收集数据总量
                                                    //TODO 可能存在数据已消费，业务应用还未调用collectDataFinish情况，需子collectDataFinish中加入flushCloseFile调用逻辑
                                                    if(
                                                        dataTaskContext.isTaskCollectFinish(id)//数据input收集已结束
                                                        &&
                                                        collectDataWriteCount.get(id).get() >= dataTaskContext.getCollectDataCount(id)
                                                    ) {
                                                        //flush\close file
                                                        flushCloseFile(id);//队列消费时判断是否满足条件执行 后续处理
                                                        //collectDataWrite Finish
                                                    }
                                                } else {
                                                    //无法查询到任务id，丢弃数据不处理
                                                    logger.warn("task id:{} 不存在,丢弃数据不处理 ", id);
                                                }

                                            }
                                        });

                                    }
                                });
                            }
                        } else {
                            //队列暂无数据需要处理
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }


                    } catch (Exception e) {
                        logger.error("CollectDataProcess 任务执行异常", e);
                    }

                }

            }
        };
        collectInputDataProcessThread.start();
    }


    /**
     * 提交任务
     * @param task
     * @return
     * @throws Exception
     */
    @Override
    public Future<DataTaskResult> submit(DataTask task) throws Exception {

        //任务参数校验
        task.check();
        logger.info("taskId {} submit",task.getId());
        File dir = new File(task.getFileDir());
        if(!dir.exists()) {
            dir.mkdirs();
        }

        //初始化文件导出处理类
        DataExport dataExport = new DefaultDataExport(
                task.getExportType(),
                task.getFileDir(),
                task.getFileName(),
                task.isAutoMutilFile(),
                task.getSingleFileMaxLine(),
                task.isAutoMutilSheet(),
                task.getSingleSheetMaxLine(),
                task.getWriteHandlers()
                );
        dataExportMap.put(task.getId(), dataExport);

        dataTaskMap.put(task.getId(), task);//缓存task配置
        collectDataWriteCount.put(task.getId(),new AtomicLong(0));//初始化writeCount
        dataTaskContext.initCollectDataCount(task.getId());//初始化collectDataCount

        dataTaskContext.setTaskCollectFinish(task.getId(), false);//标记任务执行未结束

        CompletableFuture<DataTaskResult> f = new CompletableFuture();
        completFutures.put(task.getId(), f);


        //使用数据输入器
        if(task.isUseDataInputHandler()) {
            if(task.isDataInputHandlerSync()) {//input handle同步执行
                try {
                    task.getDataInputHandler().handler(dataTaskContext);//执行DataHandler
                } catch (Exception e) {
                    logger.error("taskId {} dataInputHandler 执行异常",task.getId(),e);
                    //标记执行异常
                    failedDataInputHandelExecIds.put(task.getId(),e);
                }
                //数据输入任务结束
                collectDataFinish(task.getId());
            } else {//input handle异步执行
                //执行数据输入器
                Future<String> future = threadPoolCollectInputDataHandler.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.getDataInputHandler().handler(dataTaskContext);//执行DataHandler
                        } catch (Exception e) {
                            logger.error("taskId {} dataInputHandler 执行异常",task.getId(),e);
                            //标记执行异常
                            failedDataInputHandelExecIds.put(task.getId(),e);
                        }
                        //数据输入任务结束
                        collectDataFinish(task.getId());

                    }
                },task.getId());
            }
        } else {
            //业务自行输入数据接口
            task.setCollecter(this);
        }



        return f;

    }


    /**
     * task 执行结束，清理缓存数据
     * @param taskId
     */
    private void finishDataTaskClean(String taskId) {

        logger.info("taskId {} finishDataTaskClean",taskId);

        //dataTask缓存
        dataTaskMap.remove(taskId);
        //dataExport
        dataExportMap.remove(taskId);
        //collectDataWriteCount 已处理写入数据计数器
        collectDataWriteCount.remove(taskId);
        //collectData 数据收集数量计数器(待写入数据)
        dataTaskContext.cleanCollectDataCount(taskId);
        //data input执行异常标记
        failedDataInputHandelExecIds.remove(taskId);
        //写文件异常标记
        failedWriteFileIds.remove(taskId);
        //future 任务执行完成future缓存
        completFutures.remove(taskId);
        //flush、关闭中任务缓存
        flushCloseFileRuningTasks.remove(taskId);


    }

    @Override
    void collectData(String id,boolean isCollection, String exportKey, Object data) throws Exception{

        if(dataTaskContext.isTaskCollectFinish(id)) {//任务已结束
            return ;
        }

        DataTask dataTask = dataTaskMap.get(id);

        //不使用数据输入器
        if(!dataTask.isUseDataInputHandler()) {
            if(dataTask.isSelfServiceDataInputUseQueue()) { //使用队列
                //数据写入队列
                try {
                    dataTaskContext.collectData(id,isCollection,exportKey,data);// //TODO 异常处理
                } catch (Exception e) {
                    logger.error("taskId {} 使用队列自行灌入数据 入队列执行异常",id,e);
                    //标记执行异常
                    failedDataInputHandelExecIds.put(id,e);
                    //数据输入任务结束
                    collectDataFinish(id);
                    throw e;
                }
            } else {  //不使用队列 直接写文件

                DataExport dataExport = dataExportMap.get(id);
                SheetConfig sheetConfig = dataTask.getSheetConfig(exportKey);

                ExportWriteConfig writeConfig = null;
                if(ExportType.EXCEL.equals(dataTask.getExportType())) {
                    writeConfig = new ExcelExportWriteConfig(
                            sheetConfig.getSheetName(),
                            sheetConfig.getHead(),
                            sheetConfig.getExportClass(),
                            isCollection ? (List)data : Arrays.asList(data));
                }
                if(null != writeConfig) {
                    try {
                        dataExport.write(writeConfig);// //TODO 异常处理
                    } catch (Exception e) {
                        logger.error("taskId {} 不使用队列自行灌入数据 写文件执行异常",id,e);
                        //标记执行异常
                        failedWriteFileIds.put(id,e);
                        //数据输入任务结束
                        collectDataFinish(id);
                        throw e;
                    }
                }
            }
        }


    }

    @Override
    void collectDataFinish(String id) {

        if(dataTaskContext.isTaskCollectFinish(id)) {//任务已结束
            return ;
        }
        logger.info("taskId {} collectDataFinish",id);
        dataTaskContext.setTaskCollectFinish(id, true);//标记任务执行结束id
        taskDataInputFinishQueue.offer(id);//任务执行结束id写入队列

        DataTask dataTask = dataTaskMap.get(id);

        //不使用数据输入器
        if(!dataTask.isUseDataInputHandler()) {
            //删除输入数据接口
            dataTask.setCollecter(null);
            //不使用队列
            if(!dataTask.isSelfServiceDataInputUseQueue()) {
                flushCloseFile(id);//不使用数据输入器 不使用队列 自行灌入数据 调用collectDataFinish后 执行后续处理
                return;
            }
        }

        //不使用数据输入器  使用队列
        //使用数据输入器
        //collectDataFinish 执行在队列数据消费完之后才执行的情况，collectDataFinish内触发执行后续处理
        if(
            dataTaskContext.isTaskCollectFinish(id)//数据input收集已结束
            &&
            collectDataWriteCount.get(id).get() >= dataTaskContext.getCollectDataCount(id)
        ) {
            //flush\close file
            flushCloseFile(id);//(不使用数据输入器  使用队列) ||（使用数据输入器）collectDataFinish 执行在队列数据消费完之后才执行的情况，collectDataFinish内触发执行后续处理
            //collectDataWrite Finish
        }


    }

    @Override
    public void close() {
        stop = true;
        //任务状态信息输出打印志输出线程
        if(null != dataTaskInfoPrintThread) {
            //try { dataTaskInfoPrintThread.interrupt(); } catch (Exception e) {logger.error(e.getMessage(),e);};
        }

        //数据输入线程池(执行inputHandle)
        if(null != threadPoolCollectInputDataHandler) {
            try { threadPoolCollectInputDataHandler.shutdown(); } catch (Exception e) {logger.error(e.getMessage(),e);};
        }

        //数据收集队列消费线程
        if(null != collectInputDataProcessThread) {
            //try { collectInputDataProcessThread.interrupt(); } catch (Exception e) {logger.error(e.getMessage(),e);};
        }

        //写文件线程池
        if(null != fileWriteThreadPoolMap) {
            Set<java.util.Map.Entry<String, ThreadPoolExecutor>> fileWriteThreadPools = fileWriteThreadPoolMap.entrySet();
            for (Map.Entry<String, ThreadPoolExecutor> tpe:fileWriteThreadPools) {
                try { tpe.getValue().shutdown(); } catch (Exception e) {logger.error(e.getMessage(),e);};
            }
        }

        //flush、close文件线程池
        if(null != threadPoolFileWriteFlushClose) {
            try { threadPoolFileWriteFlushClose.shutdown(); } catch (Exception e) {logger.error(e.getMessage(),e);};
        }


    }

}
