package com.mengxiang.base.datatask.export.impl;

import com.alibaba.excel.write.handler.WriteHandler;
import com.mengxiang.base.datatask.exception.TaskExecuteException;
import com.mengxiang.base.datatask.export.DataExport;
import com.mengxiang.base.datatask.export.ExportWriteConfig;
import com.mengxiang.base.datatask.export.ExportType;
import com.mengxiang.base.datatask.export.provider.DataExportProvider;
import com.mengxiang.base.datatask.export.provider.ExcelExport;
import com.mengxiang.base.datatask.export.provider.ExcelExportWriteConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultDataExport implements DataExport {


    final static String[] EXCEL_SUFFIXS = new String[]{".xls",".XLS",".xlsx",".XLSX"};



    private DataExportProvider dataExportProvider;

    private ExportType exportType = ExportType.EXCEL;
    private String dir;
    private String fileName;//原始文件名
    private String newFileName;//新文件名
    private boolean autoMutilFile = false;
    private int singleFileMaxLine = 1000000;
    private AtomicInteger currNum = new AtomicInteger(0);
    private AtomicLong currLineCount = new AtomicLong(0);
    private boolean autoMutilSheet = false;
    private int singleSheetMaxLine = 500000;
    private List<File> files = new ArrayList<>();
    private boolean currentIsClose = false;
    private List<WriteHandler> writeHandlers;
    public DefaultDataExport(
            ExportType exportType,
            String dir,
            String fileName,
            boolean autoMutilFile,
            boolean autoMutilSheet,
            List<WriteHandler> writeHandlers) {
        this(exportType,
                dir,
                fileName,
                autoMutilFile,
                1000000,
                autoMutilSheet,
                500000,
                writeHandlers);
    }

    public DefaultDataExport(
            ExportType exportType,
            String dir,
            String fileName,
            boolean autoMutilFile,
            int singleFileMaxLine,
            boolean autoMutilSheet,
            int singleSheetMaxLine,
            List<WriteHandler> writeHandlers) {

        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }

        this.exportType = exportType;
        this.dir = dir;
        this.fileName = fileName;
        this.autoMutilFile = autoMutilFile;
        this.singleFileMaxLine = singleFileMaxLine;
        this.autoMutilSheet = autoMutilSheet;
        this.singleSheetMaxLine = singleSheetMaxLine;
        this.writeHandlers = writeHandlers;
        if(ExportType.EXCEL.equals(exportType)) {
            this.fileName = fileNameNoSuffix(fileName,EXCEL_SUFFIXS);
            if(autoMutilFile) {
                this.newFileName = this.fileName + "-" + currNum.get() + ".xlsx";//加编号
            } else {
                this.newFileName = this.fileName + ".xlsx";
            }
            dataExportProvider = new ExcelExport(dir, this.newFileName, autoMutilSheet, singleSheetMaxLine, writeHandlers);

        } else {
            throw new TaskExecuteException("不支持的导出类型:" + exportType);
        }
    }

    @Override
    public String getFileNameNoSuffix() {
        return this.fileName;
    }

    @Override
    public void write(ExportWriteConfig config) {
        if(null != dataExportProvider) {
            //写文件之前创建新文件，防止创建空文件【写完创建新文件后关闭】
            if(ExportType.EXCEL.equals(exportType) && autoMutilFile) { //自动扩展新增多文件
                ExcelExportWriteConfig cfg = (ExcelExportWriteConfig)config;
                long nowCount = currLineCount.get();
                if(nowCount >= singleFileMaxLine) {
                    synchronized (this) {
                        nowCount = currLineCount.get();
                        if(nowCount >= singleFileMaxLine) {
                            //重新初始化当前文件行数
                            currLineCount = new AtomicLong(0);
                            //flush、关闭上一个文件
                            //dataExportProvider.close();
                            //files.add(new File(dir + File.separator + this.newFileName));
                            close();
                            //生成新文件名
                            this.newFileName = this.fileName + "-" + currNum.incrementAndGet() + ".xlsx";//加编号
                            //初始化文件写入类
                            dataExportProvider = new ExcelExport(dir, this.newFileName, autoMutilSheet, singleSheetMaxLine, writeHandlers);
                            currentIsClose = false;
                        }
                    }
                }
            }
            if(ExportType.EXCEL.equals(exportType) && autoMutilFile) { //自动扩展新增多文件
                ExcelExportWriteConfig cfg = (ExcelExportWriteConfig) config;
                currLineCount.addAndGet(cfg.getData().size());
            }
            dataExportProvider.write(config);

        }
    }

    /**
     * 刷入数据到文件、关闭文件
     */
    @Override
    public synchronized void close() {
        if(null != dataExportProvider && !currentIsClose) {
            dataExportProvider.close();
            //记录文件全路径
            files.add(new File(dir + File.separator + this.newFileName));
            currentIsClose = true;
        }
    }

    @Override
    public ExportType exportType() {
        return exportType;
    }

    @Override
    public List<File> files() {
        return files;
    }

    /**
     * 删除文件后缀，返回无后缀的文件名
     * @param fileName
     * @param suffixs
     * @return
     */
    public String fileNameNoSuffix(String fileName,String[] suffixs) {
        if(null == fileName) {
            return null;
        }
        String fileNameTrim = fileName.trim();
        for(int i = 0; i < suffixs.length ; i++) {

            int idxOf = fileNameTrim.lastIndexOf(suffixs[i]);
            if(-1 == idxOf) {
                continue;
            }
            int len = idxOf + suffixs[i].length();
            if(len == fileNameTrim.length()) {
                return fileNameTrim.substring(0,fileNameTrim.lastIndexOf(suffixs[i]));
            }
        }

        return fileNameTrim;
    }


}
