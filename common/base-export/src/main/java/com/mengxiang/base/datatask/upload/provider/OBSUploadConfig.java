package com.mengxiang.base.datatask.upload.provider;

public class OBSUploadConfig implements UploadConfig{

    private String url;
    private String appid;
    private String secret;
    private String parentDirectory;
    private String newFileName;

    /**
     * 上传连接超时时间 3秒
     */
    private int connectTimeout = 3 * 1000;
    /**
     * 上传超时时间 5分钟
     */
    private int socketTimeout = 5 * 60 * 1000;

    public OBSUploadConfig(String url, String appid, String secret, String parentDirectory, String newFileName) {
        this.url = url;
        this.appid = appid;
        this.secret = secret;
        this.parentDirectory = parentDirectory;
        this.newFileName = newFileName;
    }

    public OBSUploadConfig(String url, String appid, String secret, String parentDirectory) {
        this.url = url;
        this.appid = appid;
        this.secret = secret;
        this.parentDirectory = parentDirectory;
    }

    public OBSUploadConfig(String url, String appid, String secret, String parentDirectory, int connectTimeout, int socketTimeout) {
        this.url = url;
        this.appid = appid;
        this.secret = secret;
        this.parentDirectory = parentDirectory;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
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
