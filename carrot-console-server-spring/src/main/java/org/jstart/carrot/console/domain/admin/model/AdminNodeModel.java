package org.jstart.carrot.console.domain.admin.model;

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
@TableName("admin_node")
public class AdminNodeModel implements Serializable {

    /**
     * id
     */
    @TableId(value = "id")
    private String id;

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
     * 权重值
     */
    @TableField(value = "weight")
    private Double weight;

    /**
     * 在线时间
     */
    @TableField(value = "at_time")
    private Long atTime;
}
