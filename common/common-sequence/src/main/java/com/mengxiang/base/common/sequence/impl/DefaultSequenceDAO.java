package com.mengxiang.base.common.sequence.impl;

import com.mengxiang.base.common.sequence.SequenceRange;
import com.mengxiang.base.common.sequence.constant.SequenceConstant;
import com.mengxiang.base.common.sequence.exception.SequenceException;
import com.mengxiang.base.common.sequence.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 序列DAO默认实现，JDBC方式
 *
 * @author JoinFyc
 */
public class DefaultSequenceDAO {

    private static final Logger logger = LoggerFactory
            .getLogger(SequenceConstant.SEQUENCE_LOG_NAME);

    /**
     * 序列数据源持有类
     */
    private SequenceDataSourceHolder sequenceDataSourceHolder;

    /**
     * sequence表名默认值
     */
    private static final String DEFAULT_TABLE_NAME = "worker_sequence";

    /**
     * 以下表结构字段默认值
     * 分别是：sequence名称、sequence当前值、库的标号、最小值、最大值、内步长、创建时间以及修改时间
     */
    private static final String DEFAULT_NAME_COLUMN_NAME = "name";
    private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
    private static final String DEFAULT_MIN_VALUE_COLUMN_NAME = "min_value";
    private static final String DEFAULT_MAX_VALUE_COLUMN_NAME = "max_value";
    private static final String DEFAULT_CODE_COLUMN_NAME = "code";
    private static final String DEFAULT_EFFECTIVE_TIME_COLUMN_NAME = "effective_time";
    private static final String DEFAULT_INNER_STEP_COLUMN_NAME = "step";
    private static final String DEFAULT_GMT_CREATE_COLUMN_NAME = "create_time";
    private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "update_time";

    /**
     * 序列所在的表名 默认为 sequence
     */
    private String tableName = DEFAULT_TABLE_NAME;
    /**
     * 存储序列名称的列名 默认为 name
     */
    private String nameColumnName = DEFAULT_NAME_COLUMN_NAME;
    /**
     * 存储序列值的列名 默认为 value
     */
    private String valueColumnName = DEFAULT_VALUE_COLUMN_NAME;

    private String codeColumnName = DEFAULT_CODE_COLUMN_NAME;

    /**
     * 最小值的列名 默认为 min_value
     */
    private String minValueColumnName = DEFAULT_MIN_VALUE_COLUMN_NAME;
    /**
     * 最大值的列名 默认为max_value
     */
    private String maxValueColumnName = DEFAULT_MAX_VALUE_COLUMN_NAME;

    /**
     * sequence 过期时间,如果过期，需要重置
     */
    private String effectiveTimeColumnName = DEFAULT_EFFECTIVE_TIME_COLUMN_NAME;
    /**
     * 内步长的列名 默认为step
     */
    private String innerStepColumnName = DEFAULT_INNER_STEP_COLUMN_NAME;

    /**
     * 创建时间 默认为gmt_create
     */
    private String gmtCreateColumnName = DEFAULT_GMT_CREATE_COLUMN_NAME;
    /**
     * 存储序列最后更新时间的列名 默认为 gmt_modified
     */
    private String gmtModifiedColumnName = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

    /**
     * 重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 150;

    /**
     * sequence的最大值=Long.MAX_VALUE-DELTA，超过这个值就说明sequence溢出了.
     */
    private static final long DELTA = 100000000L;

    /**
     * 重试次数
     */
    private int retryTimes = DEFAULT_RETRY_TIMES;

    /**
     * 查询sequence记录的sql<br>
     * 格式：select value from sequence where name=?
     */
    private String selectSql;

    /**
     * 更新sequence记录的sql<br>
     * 格式 update table_name(default：sequence) set value=? ,gmt_modified=? where name= and value=?
     */
    private String updateSql;

    /**
     * 插入sequence记录的sql<br>
     * 格式: insert into table_name(default:sequence)(name,value,min_value,max_value,step,gmt_create,gmt_modified) values(?,?,?,?,?,?,?)
     */
    private String insertSql;

    /**格式：select value from table_name(default:sequence) where name=? */
    /**
     * 获取db里所有sequence记录的sql<br>
     * 格式：select name,value,min_value,max_value,step from sequence
     */
    private String selectAllRecordSql;

