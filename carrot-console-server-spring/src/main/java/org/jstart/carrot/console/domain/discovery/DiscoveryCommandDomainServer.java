package org.jstart.carrot.console.domain.discovery;

import org.jstart.carrot.console.domain.discovery.model.DiscoveryInstanceModel;

public interface DiscoveryCommandDomainServer {

    /**
     * 生成服务id
     */
    String generateServiceId(String namespace, String groupName, String serviceName);


    /**
     * 保存服务实例
     * @param model
     */
    void saveModel(DiscoveryInstanceModel model);

    /**
     * 删除服务实例
     * @param id
     * @return
     */
    boolean deleteInstanceById(String id);
}
