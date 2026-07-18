package org.jstart.carrot.console.domain.namespace;

import org.jstart.carrot.console.domain.namespace.model.NamespaceModel;

public interface NamespaceCommandDomainServer {
    /**
     * 创建命名空间
     * @param namespaceModel
     * @return
     */
    String createNamespace(NamespaceModel namespaceModel);

    /**
     * 修改命名空间
     * @param namespaceModel
     * @return
     */
    boolean modifyNamespace(NamespaceModel namespaceModel);

    /**
     * 删除命名空间
     * @param id
     * @return
     */
    boolean deleteNamespace(String id);
}
