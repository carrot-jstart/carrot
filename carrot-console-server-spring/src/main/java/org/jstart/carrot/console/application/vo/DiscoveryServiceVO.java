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
public class DiscoveryServiceVO implements Serializable {
    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 组名
     */
    private String group;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务实例数量
     */
    private Long instanceCount;
}
