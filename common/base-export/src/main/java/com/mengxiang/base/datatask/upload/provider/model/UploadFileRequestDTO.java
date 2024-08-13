package com.mengxiang.base.datatask.upload.provider.model;

import java.io.File;

public class UploadFileRequestDTO {

    private File file;
    private String appId;
    private String parentDirectory;
    private String fileName;
    private String signature;

    /**
     * 上传连接超时时间 3秒
     */
    private int connectTimeout = 3 * 1000;
    /**
     * 上传超时时间 5分钟
     */
    private int socketTimeout = 5 * 60 * 1000;

    public UploadFileRequestDTO() {

    }

    public UploadFileRequestDTO(File file, String appId, String parentDirectory, String fileName, String signature) {
        this.file = file;
        this.appId = appId;
        this.parentDirectory = parentDirectory;
        this.fileName = fileName;
        this.signature = signature;
    }

    public UploadFileRequestDTO(File file, String appId, String parentDirectory, String fileName, String signature, int connectTimeout, int socketTimeout) {
        this.file = file;
        this.appId = appId;
        this.parentDirectory = parentDirectory;
        this.fileName = fileName;
        this.signature = signature;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}
