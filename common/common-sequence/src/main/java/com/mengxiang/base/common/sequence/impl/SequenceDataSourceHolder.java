package com.mengxiang.base.common.sequence.impl;

import com.mengxiang.base.common.sequence.SequenceRange;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.model.Record;
import com.mengxiang.base.common.sequence.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 序列数据源包装类
 *
 * @author JoinFyc
 */
public class SequenceDataSourceHolder {

    private static final Logger logger = LoggerFactory
            .getLogger(SequenceConstant.SEQUENCE_LOG_NAME);

    /**
     * 数据源是否可用，默认可用
     */
    private volatile boolean isAvailable = true;

    /**
     * 真正的数据源
     */
    private final DataSource ds;

    /**
     * 非阻塞锁，用于控制只允许一个业务线程去重试；
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 上次重试时间
     */
    private volatile long lastRetryTime = 0;
    /**
     * 异常次数
     */
    private volatile int exceptionTimes = 0;
    /**
     * 第一次捕获异常的时间，单位毫秒
     */
    private volatile long firstExceptionTime = 0;
    /**
     * 重试故障db的时间间隔，默认值设为30s,单位毫秒
     */
    private final int retryBadDbInterval = 30000;

    /**
     * 单位时间段，默认为5分钟，用于统计时间段内某个db抛异常的次数，单位毫秒
     */
    private final int timeInterval = 300000;
    /**
     * 单位时间内允许异常的次数，如果超过这个值便将数据源置为不可用
     */
    private final int allowExceptionTimes = 20;

    /**
     * sequence表名，默认为sequence
     */
    private String tableName;

    /**
     * 格式：select value from sequence where name=?
     */
    private String selectSql;
    /**
     * 格式update table_name(default:sequence) set value=? ,gmt_modified=? where name=? and value=?
     */
    private String updateSql;
    /**
     * 格式: insert into table_name(default:sequence)(name,value,min_value,max_value,step,gmt_create,gmt_modified) values(?,?,?,?,?,?,?)
     */
    private String insertSql;

    /**
     * 设置常用的参数
     *
     * @param tableName 表名
     * @param selectSql select的sql语句
     * @param updateSql 更新语句
     * @param insertSql 插入语句
     */
    public void setParameters(String tableName, String selectSql, String updateSql,
                              String insertSql) {
        this.tableName = tableName;
        this.selectSql = selectSql;
        this.updateSql = updateSql;
        this.insertSql = insertSql;
    }

    /**
     * 构造函数
     *
     * @param ds
     */
    public SequenceDataSourceHolder(DataSource ds) {
        this.ds = ds;
    }

    public DataSource getDs() {
        return ds;
    }

