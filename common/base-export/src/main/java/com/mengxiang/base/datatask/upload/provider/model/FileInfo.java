package com.mengxiang.base.datatask.upload.provider.model;

public class FileInfo {
    public FileInfo() {
    }

    public FileInfo(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    private String fileName;

    private String fileUrl;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}