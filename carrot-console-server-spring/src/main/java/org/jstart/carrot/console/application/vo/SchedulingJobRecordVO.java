package org.jstart.carrot.console.application.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SchedulingJobRecordVO implements Serializable {
    /**
     * 主键》unitId+planStartTime
     */
    private String id;

    /**
     * 计划id
     */
    private String unitId;

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 执行器id
     */
    private String executorName;

    /**
     * 执行器组
     */
    private String groupName;

    /**
     * ip
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 秘钥
     */
    private String secret;

    /**
     * handleId
     */
    private String unitName;

    /**
     * 计划开始时间
     */
    private Long planStartTime;

    /**
     * 实际开始时间
     */
    private Long actualStartTime;

    /**
     * 实际结束时间
     */
    private Long actualEndTime;

    /**
     * 运行状态码》 0-未开始 &200-成功 &500-失败 &400-未找到执行器 &403-拒接执行
     */
    private Integer code;

    /**
     * 运行信息
     */
    private String message;

    /**
     * 运行结果
     */
    private String result;

    /**
     * 运行日志
     */
    private List<String> log;

    /**
     * hashCode
     */
    private Integer hashValue;
}
