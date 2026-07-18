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
public class SchedulingExecutorNodeVO implements Serializable {
    /**
     * id》主键命名空间+执行器名称+组+执行器ip+执行器端口
     */
    private String id;

    /**
     * 执行器ip
     */
    private String ip;

    /**
     * 执行器端口
     */
    private Integer port;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 组
     */
    private String groupName;

    /**
     * 秘钥
     */
    private String secret;

    /**
     * 执行器id
     */
    private String executorName;

    /**
     * 命名空间
     */
    private String namespaceId;
}
