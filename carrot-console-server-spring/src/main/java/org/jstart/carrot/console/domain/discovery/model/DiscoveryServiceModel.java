package org.jstart.carrot.console.domain.discovery.model;

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
@TableName("discovery_service")
public class DiscoveryServiceModel implements Serializable {

    /**
     * 服务id
     */
    @TableId(value = "id")
    private String id;

    /**
     * 命名空间
     */
    @TableField(value = "namespace")
    private String namespace;

    /**
     * 组名
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 服务名
     */
    @TableField(value = "service_name")
    private String serviceName;

    /**
     * 服务实例数量
     */
    @TableField(value = "instance_count")
    private Long instanceCount;

}
