package com.mengxiang.base.datatask.model;

import java.util.Map;

public class SheetConfig {

    private String exportKey;
    private Class exportClass;
    private String sheetName;
    private Map<String,String> head;

    public SheetConfig() {

    }

    public SheetConfig(String exportKey, Class exportClass, String sheetName, Map<String, String> head) {
        this.exportKey = exportKey;
        this.exportClass = exportClass;
        this.sheetName = sheetName;
        this.head = head;
    }

    public String getExportKey() {
        return exportKey;
    }

    public void setExportKey(String exportKey) {
        this.exportKey = exportKey;
    }

    public Class getExportClass() {
        return exportClass;
    }

    public void setExportClass(Class exportClass) {
        this.exportClass = exportClass;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Map<String, String> getHead() {
        return head;
    }

    public void setHead(Map<String, String> head) {
        this.head = head;
    }
}
