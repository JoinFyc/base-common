package com.mengxiang.base.common.sequence.adapter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author JoinFyc
 * @date 2021/3/14
 **/
public class SequenceHikariAdapter implements DataSourceResolver {
    /**
     * spring.datasource.hikari.connection-test-query = SELECT 1
     * spring.datasource.hikari.data-source-properties.cachePrepStmts = true
     * spring.datasource.hikari.data-source-properties.leakDetectionThreshold = 4000
     * spring.datasource.hikari.data-source-properties.prepStmtCacheSize = 250
     * spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit = 2048
     * spring.datasource.hikari.data-source-properties.useServerPrepStmts = true
     * spring.datasource.hikari.maximumPoolSize = 50
     * spring.datasource.hikari.minimum-idle = 50
     * spring.datasource.hikari.pool-name = prodacctcore-hikariCP
     *
     * @param dataSource
     * @return
     */
    @Override
    public DataSource generateDataSource(DataSource dataSource) {
        HikariDataSource originDataSource = (HikariDataSource) dataSource;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setConnectionInitSql(originDataSource.getConnectionInitSql());
        hikariConfig.setLeakDetectionThreshold(originDataSource.getLeakDetectionThreshold());
        hikariConfig.setPassword(originDataSource.getPassword());
        hikariConfig.setPoolName("sequence-hikari-pool");
        hikariConfig.setUsername(originDataSource.getUsername());
        hikariConfig.setDriverClassName(originDataSource.getDriverClassName());
        hikariConfig.setJdbcUrl(originDataSource.getJdbcUrl());
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setMinimumIdle(1);
        return new HikariDataSource(hikariConfig);
    }
}