    /**
     * 在可用的数据源上获取sequence段，如果发生异常，则进行统计
     *
     * @param sequenceName sequence名称
     * @return sequence段
     * @throws SequenceException
     */
    public SequenceRange tryGetSequenceRange(String sequenceName) throws SequenceException {


        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Boolean oldAutoCommit = null;
        //查询一条记录
        try {
            // 此次即将返回的sequenceRange的起始值
            long beginValue = -1;
            //此次即将返回的sequenceRange的结束值
            long endValue = -1;
            //新的需要更新的时间
            long newEffectiveTime = DateUtils.getMill(LocalDate.now().atTime(LocalTime.MIN));
            con = ds.getConnection();
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            stmt = con.prepareStatement(updateSql);
            stmt.setLong(1, newEffectiveTime);
            stmt.setLong(2, newEffectiveTime);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, sequenceName);
            if (stmt.executeUpdate() == 0) {
                throw new SequenceException("No sequence record in the table:" + tableName + ",please initialize it!");
            }
            stmt = con.prepareStatement(selectSql);
            stmt.setString(1, sequenceName);
            rs = stmt.executeQuery();
            //select name,value,code,min_value,max_value,effective_time,step from WORKER_SEQUENCE where name = ?sudo su - root
            if (!rs.next()) {
                throw new SequenceException("No sequence record in the table:" + tableName + ",please initialize it!");
            }
            beginValue = rs.getLong(1);
            int step = rs.getInt(2);
            long effective = rs.getLong(3);
            long min = rs.getLong(4);
            long max = rs.getLong(5);
            if (beginValue == min) {
                logger.warn("sequence was refreshed,sequence name " + sequenceName);
            }
            //本次range的结束
            endValue = beginValue + step - 1;
            //验证sequence段的起始值x
            if (endValue > max) {
                endValue = max - 1;
            }
            con.commit();
            return new SequenceRange(beginValue, endValue, effective);
        } catch (Exception e) {
            logger.error("refresh sequence range failed", e);
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                logger.warn("stmt rollback failed", ex);
            }
            throw new SequenceException(e);
        } finally {
            try {
                if (con != null && oldAutoCommit != null) {
                    con.setAutoCommit(oldAutoCommit);
                }
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                logger.error("ERROR ## close resources has an error", e);
            }
        }
    }

    public Map<String, Record> getAllSequenceRecordName(String selectSql, String nameColumnName, String codeColumnName, String minValueColumnName, String maxValueColumnName, String innerStepColumnName, String effectiveColumnName) throws SQLException {
        Map<String, Record> records = new HashMap<>(0);
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        //查询所有记录
        try {
            con = ds.getConnection();
            stmt = con.prepareStatement(selectSql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(nameColumnName);
                long min = rs.getLong(minValueColumnName);
                long max = rs.getLong(maxValueColumnName);
                int step = rs.getInt(innerStepColumnName);
                long effectiveTime = rs.getLong(effectiveColumnName);
                int code = rs.getInt(codeColumnName);
                if (records.get(name) == null) {
                    Record record = new Record();
                    record.setCode(code);
                    record.setEffectiveTime(effectiveTime);
                    record.setMax(max);
                    record.setMin(min);
                    record.setStep(step);
                    records.put(name, record);
                }
            }
        } catch (SQLException e) {
            logger.error("get all the sequence record failed!", e);
            throw e;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                logger.error("ERROR ## close resources has an error", e);
            }
        }
        return records;
    }

    /**
     * insert into table_name(default:sequence)(name,code,value,min_value,max_value,step,effective_time,gmt_create,gmt_modified) values(?,?,?,?,?,?,?,?,?)
     *
     * @param businessTypes
     * @throws SQLException
     */
    public void initBusinessRecord(List<Record> businessTypes) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        //查询所有记录
        try {
            con = ds.getConnection();
            for (Record record : businessTypes) {
                stmt = con.prepareStatement(insertSql);
                stmt.setString(1, record.getName());
                stmt.setInt(2, record.getCode());
                stmt.setLong(3, 1);
                stmt.setLong(4, record.getMin());
                stmt.setLong(5, record.getMax());
                stmt.setLong(6, record.getStep());
                stmt.setLong(7, record.getEffectiveTime());
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                stmt.setTimestamp(8, timestamp);
                stmt.setTimestamp(9, timestamp);
                try {
                    stmt.execute();
                } catch (SQLException ex) {
                    logger.debug(ex.getMessage());
                    logger.info("sequence " + record.getName() + " already exists");
                }
            }
        } catch (SQLException e) {
            logger.error("get all the sequence record failed!", e);
            throw e;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                logger.error("ERROR ## close resources has an error", e);
            }
        }
    }

    /**
     * update worker_sequence set step = ?, update_date= ? where name = ?
     *
     * @param refreshStepSql
     * @param businessName
     * @param newStep
     */
    public void refreshBusinessStep(String refreshStepSql, String businessName, int newStep) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        //查询所有记录
        try {
            con = ds.getConnection();
            stmt = con.prepareStatement(refreshStepSql);
            stmt.setInt(1, newStep);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, businessName);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("get all the sequence record failed!", e);
            throw e;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                logger.error("ERROR ## close resources has an error", e);
            }
        }
    }
}
