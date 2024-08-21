package com.mengxiang.base.common.log.log;

import java.io.Serializable;

/**
 * 日志来源
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/7/25 7:33 PM
 */
public class Source implements Serializable {

    private String cls;

    private String method;

    private String file;

    private Integer line;

    public Source() {}

    public Source(StackTraceElement e) {
        this.cls = e.getClassName();
        this.file = e.getFileName();
        this.method = e.getMethodName();
        this.line = e.getLineNumber();
    }

    public String getCls() {
        return cls;
    }

    void setCls(String cls) {
        this.cls = cls;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    public String getFile() {
        return file;
    }

    void setFile(String file) {
        this.file = file;
    }

    public Integer getLine() {
        return line;
    }

    void setLine(Integer line) {
        this.line = line;
    }
}