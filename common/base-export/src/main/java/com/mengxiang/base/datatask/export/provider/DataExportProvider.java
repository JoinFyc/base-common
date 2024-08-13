package com.mengxiang.base.datatask.export.provider;

import com.mengxiang.base.datatask.export.ExportWriteConfig;

public interface DataExportProvider {

    void write(ExportWriteConfig config);

    void close();

}
