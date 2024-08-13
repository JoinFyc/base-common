package com.mengxiang.base.datatask.export.provider;

import com.mengxiang.base.datatask.export.ExportWriteConfig;

import java.util.List;
import java.util.Map;

public class ExcelExportWriteConfig implements ExportWriteConfig {

    private String sheetName;
    private Map<String,String> rowMap;
    private Class dataClass;
    private List data;

    public ExcelExportWriteConfig(String sheetName, Map<String, String> rowMap, Class dataClass, List data) {
        this.sheetName = sheetName;
        this.rowMap = rowMap;
        this.dataClass = dataClass;
        this.data = data;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Map<String, String> getRowMap() {
        return rowMap;
    }

    public void setRowMap(Map<String, String> rowMap) {
        this.rowMap = rowMap;
    }

    public Class getDataClass() {
        return dataClass;
    }

    public void setDataClass(Class dataClass) {
        this.dataClass = dataClass;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }
}
