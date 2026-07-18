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
public class ConfigItemInfoVO implements Serializable {
    private String id;

    private String namespace;

    private String groupName;

    private String dataId;

    private String content;

    private String md5;

    private String contentType;

    private Long createTime;

    private Long updateTime;
}
