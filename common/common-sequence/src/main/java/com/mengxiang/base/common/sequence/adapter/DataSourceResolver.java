package com.mengxiang.base.common.sequence.adapter;

import javax.sql.DataSource;

/**
 * @author JoinFyc
 * @date 2021/3/14
 **/
public interface DataSourceResolver {
    /**
     * 根据原始的dataSource 解析生成一个全新的DataSource
     *
     * @param dataSource
     * @return
     */
    DataSource generateDataSource(DataSource dataSource);
}
