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
public class NamespaceVO implements Serializable {
    /**
     * id》命名空间+执行器名称
     */
    private String id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 执行器名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
}
