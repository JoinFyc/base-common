package com.mengxiang.base.datatask.exception;

public class TaskExecuteException extends RuntimeException {


    public TaskExecuteException(String message) {
        super(message);
    }


    public TaskExecuteException(String message, Throwable cause) {
        super(message, cause);
    }


    public TaskExecuteException(Throwable cause) {
        super(cause);
    }


    protected TaskExecuteException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
