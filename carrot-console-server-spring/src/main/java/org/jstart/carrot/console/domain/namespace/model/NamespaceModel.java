package org.jstart.carrot.console.domain.namespace.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 命名空间
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "namespace")
public class NamespaceModel implements Serializable {
    /**
     * id
     */
    @TableId(value = "id")
    private String id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Long createTime;

    /**
     * 命名空间名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 命名空间描述
     */
    @TableField(value = "description")
    private String description;
}