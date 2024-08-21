package com.mengxiang.base.common.model.request;

/**
 * 分页请求对象
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/5/9 11:33 AM
 */
public class PagingRequest extends BaseRequest {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认条数
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 页码
     */
    private Integer pageIndex;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 扩展字段，请求时传入，分页结果会原样返回
     * 例如：可用于传入总记录数，如果没有传入，则方法内部求取count，否则直接返回总记录数，不需要每一页重新count
     */
    private Object extra;

    public PagingRequest() {
        this.pageIndex = DEFAULT_PAGE_INDEX;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public PagingRequest(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public PagingRequest(Integer pageIndex, Integer pageSize, Object extra) {
        this(pageIndex, pageSize);
        this.extra = extra;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
