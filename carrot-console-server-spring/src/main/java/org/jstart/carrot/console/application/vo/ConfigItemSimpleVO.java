package org.jstart.carrot.console.application.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConfigItemSimpleVO implements Serializable {
    private String id;

    private String namespace;

    private String groupName;

    private String dataId;

    private String contentType;

    private Long updateTime;
}
