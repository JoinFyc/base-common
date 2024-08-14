package com.mengxiang.base.common.sequence.adapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JoinFyc
 * @date 2021/3/14
 **/
public class SequenceDataSourceGenerate {

    private static final String hikariDataSourceClazz = "com.zaxxer.hikari.HikariDataSource";
    private static final String shardingDataSourceClazz = "org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource";
    static Map<String, DataSourceResolver> dataSourceResolverMap = new HashMap<String, DataSourceResolver>() {{
        put(hikariDataSourceClazz, new SequenceHikariAdapter());
        put(shardingDataSourceClazz, new ShardingJdbcAdapter());
    }};

    public static DataSource generateDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return null;
        }
        DataSourceResolver dataSourceResolver = dataSourceResolverMap.get(dataSource.getClass().getName());
        //假如无法另起一个连接，直接返回原始data source
        if (dataSourceResolver == null) {
            return dataSource;
        }
        return dataSourceResolver.generateDataSource(dataSource);
    }
}
