package org.jstart.carrot.console.domain.discovery;


import org.jstart.carrot.console.application.dto.SearchDiscoveryInstance;
import org.jstart.carrot.console.application.dto.SearchDiscoveryService;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryInstanceModel;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryServiceModel;

import java.util.List;

public interface DiscoveryQueryDomainServer {
    /**
     * 根据id查询实例
     * @param id
     * @return
     */
    DiscoveryInstanceModel selectInstanceById(String id);

    /**
     * 查询实例列表
     * @param search
     * @return
     */
   PageResult<List<DiscoveryInstanceModel>> selectInstance(SearchDiscoveryInstance search);

   /**
     * 查询服务列表
     * @param dto
     * @return
     */
    PageResult<List<DiscoveryServiceModel>> searchService(SearchDiscoveryService dto);
}
