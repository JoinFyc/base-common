package com.mengxiang.base.datatask;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.excel.write.handler.WriteHandler;
import com.mengxiang.base.datatask.exception.TaskExecuteException;
import com.mengxiang.base.datatask.export.ExportType;
import com.mengxiang.base.datatask.handler.DataInputHandler;
import com.mengxiang.base.datatask.model.SheetConfig;
import com.mengxiang.base.datatask.upload.DestType;
import com.mengxiang.base.datatask.upload.provider.OBSUploadConfig;
import com.mengxiang.base.datatask.upload.provider.UploadConfig;

import java.util.*;

public class DataTask {

    private String id;
    private String fileDir;
    private String fileName;
    private ExportType exportType = ExportType.EXCEL;
    private Map<String, SheetConfig> sheet = new HashMap<>();
    boolean autoMutilFile = false;
    int singleFileMaxLine = 1000000;
    private boolean autoMutilSheet = false;
    private int singleSheetMaxLine = 500000;
    private DataInputHandler dataInputHandler;

    private boolean upload = false;
    private DestType uploadDestType = DestType.HUAWEI_OBS;
    private UploadConfig uploadConfig;

    private Collecter collecter;
    private boolean useDataInputHandler = false;
    private boolean dataInputHandlerSync = false;

    private boolean selfServiceDataInputUseQueue = true;

    private boolean compressExportFile = false;
    private boolean compressAfterDeleteFile = true;

    private boolean taskFinishAfterCleanFiles = false;

    private List<WriteHandler> writeHandlers = new ArrayList<>();

    /**
     * 参数校验
     */
    public void check() {

        if(StringUtils.isBlank(fileDir)) {
            throw new TaskExecuteException("DataTask fileDir 参数不能为空");
        }
        if(StringUtils.isBlank(fileName)) {
            throw new TaskExecuteException("DataTask fileName 参数不能为空");
        }
        if(null == exportType) {
            throw new TaskExecuteException("DataTask exportType 参数不能为空");
        }

        if(ExportType.EXCEL.equals(exportType)) {
            if(sheet.isEmpty()) {
                throw new TaskExecuteException("DataTask exportType为excel时 未使用sheet(...)方法配置表格属性");
            }
        }


        //使用数据输入器
        if(useDataInputHandler) {
            if(null == dataInputHandler) {
                throw new TaskExecuteException("DataTask useDataInputHandler:true dataHandler 参数不能为空");
            }
            if(StringUtils.isBlank(dataInputHandler.id())) {
                throw new TaskExecuteException("DataTask dataHandler.id() 参数不能为空");
            }
        } else {
            id = "task-" + UUID.randomUUID().toString().replaceAll("-","");
        }

        if(isUpload()) {
            if(null == getUploadConfig()) {
                throw new TaskExecuteException("DataTask 开启上传功能 uploadConfig参数不能为空");
            }
            if(DestType.HUAWEI_OBS.equals(getUploadDestType())) {
                if ( getUploadConfig() instanceof OBSUploadConfig ) {
                    OBSUploadConfig obsUploadConfig = ((OBSUploadConfig) getUploadConfig());
                    if(StringUtils.isBlank(obsUploadConfig.getUrl())) {
                        throw new TaskExecuteException("DataTask uploadConfig-OBSUploadConfig.url 参数不能为空");
                    }
                    if(StringUtils.isBlank(obsUploadConfig.getAppid())) {
                        throw new TaskExecuteException("DataTask uploadConfig-OBSUploadConfig.appid 参数不能为空");
                    }
                    if(StringUtils.isBlank(obsUploadConfig.getSecret())) {
                        throw new TaskExecuteException("DataTask uploadConfig-OBSUploadConfig.secret 参数不能为空");
                    }
                    if(StringUtils.isBlank(obsUploadConfig.getParentDirectory())) {
                        throw new TaskExecuteException("DataTask uploadConfig-OBSUploadConfig.parentDirectory 参数不能为空");
                    }
                } else {
                    throw new TaskExecuteException("DataTask 开启上传功能 DestType为HUAWEI_OBS uploadConfig必须为OBSUploadConfig");
                }
            }
        }

    }

    private DataTask() {
    }

    public static DataTask build() {
        return new DataTask();
    }

