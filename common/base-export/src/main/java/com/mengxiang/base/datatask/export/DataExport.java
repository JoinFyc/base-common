package com.mengxiang.base.datatask.export;


import java.io.File;
import java.util.List;

public interface DataExport {

    String getFileNameNoSuffix();

    /**
     * 写文件
     */
    void write(ExportWriteConfig config);

    /**
     * 刷入数据到文件、关闭文件
     */
    void close();

    ExportType exportType();

    List<File> files();

}
