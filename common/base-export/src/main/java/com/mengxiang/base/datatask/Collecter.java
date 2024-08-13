package com.mengxiang.base.datatask;

abstract class Collecter {

    /**
     * 输入数据-灌数据
     * @param id
     * @param isCollection
     * @param exportKey
     * @param data
     * @throws Exception
     */
    abstract void collectData(String id,boolean isCollection, String exportKey, Object data) throws Exception;

    /**
     * 输入数据结束
     * @param id
     */
    abstract void collectDataFinish(String id);

}
