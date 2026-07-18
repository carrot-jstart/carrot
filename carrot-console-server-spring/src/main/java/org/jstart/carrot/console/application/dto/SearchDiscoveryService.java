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
public class SearchDiscoveryService extends SearchBasicDTO<String> {
    private String eqNamespace;
    private String likeGroup;
    private String likeServiceName;
}
