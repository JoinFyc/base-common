package com.mengxiang.base.export.sample;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.mengxiang.base.datatask.page.PageData;
import com.mengxiang.base.datatask.util.BeanGetSet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

public class ExcelUtil {



    /**
     * hutool poi
     * @param fileName
     * @param sheetName
     * @param rowMap
     * @param pages
     * @param <T>
     */
    public static<T> void hutoolExcelBatchExport(String fileName,String sheetName, Map<String,String> rowMap, PageData<T> pages) {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }

        // 通过工具类创建writer
        ExcelWriter writer = null;

        try{
            writer = cn.hutool.poi.excel.ExcelUtil.getBigWriter(fileName);

            // 默认的，未添加alias的属性也会写出，如果想只写出加了别名的字段，可以调用此方法排除之
            writer.setHeaderAlias(rowMap);
            writer.setOnlyAlias(true);
            //writer.setSheet("商家货款（总）");
            writer.renameSheet(sheetName);//sheet1
            writer.passCurrentRow();
            List<T> data = null;

            while( null != (data = pages.nextPage()) ) {
                //设置当前行号
                writer.setCurrentRow(writer.getRowCount());
                writer.write(data);
            }
        } finally {
            if(null != writer) {
                // 关闭writer，释放内存
                writer.close();
            }
        }


    }

    /**
     * easyexcel
     * @param fileName
     * @param sheetName
     * @param pages
     * @param <T>
     * @throws Exception
     */
    public static<T> void easyexcelBatchExport(String fileName,String sheetName, PageData<T> pages) throws Exception {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }
        com.alibaba.excel.ExcelWriter excelWriter = null;
        try {

            excelWriter = EasyExcel.write(fileName, pages.dataClass()).useDefaultStyle(false).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

            List<T> data = null;

            while( null != (data = pages.nextPage()) ) {
                excelWriter.write(data, writeSheet);
            }

        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    /**
     * easyexcel
     * @param fileName
     * @param sheetName
     * @param pages
     * @param <T>
     * @throws Exception
     */
    public static<T> void easyexcelBatchExportCustHead(String fileName,String sheetName, Map<String,String> rowMap, PageData<T> pages) throws Exception {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }
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
                    .head(heads)
                    //.includeColumnFiledNames(includeColumnFiledNames) //无效 nullpoint
                    .useDefaultStyle(false)
                    .build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

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

    /**
     * fastexcel
     * @param fileName
     * @param sheetName
     * @param rowMap
     * @param pages
     * @param <T>
     * @throws Exception
     */
    public static<T> void fastexcelBatchExport(String fileName,String sheetName, Map<String,String> rowMap, PageData<T> pages) throws Exception {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }
        FileOutputStream fileOutputStream = null;
        Workbook wb = null;

        try {
            fileOutputStream = new FileOutputStream(fileName);
            wb = new Workbook(fileOutputStream, "Application", "1.0");

            Worksheet ws = wb.newWorksheet(fileName);

            BeanGetSet getSet = new BeanGetSet(pages.dataClass());
            List<String> fieldNames = getSet.fieldNames();
            List<String> writeFieldNames = new ArrayList<>();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fname = fieldNames.get(i);
                String titleName = rowMap.get(fname);
                if(null != titleName) {
                    ws.value(0, i, titleName);//写表头
                    writeFieldNames.add(fname);//记录需要写excel的字段
                }
            }

            int idx = 1;

            List<T> data = null;

            while( null != (data = pages.nextPage()) ) {
                for(int i =0; i < data.size(); i++) {
                    Object o = data.get(i);
                    for (int j = 0; j < writeFieldNames.size(); j++ ) {
                        //ws.value(idx, j, getSet.get(o,writeFieldNames.get(j)).toString());

                        Object ov = getSet.get(o,writeFieldNames.get(j));//反射取值
                        if(null != ov) {
                            //写数据
                            if(o.getClass().equals(String.class)) {
                                ws.value(idx, j, (String)ov);
                            } else if(o.getClass().equals(Number.class)) {
                                ws.value(idx, j, (Number)ov);
                            } else if(o.getClass().equals(Boolean.class)) {
                                ws.value(idx, j, (Boolean)ov);
                            } else if(o.getClass().equals(Date.class)) {
                                ws.value(idx, j, (Date)ov);
                            } else if(o.getClass().equals(LocalDateTime.class)) {
                                ws.value(idx, j, (LocalDateTime)ov);
                            } else if(o.getClass().equals(LocalDate.class)) {
                                ws.value(idx, j, (LocalDate)ov);
                            } else if(o.getClass().equals(ZonedDateTime.class)) {
                                ws.value(idx, j, (ZonedDateTime)ov);
                            } else {
                                ws.value(idx, j, ov.toString());
                            }
                        }

                    }
                    idx++;
                }
                ws.flush();
            }
        } finally {
            //ws.flush();
            if(null != wb) {
                wb.finish();
            }
            if(null != fileOutputStream) {
                fileOutputStream.close();
            }
        }

    }

    /**
     * fastexcel bean取值使用Map
     * @param fileName
     * @param sheetName
     * @param rowMap
     * @param pages
     * @param <T>
     * @throws Exception
     */
    public static<T> void fastexcelBatchExport_Map(String fileName,String sheetName, Map<String,String> rowMap, PageData<T> pages) throws Exception {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }
        FileOutputStream fileOutputStream = null;
        Workbook wb = null;

        try {
            fileOutputStream = new FileOutputStream(fileName);
            wb = new Workbook(fileOutputStream, "Application", "1.0");

            Worksheet ws = wb.newWorksheet(fileName);

            BeanGetSet getSet = new BeanGetSet(pages.dataClass());
            List<String> fieldNames = getSet.fieldNames();
            List<String> writeFieldNames = new ArrayList<>();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fname = fieldNames.get(i);
                String titleName = rowMap.get(fname);
                if(null != titleName) {
                    ws.value(0, i, titleName);//写表头
                    writeFieldNames.add(fname);//记录需要写excel的字段
                }
            }

            int idx = 1;

            List<T> data = null;

            while( null != (data = pages.nextPage()) ) {
                for(int i =0; i < data.size(); i++) {
                    Object o = data.get(i);
                    Map<String, Object> dataMap = BeanUtil.beanToMap(o, new LinkedHashMap(), false, false);
                    for (int j = 0; j < writeFieldNames.size(); j++ ) {
                        //ws.value(idx, j, getSet.get(o,writeFieldNames.get(j)).toString());

                        Object ov = dataMap.get(writeFieldNames.get(j));//Map取值
                        if(null != ov) {
                            //写数据
                            if(o.getClass().equals(String.class)) {
                                ws.value(idx, j, (String)ov);
                            } else if(o.getClass().equals(Number.class)) {
                                ws.value(idx, j, (Number)ov);
                            } else if(o.getClass().equals(Boolean.class)) {
                                ws.value(idx, j, (Boolean)ov);
                            } else if(o.getClass().equals(Date.class)) {
                                ws.value(idx, j, (Date)ov);
                            } else if(o.getClass().equals(LocalDateTime.class)) {
                                ws.value(idx, j, (LocalDateTime)ov);
                            } else if(o.getClass().equals(LocalDate.class)) {
                                ws.value(idx, j, (LocalDate)ov);
                            } else if(o.getClass().equals(ZonedDateTime.class)) {
                                ws.value(idx, j, (ZonedDateTime)ov);
                            } else {
                                ws.value(idx, j, ov.toString());
                            }
                        }

                    }
                    idx++;
                }
                ws.flush();
            }
        } finally {
            //ws.flush();
            if(null != wb) {
                wb.finish();
            }
            if(null != fileOutputStream) {
                fileOutputStream.close();
            }
        }

    }


    /**
     * poi excel
     * @param fileName
     * @param sheetName
     * @param rowMap
     * @param pages
     * @param <T>
     * @throws Exception
     */
    public static<T> void poiexcelBatchExport(String fileName,String sheetName, Map<String,String> rowMap, PageData<T> pages) throws Exception {

        if(new File(fileName).exists()) {
            new File(fileName).delete();
        }
        FileOutputStream fileOutputStream = null;
        org.apache.poi.ss.usermodel.Workbook workbook = null;
        try {

            fileOutputStream = new FileOutputStream(fileName);

            // 1、创建一个工作簿 07
            workbook = new SXSSFWorkbook(100);
            // 2、创建一个工作表
            Sheet sheet = workbook.createSheet(fileName);
            // 3、创建一个行  （1,1）
            int row = 0;
            Row rowHead = sheet.createRow(row);

            BeanGetSet getSet = new BeanGetSet(pages.dataClass());
            List<String> fieldNames = getSet.fieldNames();
            List<String> writeFieldNames = new ArrayList<>();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fname = fieldNames.get(i);
                String titleName = rowMap.get(fname);
                if(null != titleName) {
                    //创建一个单元格
                    Cell cell = rowHead.createCell(i);
                    cell.setCellValue(titleName);
                    writeFieldNames.add(fname);//记录需要写excel的字段
                }
            }

            List<T> data = null;

            while( null != (data = pages.nextPage()) ) {
                for(int i =0; i < data.size(); i++) {
                    Object o = data.get(i);
                    row++;
                    Row row_ = sheet.createRow(row);
                    for (int j = 0; j < writeFieldNames.size(); j++ ) {
                        //ws.value(idx, j, getSet.get(o,writeFieldNames.get(j)).toString());

                        Object ov = getSet.get(o,writeFieldNames.get(j));//反射取值
                        if(null != ov) {
                            Cell cell = row_.createCell(j);
                            //写数据
                            if(o.getClass().equals(String.class)) {
                                cell.setCellValue((String)ov);
                            } else if(o.getClass().equals(Double.class)) {
                                cell.setCellValue((Double)ov);
                            } else if(o.getClass().equals(Boolean.class)) {
                                cell.setCellValue((Boolean)ov);
                            } else if(o.getClass().equals(Date.class)) {
                                cell.setCellValue((Date)ov);
                            } else if(o.getClass().equals(LocalDateTime.class)) {
                                cell.setCellValue((LocalDateTime)ov);
                            } else if(o.getClass().equals(LocalDate.class)) {
                                cell.setCellValue((LocalDate)ov);
                            } else if(o.getClass().equals(Calendar.class)) {
                                cell.setCellValue((Calendar)ov);
                            } else {
                                cell.setCellValue(ov.toString());
                            }
                        }

                    }

                }

            }

            // 输出
            workbook.write(fileOutputStream);

        } finally {

            if(null != fileOutputStream) {
                fileOutputStream.close();
            }

            if(null != workbook) {
                workbook.close();
            }

        }

    }


}
