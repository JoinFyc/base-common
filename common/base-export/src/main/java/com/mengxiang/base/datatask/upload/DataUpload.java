package com.mengxiang.base.datatask.upload;


import com.mengxiang.base.datatask.model.UploadResult;
import com.mengxiang.base.datatask.upload.exception.FileUploadException;
import com.mengxiang.base.datatask.upload.provider.DataUploadProvider;
import com.mengxiang.base.datatask.upload.provider.OBSDataUploadProvider;
import com.mengxiang.base.datatask.upload.provider.OBSUploadConfig;
import com.mengxiang.base.datatask.upload.provider.UploadConfig;

import java.io.File;

public interface DataUpload {

    static UploadResult uploadFile(File sourceFile, DestType destType, UploadConfig uploadConfig) {
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
                    sourceFile.getAbsolutePath(),
                    obsUploadConfig.getParentDirectory(),
                    null == obsUploadConfig.getNewFileName() ? sourceFile.getName() : obsUploadConfig.getNewFileName(),
                    obsUploadConfig.getConnectTimeout(),
                    obsUploadConfig.getSocketTimeout()
            );
        }
        return file;
    }

    /**
     * 上传文件
     */
    UploadResult uploadFile();

}
