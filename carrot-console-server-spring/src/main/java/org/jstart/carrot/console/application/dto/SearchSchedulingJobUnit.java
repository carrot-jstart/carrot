package org.jstart.carrot.console.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jstart.carrot.console.comm.entity.dto.SearchBasicDTO;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchSchedulingJobUnit extends SearchBasicDTO<String> {

    /**
     * 命名空间
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


    /**
     * 名称
     */
    private String likeName;
}
