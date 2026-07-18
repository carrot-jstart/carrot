package org.jstart.carrot.console.domain.scheduling.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: jstart
 * @date: 2026/5/27
 * @description: 执行器节点信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "scheduling_executor_node")
public class ExecutorNodeModel implements Serializable {

    /**
     * id》主键命名空间+执行器名称+组+执行器ip+执行器端口
     */
    @TableId(value = "id")
    private String id;

    /**
     * 执行器ip
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 执行器端口
     */
    @TableField(value = "port")
    private Integer port;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 组
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 秘钥
     */
    @TableField(value = "secret")
    private String secret;

    /**
     * 执行器id
     */
    @TableField(value = "executor_name")
    private String executorName;

    /**
     * 命名空间
     */
    @TableField(value = "namespace_id")
    private String namespaceId;

    /**
     * 权重
     */
    @TableField(value = "weight")
    private Double weight;

}