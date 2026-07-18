package org.jstart.carrot.console.domain.scheduling.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jstart.carrot.console.infrastructure.pg.StringListArrayTypeHandler;

import java.io.Serializable;
import java.util.List;

/**
 * 排班
 */
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduling_job_record", autoResultMap = true)
public class JobRecordModel implements Serializable {
    /**
     * 主键》unitId+planStartTime
     */
    @TableId(value = "id")
    private String id;

    /**
     * 计划id
     */
    @TableField(value = "unit_id")
    private String unitId;

    /**
     * 命名空间
     */
    @TableField(value = "namespace_id")
    private String namespaceId;

    /**
     * 执行器id
     */
    @TableField(value = "executor_name")
    private String executorName;

    /**
     * 执行器组
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * ip
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 端口
     */
    @TableField(value = "port")
    private Integer port;

    /**
     * 秘钥
     */
    @TableField(value = "secret")
    private String secret;

    /**
     * handleId
     */
    @TableField(value = "unit_name")
    private String unitName;

    /**
     * 计划开始时间
     */
    @TableField(value = "plan_start_time")
    private Long planStartTime;

    /**
     * 实际开始时间
     */
    @TableField(value = "actual_start_time")
    private Long actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField(value = "actual_end_time")
    private Long actualEndTime;

    /**
     * 运行状态码
     */
    @TableField(value = "code")
    private Integer code;

    /**
     * 运行信息
     */
    @TableField(value = "message")
    private String message;

    /**
     * 运行结果
     */
    @TableField(value = "result")
    private String result;

    /**
     * 运行日志
     */
    @TableField(value = "log",typeHandler = StringListArrayTypeHandler.class)
    private List<String> log;

    /**
     * hashCode
     */
    @TableField(value = "hash_value")
    private Integer hashValue;
}
