package org.jstart.carrot.console.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ServiceKey implements Serializable {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 分组
     */
    private String group;

    /**
     * 服务名称
     */
    private String serviceName;
}
