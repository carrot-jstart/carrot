package org.jstart.carrot.console.domain.discovery.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jstart.carrot.console.infrastructure.pg.JsonbTypeHandler;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "discovery_instance", autoResultMap = true)
public class DiscoveryInstanceModel implements Serializable {
    @TableId(value = "id")
    private String id;

    @TableField(value = "namespace")
    private String namespace;

    @TableField(value = "group_name")
    private String groupName;

    @TableField(value = "service_name")
    private String serviceName;

    @TableField(value = "instance_id")
    private String instanceId;

    @TableField(value = "ip")
    private String ip;

    @TableField(value = "port")
    private Integer port;

    @TableField(value = "weight")
    private Double weight;

    @TableField(value = "metadata",typeHandler = JsonbTypeHandler.class)
    private Map<String,String> metadata;

    @TableField(value = "last_heartbeat_at")
    private Long lastHeartbeatAt;

    @TableField(value = "create_time")
    private Long createTime;

    @TableField(value = "update_time")
    private Long updateTime;
}
