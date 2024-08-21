package com.mengxiang.base.common.model.result;

import com.mengxiang.base.common.model.request.PagingRequest;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
 *
 * @author JoinFyc
 * @version 1.0
 * @date 2019/5/9 11:15 AM
 */
public class Pagination<T> implements Serializable {

    /**
     * 页码
     */
    private Integer pageIndex;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 当前页记录数
     */
    private Integer currentPageSize;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据
     */
    private List<T> result;

    /**
     * 扩展字段，请求时传入，分页结果会原样返回
     */
    private Object extra;

    public Pagination() {
    }

    public Pagination(Integer pageIndex, Integer pageSize, Long total, List<T> result) {
        this(pageIndex, pageSize, total, result, null);
    }

    public Pagination(Integer pageIndex, Integer pageSize, Long total, List<T> result, Object extra) {
        validate(pageIndex, pageSize, total);
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.extra = extra;
        this.total = total;
        this.result = result;
        calculatePageInfo(pageIndex, pageSize, total);
    }

    public Pagination(PagingRequest request, Long total, List<T> result) {
        this(request.getPageIndex(), request.getPageSize(), total, result, request.getExtra());
    }

    private void calculatePageInfo(Integer pageIndex, Integer pageSize, Long total) {
        this.pages = (total.intValue() - 1) / pageSize + 1;
        if (pageIndex < pages) {
            this.currentPageSize = pageSize;
        } else if (pageIndex.equals(pages)) {
            this.currentPageSize = total.intValue() - (pageIndex - 1) * pageSize;
        } else {
            this.currentPageSize = 0;
        }
    }

    private void validate(Integer pageIndex, Integer pageSize, Long total) {
        if (!isPositive(pageIndex)) {
            throw new IllegalArgumentException("Page index must be positive.");
        }
        if (!isPositive(pageSize)) {
            throw new IllegalArgumentException("Page size must be positive.");
        }
        if (!isPositiveOrZero(total)) {
            throw new IllegalArgumentException("Total number must be positive or zero.");
        }
    }

    private boolean isPositive(Number number) {
        return number != null && number.longValue() > 0;
    }

    private boolean isPositiveOrZero(Number number) {
        return number != null && number.longValue() >= 0;
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

    public Integer getCurrentPageSize() {
        if (currentPageSize == null && isPositive(pageIndex)
                && isPositive(pageSize) && isPositiveOrZero(total)) {
            calculatePageInfo(pageIndex, pageSize, total);
        }
        return currentPageSize;
    }

    public void setCurrentPageSize(Integer currentPageSize) {
        this.currentPageSize = currentPageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPages() {
        if (pages == null && isPositive(pageIndex)
                && isPositive(pageSize) && isPositiveOrZero(total)) {
            calculatePageInfo(pageIndex, pageSize, total);
        }
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public Boolean hasNext() {
        return this.pageIndex < this.pages;
    }

    public Boolean hasPrevious() {
        return this.pageIndex > 1;
    }
}