    /**
     * 导出类型为excel时 配置表格的属性
     * @param exportKey sheet表格别名
     * @param exportClass 导出数据dto的class
     * @param sheetName sheet表格名
     * @param head
     * @return
     */
    public DataTask sheet(String exportKey, Class exportClass, String sheetName, Map<String,String> head) {
        if(StringUtils.isBlank(exportKey)) {
            throw new TaskExecuteException("DataTask exportKey 参数不能为空");
        }
        if(null == exportClass) {
            throw new TaskExecuteException("DataTask exportClass 参数不能为空");
        }
        if(StringUtils.isBlank(sheetName)) {
            throw new TaskExecuteException("DataTask sheetName 参数不能为空");
        }
        if(null == head || head.isEmpty()) {
            throw new TaskExecuteException("DataTask head 参数不能为空");
        }

        sheet.put(exportKey,new SheetConfig(exportKey,exportClass,sheetName,head));
        return this;
    }

    /**
    public void collectData(String exportKey, Object data) {
        if(useDataInputHandler) {
            //使用数据输入器
            return;
        }
        if(null == data) {
            return;
        }
        collecter.collectData(id,false,exportKey,data);
    }
    **/

    /**
     * 不使用DataInputHandler(useDataInputHandler=false)自行灌入数据方法
     * @param exportKey sheet表格别名，与配置方法sheet(...)配置的别名一致
     * @param dataList 数据
     * @throws Exception 产生、抛出异常时将自动关闭任务
     */
    /**
     *
     * @param exportKey
     * @param dataList
     * @throws Exception
     */
    public void collectDataInput(String exportKey, List dataList) throws Exception{
        if(useDataInputHandler) {
            //使用数据输入器
            return;
        }
        if(null == dataList || dataList.isEmpty()) {
            return;
        }
        if(null == collecter) {
            return;
        }
        collecter.collectData(id,true,exportKey,dataList);
    }


    /**
     * 结束数据输入<br>
     *     不使用DataInputHandler(useDataInputHandler=false)自行灌入数据方法时必须调用collectDataInputFinish();
     */
    public void collectDataInputFinish() {
        if(useDataInputHandler) {
            //使用数据输入器
            return;
        }
        if(null == collecter) {
            return;
        }
        collecter.collectDataFinish(id);
    }


    /**
     * 导出文件路径
     * @param fileDir
     * @return
     */
    public DataTask fileDir(String fileDir) {
        this.fileDir = fileDir;
        return this;
    }

    /**
     * 导出文件名
     * @param fileName
     * @return
     */
    public DataTask fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 导出类型
     * @param exportType
     * @return
     */
    public DataTask exportType(ExportType exportType) {
        this.exportType = exportType;
        return this;
    }

    /**
     * 超行数上限自动新增sheet
     * @param autoMutilSheet
     * @return
     */
    public DataTask autoMutilSheet(boolean autoMutilSheet) {
        this.autoMutilSheet = autoMutilSheet;
        return this;
    }

    /**
     * 单sheet行数上限
     * @param singleSheetMaxLine
     * @return
     */
    public DataTask singleSheetMaxLine(int singleSheetMaxLine) {
        this.singleSheetMaxLine = singleSheetMaxLine;
        return this;
    }

    /**
     * 超行数上限自动新增导出文件
     * @param autoMutilFile
     * @return
     */
    public DataTask autoMutilFile(boolean autoMutilFile) {
        this.autoMutilFile = autoMutilFile;
        return this;
    }

    /**
     * 单文件行数上限
     * @param singleFileMaxLine
     * @return
     */
    public DataTask singleFileMaxLine(int singleFileMaxLine) {
        this.singleFileMaxLine = singleFileMaxLine;
        return this;
    }

    /**
     * 是否使用导出数据输入器<br>
     *     true 使用导出数据输入器，任务执行器将DataInputHandler使用内部线程池自动执行<br>
     *     false 不使用使用导出数据输入器，配合DataTask.collectData、DataTask.collectDataFinish自行灌入导出数据<br>
     * @param useDataInputHandler
     * @return
     */
    public DataTask useDataInputHandler(boolean useDataInputHandler) {
        this.useDataInputHandler = useDataInputHandler;
        return this;
    }

    /**
     * 导出数据输入器是否同步执行（需要配置开启useDataInputHandler=true使用）<br>
     * false 默认值，异步执行<br>
     * true 同步执行，提交任务submit将同步等待数据输入器执行完毕后才返回<br>
     * @param dataInputHandlerSync
     * @return
     */
    public DataTask dataInputHandlerSync(boolean dataInputHandlerSync) {
        this.dataInputHandlerSync = dataInputHandlerSync;
        return this;
    }

