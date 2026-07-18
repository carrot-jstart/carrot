package org.jstart.carrot.console.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 执行器心跳信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SchedulingHearBeatDTO implements Serializable {

    /**
     * 执行器密钥
     */
    private String secret;

    /**
     * 执行器ip
     */
    private String ip;
    /**
     * 执行器端口
     */
    private Integer port;

    /**
     * 权重
     */
    private Double weight;
}
