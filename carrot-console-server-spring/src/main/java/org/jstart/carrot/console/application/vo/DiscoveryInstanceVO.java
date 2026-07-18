package org.jstart.carrot.console.application.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscoveryInstanceVO implements Serializable {

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 权重
     */
    private Double weight;

    /**
     * 元数据
     */
    private Map<String,String> metadata;

    /**
     * 最后心跳时间
     */
    private Long lastHeartbeatAt;
}
