package com.mengxiang.base.datatask.export.provider;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.mengxiang.base.datatask.export.ExportWriteConfig;
import com.mengxiang.base.datatask.util.BeanGetSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ExcelExport implements DataExportProvider {

    static Logger logger = LoggerFactory.getLogger(ExcelExport.class);
    private String dir;
    private String fileName;
    private String fileFullPath;
    private ConcurrentHashMap<String, ExcelExportConfig> exportExcelConfigs = new ConcurrentHashMap();
    private ConcurrentHashMap<String, AtomicInteger> sheetLineCount = new ConcurrentHashMap();
    private ConcurrentHashMap<String, AtomicInteger> sheetCurrNum = new ConcurrentHashMap();
    private ConcurrentHashMap<String, String> sheetNames = new ConcurrentHashMap();

    private com.alibaba.excel.ExcelWriter excelWriter = null;
    private boolean close = false;

    private boolean autoMutilSheet = false;
    private int singleSheetMaxLine = 500000;
    private List<WriteHandler> writeHandlers;

    public ExcelExport(String dir, String fileName, boolean autoMutilSheet, List<WriteHandler> writeHandlers){
        this(dir,fileName,autoMutilSheet,500000, writeHandlers);
    }

    public ExcelExport(String dir, String fileName, boolean autoMutilSheet, int singleSheetMaxLine, List<WriteHandler> writeHandlers) {

        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }

        this.autoMutilSheet = autoMutilSheet;
        this.singleSheetMaxLine = singleSheetMaxLine;
        this.writeHandlers = writeHandlers;

        fileFullPath = dir + File.separator + fileName;
        if(new File(fileFullPath).exists()) {
            new File(fileFullPath).delete();
        }

        ExcelWriterBuilder excelWriterBuilder = EasyExcel
                //.write(fileName, pages.dataClass())
                .write(fileFullPath)
                //.head(heads)
                //.includeColumnFiledNames(includeColumnFiledNames) //无效 nullpoint
                .useDefaultStyle(false);
                //.build();
        if(null != writeHandlers && !writeHandlers.isEmpty()) {
            writeHandlers.forEach(new Consumer<WriteHandler>() {
                @Override
                public void accept(WriteHandler writeHandler) {
                    if(null != writeHandler) {
                        excelWriterBuilder.registerWriteHandler(writeHandler);
                    }
                }
            });
        }
        excelWriter = excelWriterBuilder.build();
    }

    public void write(String sheetName0, Map<String,String> rowMap,Class dataClass, List data) {
        String sheetName = sheetName0;
        if(autoMutilSheet) {
            if(null == sheetNames.get(sheetName0)) {//初始化
                synchronized (this) {
                    if(null == sheetNames.get(sheetName0)) {
                        if(null == sheetCurrNum.get(sheetName0)) {
                            sheetCurrNum.put(sheetName0, new AtomicInteger(0));
                        }
                        sheetName = sheetName0 + "-" +  sheetCurrNum.get(sheetName0).get();
                        sheetNames.put(sheetName0,sheetName);
                        sheetLineCount.put(sheetName, new AtomicInteger(0));//初始化sheet行数
                    } else {
                        sheetName = sheetNames.get(sheetName0);
                    }
                }
            } else {
                sheetName = sheetNames.get(sheetName0);
            }

            //sheet行数达到上限
            AtomicInteger currSheetLineCount = sheetLineCount.get(sheetName);
            if(null != currSheetLineCount
                    && currSheetLineCount.get() >= singleSheetMaxLine) { //大于等于最大上限
                synchronized (this) {
                    currSheetLineCount = sheetLineCount.get(sheetName);
                    if(null != currSheetLineCount
                            && currSheetLineCount.get() >= singleSheetMaxLine) { //大于等于最大上限
                        sheetName = sheetName0 + "-" + sheetCurrNum.get(sheetName0).incrementAndGet();//sheetName + 1
                        sheetNames.put(sheetName0,sheetName);
                        sheetLineCount.put(sheetName, new AtomicInteger(0));//初始化sheet行数
                    } else {
                        sheetName = sheetNames.get(sheetName0);
                    }
                }
            }
        } else {
            if(null == sheetLineCount.get(sheetName)) {
                synchronized (this) {
                    if(null == sheetLineCount.get(sheetName)) {
                        sheetLineCount.put(sheetName, new AtomicInteger(0));//初始化sheet行数
                    }
                }
            }
        }

        //缓存sheet配置
        ExcelExportConfig excelExportConfig = exportExcelConfigs.get(sheetName);
        if(null == excelExportConfig) {
            synchronized (this) {
                excelExportConfig = exportExcelConfigs.get(sheetName);
                if(null == excelExportConfig) {
                    List<List<String>> heads = new ArrayList<List<String>>();
                    List<String> fields = new ArrayList<String>();

                    BeanGetSet getSet = new BeanGetSet(dataClass);

                    java.util.Set<Map.Entry<String, String>> entries = rowMap.entrySet();
                    for (Map.Entry<String, String> e : entries) {
                        heads.add(Arrays.asList(e.getValue()));
                        fields.add(e.getKey());
                    }
                    ExcelWriterSheetBuilder excelWriterSheetBuilder = EasyExcel.writerSheet(sheetName).head(heads);
                    if(null != writeHandlers && !writeHandlers.isEmpty()) {
                        writeHandlers.forEach(new Consumer<WriteHandler>() {
                            @Override
                            public void accept(WriteHandler writeHandler) {
                                if(null != writeHandler) {
                                    excelWriterSheetBuilder.registerWriteHandler(writeHandler);
                                }
                            }
                        });
                    }
                    WriteSheet writeSheet = excelWriterSheetBuilder.build();

                    excelExportConfig = new ExcelExportConfig(writeSheet,heads,fields,getSet);
                    //sheetLineCount.put(sheetName, new AtomicInteger(0));
                    //sheetCurrNum.put(sheetName0, new AtomicInteger(0));

                    exportExcelConfigs.put(sheetName, excelExportConfig);
                }
            }
        }

        List<List<Object>> rows = new ArrayList<>();
        ExcelExportConfig finalExcelExportConfig = excelExportConfig;
        data.forEach(new Consumer() {
            @Override
            public void accept(Object t) {
                List<Object> row = new ArrayList<Object>(finalExcelExportConfig.getFields().size());
                finalExcelExportConfig.getFields().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String f) {
                        Object ov = finalExcelExportConfig.getGetSet().get(t, f);//反射取值 支持Map
                        row.add(ov);
                    }
                });
                rows.add(row);
            }
        });
        if (rows.isEmpty()) {
            return;
        }
        excelWriter.write(rows, finalExcelExportConfig.getWriteSheet());
        sheetLineCount.get(sheetName).addAndGet(rows.size());
    }

    @Override
    public void close() {
        if (excelWriter != null && !close) {
            excelWriter.finish();
            close = true;
        }
    }

    @Override
    public void write(ExportWriteConfig config) {
        ExcelExportWriteConfig cfg = (ExcelExportWriteConfig)config;
        write(cfg.getSheetName(),cfg.getRowMap(),cfg.getDataClass(),cfg.getData());
    }


    /**
    public <T> void easyexcelBatchExportCustHead(String fileName, String sheetName, Map<String,String> rowMap, PageData<T> pages) throws Exception {


        com.alibaba.excel.ExcelWriter excelWriter = null;
        try {

            List<List<String>> heads = new ArrayList<List<String>>();
            List<String> fields = new ArrayList<String>();
            //Set<String> includeColumnFiledNames = new HashSet<String>();

            BeanGetSet getSet = new BeanGetSet(pages.dataClass());

            java.util.Set<Map.Entry<String, String>> entries = rowMap.entrySet();
            for (Map.Entry<String, String> e : entries) {
                heads.add(Arrays.asList(e.getValue()));
                fields.add(e.getKey());
                //includeColumnFiledNames.add(e.getValue());
            }

            excelWriter = EasyExcel
                    //.write(fileName, pages.dataClass())
                    .write(fileName)
                    //.head(heads)
                    //.includeColumnFiledNames(includeColumnFiledNames) //无效 nullpoint
                    .useDefaultStyle(false)
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).head(heads).build();

            List<T> data = null;

            while (null != (data = pages.nextPage())) {
                List<List<Object>> rows = new ArrayList<>();
                data.forEach(new Consumer<T>() {
                    @Override
                    public void accept(T t) {
                        List<Object> row = new ArrayList<Object>(fields.size());
                        fields.forEach(new Consumer<String>() {
                            @Override
                            public void accept(String f) {
                                Object ov = getSet.get(t, f);//反射取值
                                row.add(ov);
                            }
                        });
                        rows.add(row);
                    }
                });
                if (rows.isEmpty()) {
                    continue;
                }
                excelWriter.write(rows, writeSheet);
                //excelWriter.write(data, writeSheet);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }
    **/

}
