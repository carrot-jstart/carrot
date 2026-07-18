package org.jstart.carrot.console.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jstart.carrot.console.comm.entity.dto.SearchBasicDTO;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchSchedulingJobRecord extends SearchBasicDTO<String> {
    /**
     * 命名空间
     */
    private String eqNamespaceId;

    /**
     * 执行器名称
     */
    private String eqExecutorName;

    /**
     * 组
     */
    private String eqGroupName;

    /**
     * 调度单位id
     */
    private String eqUnitId;

    /**
     * 运行状态码
     */
    private Integer eqCode;

    /**
     * 计划开始最小时间
     */
    private Long planStartTimeMin;

    /**
     * 计划开始最大时间
     */
    private Long planStartTimeMax;
}
