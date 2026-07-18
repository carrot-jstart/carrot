package org.jstart.carrot.console.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jstart.carrot.console.comm.entity.dto.SearchBasicDTO;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SearchSchedulingExecutorNode extends SearchBasicDTO<String> {
    /**
     * 命名空间id
     */
    private String eqNamespaceId;


    /**
     * 执行器名称
     */
    private String eqExecutorName;

    /**
     * 组
     */
    private String eqGroupName;

}
