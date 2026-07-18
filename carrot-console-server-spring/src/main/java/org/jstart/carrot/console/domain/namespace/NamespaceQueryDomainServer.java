package org.jstart.carrot.console.domain.namespace;

import org.jstart.carrot.console.domain.namespace.model.NamespaceModel;

import java.util.List;

public interface NamespaceQueryDomainServer {
    /**
     * 获取所有命名空间
     * @return
     */
    List<NamespaceModel> getAllNamespaces();

    /**
     * 是否存在
     */
    boolean exists(String id);
}
