package com.mengxiang.base.datatask.upload.provider;

import com.mengxiang.base.datatask.model.UploadResult;

public interface DataUploadProvider {

    UploadResult upload(String uploadFilepath, String parentDirectory, String newFileName, int connectTimeout, int socketTimeout);
}
