package org.jstart.carrot.console.comm.entity.vo;

import java.io.Serializable;

/**
 * 分页查询结果
 */
public class PageResult<T> implements Serializable {
    /**
     * 每页大小
     */
    private Integer limit;
    /**
     * 当前页
     */
    private Integer page;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 查询结果
     */
    private T data;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public PageResult() {
    }
    public PageResult(Integer limit, Integer page, Long total, T data) {
        this.limit = limit;
        this.page = page;
        this.total = total;
        this.data = data;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "limit=" + limit +
                ", page=" + page +
                ", total=" + total +
                ", data=" + data +
                '}';
    }
}
