package org.jstart.carrot.console.application.service;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.application.dto.NameSpaceDTO;
import org.jstart.carrot.console.application.vo.NamespaceVO;
import org.jstart.carrot.console.comm.utils.StringUtil;
import org.jstart.carrot.console.domain.namespace.NamespaceCommandDomainServer;
import org.jstart.carrot.console.domain.namespace.NamespaceQueryDomainServer;
import org.jstart.carrot.console.domain.namespace.model.NamespaceModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NamespaceService{
    private final NamespaceCommandDomainServer namespaceCommandDomainServer;
    private final NamespaceQueryDomainServer namespaceQueryDomainServer;
    public List<NamespaceVO> getAllNamespaces() {
        List<NamespaceModel> data = namespaceQueryDomainServer.getAllNamespaces();
        return data.stream().map(item -> new NamespaceVO(item.getId(),item.getCreateTime(),item.getName(),item.getDescription())).toList();
    }


    public String createNamespace(NameSpaceDTO dto) {
        if(StringUtil.isNullOrEmpty(dto.getId())){
            dto.setId(UUID.randomUUID().toString());
        }
        return namespaceCommandDomainServer.createNamespace(new NamespaceModel()
                .setId(dto.getId())
                .setName(dto.getName())
                .setDescription(dto.getDescription())
                .setCreateTime(System.currentTimeMillis())
        );
    }

    public boolean modifyNamespace(NameSpaceDTO dto) {
        return namespaceCommandDomainServer.modifyNamespace(new NamespaceModel()
                .setId(dto.getId())
                .setName(dto.getName())
                .setDescription(dto.getDescription())
        );
    }

    public boolean deleteNamespace(String id) {
        return namespaceCommandDomainServer.deleteNamespace(id);
    }
}