    /**
     * 不使用数据输入器自行灌入导出数据是否使用内置队列（需要配置关闭useDataInputHandler=false使用）<br>
     * @param selfServiceDataInputUseQueue
     * @return
     */
    public DataTask selfServiceDataInputUseQueue(boolean selfServiceDataInputUseQueue) {
        this.selfServiceDataInputUseQueue = selfServiceDataInputUseQueue;
        return this;
    }

    /**
     * 导出数据输入器
     * @param dataInputHandler
     * @return
     */
    public DataTask dataInputHandler(DataInputHandler dataInputHandler) {
        this.dataInputHandler = dataInputHandler;
        if(null != dataInputHandler) {
            //设置id
            this.id = dataInputHandler.genId();
        }
        return this;
    }

    /**
     * 是否开启上传功能
     * @param upload
     * @return
     */
    public DataTask upload(boolean upload) {
        this.upload = upload;
        return this;
    }

    /**
     * 上传目的地类型
     * @param uploadDestType
     * @return
     */
    public DataTask uploadDestType(DestType uploadDestType) {
        this.uploadDestType = uploadDestType;
        return this;
    }

    /**
     * 上传配置 <br>
     *     华为云OBS OBSUploadConfig <br>
     * @param uploadConfig
     * @return
     */
    public DataTask uploadConfig(UploadConfig uploadConfig) {
        this.uploadConfig = uploadConfig;
        return this;
    }


    /**
     * 是否生存压缩文件
     * @param compressExportFile
     * @return
     */
    public DataTask compressExportFile(boolean compressExportFile) {
        this.compressExportFile = compressExportFile;
        return this;
    }

    /**
     * 压缩文件后是否删除源文件
     * @param compressAfterDeleteFile
     * @return
     */
    public DataTask compressAfterDeleteFile(boolean compressAfterDeleteFile) {
        this.compressAfterDeleteFile = compressAfterDeleteFile;
        return this;
    }

    /**
     * 任务结束后清理文件(导出文件、压缩文件)
     * @param taskFinishAfterCleanFiles
     * @return
     */
    public DataTask taskFinishAfterCleanFiles(boolean taskFinishAfterCleanFiles) {
        this.taskFinishAfterCleanFiles = taskFinishAfterCleanFiles;
        return this;
    }

    /**
     * 添加自定义单元格处理器
     * @param writeHandler
     * @return
     */
    public DataTask addWriteHandler(WriteHandler writeHandler) {
        if(null != writeHandler) {
            if(null == writeHandlers) {
                writeHandlers = new ArrayList<>();
            }
            this.writeHandlers.add(writeHandler);
        }
        return this;
    }

    
    /**
     * 任务ID
     * @return
     */
    public String getId() {
        return id;
    }

    public String getFileDir() {
        return fileDir;
    }

    public String getFileName() {
        return fileName;
    }

    public ExportType getExportType() {
        return exportType;
    }

    /**
     * excel导出sheet配置
     * @param exportKey
     * @return
     */
    public SheetConfig getSheetConfig(String exportKey) {
        return sheet.get(exportKey);
    }


    public DataInputHandler getDataInputHandler() {
        return dataInputHandler;
    }

    public boolean isUpload() {
        return upload;
    }

    public DestType getUploadDestType() {
        return uploadDestType;
    }

    public UploadConfig getUploadConfig() {
        return uploadConfig;
    }

    public boolean isAutoMutilSheet() {
        return autoMutilSheet;
    }

    public int getSingleSheetMaxLine() {
        return singleSheetMaxLine;
    }

    public boolean isAutoMutilFile() {
        return autoMutilFile;
    }

    public int getSingleFileMaxLine() {
        return singleFileMaxLine;
    }

    void setCollecter(Collecter collecter) {
        this.collecter = collecter;
    }

    public boolean isUseDataInputHandler() {
        return useDataInputHandler;
    }

    public boolean isCompressExportFile() {
        return compressExportFile;
    }

    public boolean isCompressAfterDeleteFile() {
        return compressAfterDeleteFile;
    }

    public boolean isDataInputHandlerSync() {
        return dataInputHandlerSync;
    }

    public boolean isSelfServiceDataInputUseQueue() {
        return selfServiceDataInputUseQueue;
    }

    public boolean isTaskFinishAfterCleanFiles() {
        return taskFinishAfterCleanFiles;
    }

    List<WriteHandler> getWriteHandlers() {
        return writeHandlers;
    }


}
