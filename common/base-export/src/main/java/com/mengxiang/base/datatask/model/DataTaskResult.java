package com.mengxiang.base.datatask.model;

import java.io.File;
import java.util.List;

public class DataTaskResult {

    public static enum DataTaskErrorType {

        DATA_INPUT,
        WRITE_FILE,
        FLUSH_FILE,
        COMPRESS_FILE,
        UPLOAD_FILE
        ;

    }

    /**
     * 任务执行结果
     */
    private boolean success = true;
    /**
     * 导出数据生成的文件列表
     */
    private List<File> writeFileResult;
    /**
     * 压缩文件生成的文件
     */
    private File compressedFile;
    /**
     * 上传文件返回的列表
     */
    private List<UploadResult> uploadFileResult;
    /**
     * 任务失败异常类型
     */
    private DataTaskErrorType errorType;
    /**
     * 任务失败的异常信息
     */
    private Throwable error;

    public DataTaskResult() {

    }

    /**
     * 删除生存的文件<br>
     *     1 导出数据生成的文件列表[writeFileResult]<br>
     *     2 压缩文件生成的文件[compressedFile]<br>
     */
    public void deleteFiles() {
        if(null != writeFileResult && !writeFileResult.isEmpty()) {
            for (File file : writeFileResult) {
                try {
                    if(file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {}
            }
        }
        try {
            if(compressedFile.exists()) {
                compressedFile.delete();
            }
        } catch (Exception e) {}

    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<File> getWriteFileResult() {
        return writeFileResult;
    }

    public void setWriteFileResult(List<File> writeFileResult) {
        this.writeFileResult = writeFileResult;
    }

    public List<UploadResult> getUploadFileResult() {
        return uploadFileResult;
    }

    public void setUploadFileResult(List<UploadResult> uploadFileResult) {
        this.uploadFileResult = uploadFileResult;
    }

    public DataTaskErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(DataTaskErrorType errorType) {
        this.errorType = errorType;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public File getCompressedFile() {
        return compressedFile;
    }

    public void setCompressedFile(File compressedFile) {
        this.compressedFile = compressedFile;
    }
}
