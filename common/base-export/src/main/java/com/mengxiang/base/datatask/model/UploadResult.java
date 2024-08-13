package com.mengxiang.base.datatask.model;

public class UploadResult<T> {

    private boolean success = false;
    private String fileUrl;
    private T originalIploadResult;
    private Throwable err;

    public UploadResult() {

    }

    public UploadResult(boolean success, String fileUrl, T originalIploadResult) {
        this.success = success;
        this.fileUrl = fileUrl;
        this.originalIploadResult = originalIploadResult;
    }

    public UploadResult(boolean success,T originalIploadResult, Throwable err) {
        this.success = success;
        this.originalIploadResult = originalIploadResult;
        this.err = err;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public T getOriginalIploadResult() {
        return originalIploadResult;
    }

    public void setOriginalIploadResult(T originalIploadResult) {
        this.originalIploadResult = originalIploadResult;
    }

    public Throwable getErr() {
        return err;
    }

    public void setErr(Throwable err) {
        this.err = err;
    }
}
