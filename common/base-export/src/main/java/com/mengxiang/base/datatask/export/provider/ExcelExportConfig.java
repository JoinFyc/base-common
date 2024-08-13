package com.mengxiang.base.datatask.export.provider;

import com.alibaba.excel.write.metadata.WriteSheet;
import com.mengxiang.base.datatask.util.BeanGetSet;

import java.util.List;

public class ExcelExportConfig {

    private WriteSheet writeSheet;
    private List<List<String>> heads;
    private List<String> fields;
    private BeanGetSet getSet;

    public ExcelExportConfig() {

    }

    public ExcelExportConfig(WriteSheet writeSheet, List<List<String>> heads, List<String> fields, BeanGetSet getSet) {
        this.writeSheet = writeSheet;
        this.heads = heads;
        this.fields = fields;
        this.getSet = getSet;
    }

    public WriteSheet getWriteSheet() {
        return writeSheet;
    }

    public void setWriteSheet(WriteSheet writeSheet) {
        this.writeSheet = writeSheet;
    }

    public List<List<String>> getHeads() {
        return heads;
    }

    public void setHeads(List<List<String>> heads) {
        this.heads = heads;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public BeanGetSet getGetSet() {
        return getSet;
    }

    public void setGetSet(BeanGetSet getSet) {
        this.getSet = getSet;
    }
}
