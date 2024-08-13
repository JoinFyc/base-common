package com.mengxiang.base.datatask.model;

import java.util.Collections;

public class CollectData<T> {

    private static final String SPLIT_KEY = "#.#";

    private String collectKey;
    private String id;
    private String exportKey;
    private boolean collection;
    private T value;

    public CollectData() {

    }


    public static String[] parseIdExportKey(String collectKey) {
        return collectKey.split(SPLIT_KEY);
    }

    public CollectData(String id, boolean isCollection, String exportKey, T value) {
        this.id = id;
        this.exportKey = exportKey;
        this.value = value;
        this.collection = isCollection;
        this.collectKey = id + SPLIT_KEY + exportKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExportKey() {
        return exportKey;
    }

    public void setExportKey(String exportKey) {
        this.exportKey = exportKey;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public String getCollectKey() {
        return collectKey;
    }
}