    /**
     * DefaultSequenceDao是否已经初始化
     */
    private volatile boolean initialize = false;

    /**
     * 初始化multiSequenceDao<br>
     * 1）获取数据源的个数；2）生成随机对象； 3）初始化各个数据源包装器的sql等参数 4）如果配置了log库，则初始化异步log库
     *
     * @throws SequenceException
     */
    public void init() {
        if (initialize) {
            throw new SequenceException("ERROR ## the DefaultSequenceDao has inited");
        }

        sequenceDataSourceHolder.setParameters(getTableName(), getSelectSql(), getUpdateSqlNew(),
                getInsertSql());

        initialize = true;
    }

    /**
     * 取得下一个可用的序列区间
     *
     * @param name 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException
     */
    public SequenceRange nextRange(String name) throws SequenceException {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("序列名称不能为空");
        }

        SequenceRange sequenceRange = sequenceDataSourceHolder.tryGetSequenceRange(name);
        return sequenceRange;
    }

    /**
     * 获取当前db里所有的sequence记录的指定字段值
     *
     * @return Map<String, Map < String, Object>> 外层key标识sequence名字，内层key表示最小、最大值以及步长等；
     * @throws SQLException
     */
    public Map<String, Record> getAllSequenceNameRecord() throws SQLException {
        Map<String, Record> sequenceRecordMap = sequenceDataSourceHolder
                .getAllSequenceRecordName(getSelectAllRecord(), getNameColumnName(), getCodeColumnName(),
                        getMinValueColumnName(), getMaxValueColumnName(), getInnerStepColumnName(), getEffectiveTimeColumnName());

        return sequenceRecordMap;
    }

    public void initBusinessRecord(List<Record> businessTypes) throws SQLException {
        sequenceDataSourceHolder.initBusinessRecord(businessTypes);
    }

    public void refreshStep(String businessName, int newStep) throws SQLException {
        sequenceDataSourceHolder.refreshBusinessStep(getRefreshStepSql(), businessName, newStep);
    }

    private String getRefreshStepSql() {
        return "update worker_sequence set step = ?, update_time=? where name = ?";
    }


    /**
     * 格式 update table_name(default：sequence) set value=? ,gmt_modified=? where name=? and value=? and expire_time=?
     */
    private String getUpdateSql() {
        if (updateSql == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("update worker_sequence set \n" +
                    "value= (case when effective_time < ? then MIN_VALUE else value+step end),\n" +
                    "effective_time=(case when effective_time< ? then ? else effective_time end),\n" +
                    "update_time= ?\n" +
                    "where  name= ?;");
            updateSql = buffer.toString();
        }
        return updateSql;
    }

    /**
     * 格式：select value from table_name(default:sequence) where name=?
     */
    private String getSelectSql() {
        if (selectSql == null) {
            selectSql = "select value,step,effective_time,min_value,max_value from worker_sequence where name = ?";
        }
        return selectSql;
    }


    /**
     * 新版更新sql
     *
     * @return
     */
    private String getUpdateSqlNew() {
        return "update worker_sequence set \n" +
                "value= (case when max_value< (step+value) then MIN_VALUE else value+step end),\n" +
                "effective_time=(case when effective_time< ? then ? else effective_time end),\n" +
                "update_time= ?\n" +
                "where  name= ?;";
    }

    /**
     * 格式：select name,value,min_value,max_value,step from table_name(default:sequence)
     */
    public String getSelectAllRecord() {
        if (selectAllRecordSql == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("select name,value,code,min_value,max_value,effective_time,step from worker_sequence");
            selectAllRecordSql = buffer.toString();
        }
        return selectAllRecordSql;
    }

    /**
     * 格式: insert into table_name(default:sequence)(name,code,value,min_value,max_value,step,effective_time,gmt_create,gmt_modified) values(?,?,?,?,?,?,?,?,?)
     */
    private String getInsertSql() {
        if (insertSql == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("insert into ").append(getTableName()).append("(");
            buffer.append(getNameColumnName()).append(",");
            buffer.append(getCodeColumnName()).append(",");
            buffer.append(getValueColumnName()).append(",");
            buffer.append(getMinValueColumnName()).append(",");
            buffer.append(getMaxValueColumnName()).append(",");
            buffer.append(getInnerStepColumnName()).append(",");
            buffer.append(getEffectiveTimeColumnName()).append(",");
            buffer.append(getGmtCreateColumnName()).append(",");
            buffer.append(getGmtModifiedColumnName()).append(") values(?,?,?,?,?,?,?,?,?);");
            insertSql = buffer.toString();
        }
        return insertSql;
    }

    /**
     * Getter method for property <tt>retryTimes</tt>.
     *
     * @return property value of retryTimes
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param retryTimes value to be assigned to property retryTimes
     */
    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException(
                    "Property retryTimes cannot be less than zero, retryTimes = " + retryTimes);
        }
        this.retryTimes = retryTimes;
    }

    /**
     * Getter method for property <tt>tableName</tt>.
     *
     * @return property value of tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param tableName value to be assigned to property tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Getter method for property <tt>nameColumnName</tt>.
     *
     * @return property value of nameColumnName
     */
    public String getNameColumnName() {
        return nameColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param nameColumnName value to be assigned to property nameColumnName
     */
    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    /**
     * Getter method for property <tt>valueColumnName</tt>.
     *
     * @return property value of valueColumnName
     */
    public String getValueColumnName() {
        return valueColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param valueColumnName value to be assigned to property valueColumnName
     */
    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    /**
     * Getter method for property <tt>minValueColumnName</tt>.
     *
     * @return property value of minValueColumnName
     */
    public String getMinValueColumnName() {
        return minValueColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param minValueColumnName value to be assigned to property minValueColumnName
     */
    public void setMinValueColumnName(String minValueColumnName) {
        this.minValueColumnName = minValueColumnName;
    }

    /**
     * Getter method for property <tt>maxValueColumnName</tt>.
     *
     * @return property value of maxValueColumnName
     */
    public String getMaxValueColumnName() {
        return maxValueColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param maxValueColumnName value to be assigned to property maxValueColumnName
     */
    public void setMaxValueColumnName(String maxValueColumnName) {
        this.maxValueColumnName = maxValueColumnName;
    }

    /**
     * Getter method for property <tt>innerStepColumnName</tt>.
     *
     * @return property value of innerStepColumnName
     */
    public String getInnerStepColumnName() {
        return innerStepColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param innerStepColumnName value to be assigned to property innerStepColumnName
     */
    public void setInnerStepColumnName(String innerStepColumnName) {
        this.innerStepColumnName = innerStepColumnName;
    }

    /**
     * Getter method for property <tt>gmtCreateColumnName</tt>.
     *
     * @return property value of gmtCreateColumnName
     */
    public String getGmtCreateColumnName() {
        return gmtCreateColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param gmtCreateColumnName value to be assigned to property gmtCreateColumnName
     */
    public void setGmtCreateColumnName(String gmtCreateColumnName) {
        this.gmtCreateColumnName = gmtCreateColumnName;
    }

    /**
     * Getter method for property <tt>gmtModifiedColumnName</tt>.
     *
     * @return property value of gmtModifiedColumnName
     */
    public String getGmtModifiedColumnName() {
        return gmtModifiedColumnName;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param gmtModifiedColumnName value to be assigned to property gmtModifiedColumnName
     */
    public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
        this.gmtModifiedColumnName = gmtModifiedColumnName;
    }

    /**
     * Getter method for property <tt>sequenceDataSourceHolder</tt>.
     *
     * @return property value of sequenceDataSourceHolder
     */
    public SequenceDataSourceHolder getSequenceDataSourceHolder() {
        return sequenceDataSourceHolder;
    }

    /**
     * Setter method for property <tt>counterType</tt>.
     *
     * @param sequenceDataSourceHolder value to be assigned to property sequenceDataSourceHolder
     */
    public void setSequenceDataSourceHolder(SequenceDataSourceHolder sequenceDataSourceHolder) {
        this.sequenceDataSourceHolder = sequenceDataSourceHolder;
    }

    public String getEffectiveTimeColumnName() {
        return effectiveTimeColumnName;
    }

    public String getCodeColumnName() {
        return codeColumnName;
    }
}
