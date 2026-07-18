package org.jstart.carrot.console.comm.entity.dto;


import java.io.Serializable;

/**
 * 分页返回
 */

public class PagerBasicDTO implements Serializable {
    /**
     * 页大小(每页返回的记录条数)
     */
    private Integer pageSize;
    /**
     * 页号(第几页，从1开始)
     */
    private Integer pageIndex;

    public PagerBasicDTO(Integer pageSize, Integer pageIndex) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }
    public PagerBasicDTO() {
    }
    public Integer getPageSize() {
        return pageSize;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    public Integer getPageIndex() {
        return pageIndex;
    }
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
}
