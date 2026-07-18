package org.jstart.carrot.console.application.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SchedulingJobUnitVO implements Serializable {
    /**
     * 主键》命名空间+执行器名称+组+handleName
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 执行器id
     */
    private String executorName;

    /**
     * 组
     */
    private String groupName;

    /**
     * 调度类型
     */
    private Integer type;

    /**
     * 调度参数值
     */
    private String typeValue;

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 最后一次执行时间
     */
    private Long lastPlanTime;

    /**
     * hashValue
     */
    private Integer hashValue;

    /**
     * 是否启用》0-启用&10不启用
     */
    private Integer enable;
}
