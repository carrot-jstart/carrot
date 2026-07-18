package org.jstart.carrot.console.domain.scheduling.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "scheduling_job_unit", autoResultMap = true)
public class JobUnitModel implements Serializable {
    /**
     * 主键》命名空间+执行器名称+组+handleName
     */
    @TableId(value = "id")
    private String id;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 执行器id
     */
    @TableField(value = "executor_name")
    private String executorName;

    /**
     * 组
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 调度类型
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 调度参数值
     */
    @TableField(value = "type_value")
    private String typeValue;

    /**
     * 命名空间
     */
    @TableField(value = "namespace_id")
    private String namespaceId;

    /**
     * 最后一次执行时间
     */
    @TableField(value = "last_plan_time")
    private Long lastPlanTime;

    /**
     * hashValue
     */
    @TableField(value = "hash_value")
    private Integer hashValue;

    /**
     * 是否启用》0-启用&10不启用
     */
    @TableField(value = "enable")
    private Integer enable;
}
