package com.mengxiang.base.datatask.page;

import java.util.List;

/**
 * 分页数据接口
 * @param <T>
 */
public interface PageData<T> {
    /**
     * 下一页数据，返回null表示分页结束
     * @return
     */
    List<T> nextPage();
    /**
     * 数据类的类型
     * @return
     */
    Class dataClass();

    int maxPage();

    int currPage();

}
