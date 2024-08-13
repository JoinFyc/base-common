package com.mengxiang.base.datatask.upload.provider.model;

public interface IErrorCode {
    Integer UNDEFINED_ERROR = 9999;
    Integer SYSTEM_ERROR = 9000;
    Integer ARGUMENT_ERROR = 9100;
    Integer SUCCESS = 0;

    Integer getCode();
}
