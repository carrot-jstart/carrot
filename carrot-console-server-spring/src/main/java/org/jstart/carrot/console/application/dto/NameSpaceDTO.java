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
public class NameSpaceDTO implements Serializable {
    /**
     *  id
     */
    private String id;

    /**
     * 命名空间名称
     */
    private String name;

    /**
     * 命名空间描述
     */
    private String description;
}
