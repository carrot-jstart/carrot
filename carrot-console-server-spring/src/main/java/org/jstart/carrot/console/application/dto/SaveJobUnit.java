package org.jstart.carrot.console.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jstart.carrot.scheduling.constant.EJobUnitType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SaveJobUnit implements Serializable {

    /**
     * 名称
     */
    private String name;
    /**
     * 调度类型
     */
    private EJobUnitType type;

    /**
     * 调度参数值
     */
    private String typeValue;

}
