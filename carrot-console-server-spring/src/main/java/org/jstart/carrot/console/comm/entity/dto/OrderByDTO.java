package org.jstart.carrot.console.comm.entity.dto;

import java.io.Serializable;

/**
 * 排序字段
 */
public class OrderByDTO implements Serializable {
    /**
     * 排序字段枚举(1:创建时间，2修改时间，3其他)
     */
    private Integer orderBy;
    /**
     * 是否升序排序 true=升序,不传或者其他任意值=降序(默认)
     */
    private Boolean isAsc=false;

    public OrderByDTO() {
    }

    public OrderByDTO(Integer orderBy) {
        this.orderBy = orderBy;
    }

    public OrderByDTO(Integer orderBy, Boolean isAsc) {
        this.orderBy = orderBy;
        this.isAsc = isAsc;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public OrderByDTO setOrderBy(Integer orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public Boolean getIsAsc() {
        return isAsc;
    }

    public OrderByDTO setIsAsc(Boolean asc) {
        isAsc = asc;
        return this;
    }


}
