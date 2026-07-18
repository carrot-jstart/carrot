package org.jstart.carrot.console.application.dto;

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
public class DiscoveryInstanceDTO implements Serializable {

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 权重
     */
    private Double weight;

    /**
     * 元数据
     */
    private Map<String,String> metadata;
}
