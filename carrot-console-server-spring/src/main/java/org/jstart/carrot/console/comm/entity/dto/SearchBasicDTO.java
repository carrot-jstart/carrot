package org.jstart.carrot.console.comm.entity.dto;


import java.io.Serializable;
import java.util.List;


public class SearchBasicDTO<T> extends PagerBasicDTO implements Serializable {
    /**
     * 关键字
     */
    private String keywords;
    /**
     * id列表
     */
    private List<T> listId;
    /**
     * 数据状态 EDataStatus
     */
    private Integer status;
    /**
     * 数据状态列表
     */
    private List<Integer> listStatus;
    /**
     * 数据创建时间-范围(eg: 2021-01-01 00:00:00 ~ 2021-01-01 23:59:59)"
     */
    private String createTimeRange;
    /**
     * 数据创建时间-起始
     * @ignore
     */
    private Long createTimeStart;
    /**
     * 数据创建时间-截止
     * @ignore
     */
    private Long createTimeEnd;
    /**
     * 上次请求最后一条数据的id
     */
    private Long lastId;
    /**
     * 排序列表
     */
    private List<OrderByDTO> orderBy;

    public SearchBasicDTO() {
    }
    public SearchBasicDTO(Integer pageSize, Integer pageIndex) {
        super(pageSize, pageIndex);
    }
    public SearchBasicDTO(Integer pageSize, Integer pageIndex, String keywords) {
        super(pageSize, pageIndex);
        this.keywords = keywords;
    }
    public SearchBasicDTO(Integer pageSize, Integer pageIndex, String keywords, List<OrderByDTO> orderBy) {
        super(pageSize, pageIndex);
        this.keywords = keywords;
        this.orderBy = orderBy;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<T> getListId() {
        return listId;
    }

    public void setListId(List<T> listId) {
        this.listId = listId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Integer> getListStatus() {
        return listStatus;
    }

    public void setListStatus(List<Integer> listStatus) {
        this.listStatus = listStatus;
    }

    public String getCreateTimeRange() {
        return createTimeRange;
    }

    public void setCreateTimeRange(String createTimeRange) {
        this.createTimeRange = createTimeRange;
    }

    public Long getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Long createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Long getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Long createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public Long getLastId() {
        return lastId;
    }

    public void setLastId(Long lastId) {
        this.lastId = lastId;
    }

    public List<OrderByDTO> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderByDTO> orderBy) {
        this.orderBy = orderBy;
    }
}
