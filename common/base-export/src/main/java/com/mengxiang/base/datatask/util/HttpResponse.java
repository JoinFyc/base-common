package com.mengxiang.base.datatask.util;

public class HttpResponse<T> {
    private int code = 200;
    private String message = "ok";
    private String response;
    private T result;
    private boolean success = false;
    private String requestId;
    private String taskId;

    public HttpResponse() {

    }

    public HttpResponse(int code, String message, String response) {
        this.code = code;
        this.message = message;
        this.response = response;
    }

    public HttpResponse(int code, String message, String response, T result) {
        this.code = code;
        this.message = message;
        this.response = response;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", response='" + response + '\'' +
                ", result=" + result +
                ", success=" + success +
                ", requestId='" + requestId + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}