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
public class ModifyConfigItem implements Serializable {
    private String id;

    private String content;


    private String contentType;

}
