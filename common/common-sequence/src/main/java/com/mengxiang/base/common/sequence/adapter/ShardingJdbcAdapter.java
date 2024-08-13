package com.mengxiang.base.common.sequence.adapter;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

public class ShardingJdbcAdapter implements DataSourceResolver {

    @Override
    public DataSource generateDataSource(DataSource dataSource) {
        Map<String, DataSource> dataSourceMap = ((ShardingDataSource) dataSource).getDataSourceMap();
        if (dataSourceMap.size() == 1) {
            Optional<DataSource> sourceOptional = dataSourceMap.values().stream().findFirst();
            return SequenceDataSourceGenerate.generateDataSource(sourceOptional.get());
        }
        return dataSource;
    }
}
