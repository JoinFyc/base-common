package com.mengxiang.base.datatask.upload.impl;

import com.mengxiang.base.datatask.model.UploadResult;
import com.mengxiang.base.datatask.upload.DataUpload;
import com.mengxiang.base.datatask.upload.DestType;
import com.mengxiang.base.datatask.upload.exception.FileUploadException;
import com.mengxiang.base.datatask.upload.provider.DataUploadProvider;
import com.mengxiang.base.datatask.upload.provider.OBSDataUploadProvider;
import com.mengxiang.base.datatask.upload.provider.OBSUploadConfig;
import com.mengxiang.base.datatask.upload.provider.UploadConfig;

import java.io.File;

public class DefaultDataUpload implements DataUpload {

    private DestType destType;
    private File sourceFile;
    private String sourceFileDir;
    private String sourceFileName;
    private UploadConfig uploadConfig;

    public DefaultDataUpload(
            String sourceFileDir,
            String sourceFileName,
            DestType destType,
            UploadConfig uploadConfig) {
        this.destType = destType;
        this.sourceFileDir = sourceFileDir;
        this.sourceFileName = sourceFileName;
        this.sourceFile = new File(this.sourceFileDir + File.separator + this.sourceFileName);
        this.uploadConfig = uploadConfig;
    }

    public DefaultDataUpload(
            File sourceFile,
            DestType destType,
            UploadConfig uploadConfig) {
        this.destType = destType;
        this.sourceFile = sourceFile;
        this.uploadConfig = uploadConfig;
    }

    @Override
    public UploadResult uploadFile() {
        UploadResult file = null;
        if(DestType.HUAWEI_OBS.equals(destType)) {
            if (null == uploadConfig) {
                throw new FileUploadException("DataTask obsConfig 参数不能为空");
            }
            OBSUploadConfig obsUploadConfig = (OBSUploadConfig)uploadConfig;
            DataUploadProvider dataUploadProvider =
                    new OBSDataUploadProvider(
                            obsUploadConfig.getUrl(),
                            obsUploadConfig.getAppid(),
                            obsUploadConfig.getSecret());
            file = dataUploadProvider.upload(
                    this.sourceFile.getAbsolutePath(),
                    obsUploadConfig.getParentDirectory(),
                    null == obsUploadConfig.getNewFileName() ? this.sourceFile.getName() : obsUploadConfig.getNewFileName(),
                    obsUploadConfig.getConnectTimeout(),
                    obsUploadConfig.getSocketTimeout()
                    );
        }
        return file;
    }


}
