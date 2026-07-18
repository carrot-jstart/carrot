package org.jstart.carrot.console.domain.config;

import org.jstart.carrot.console.domain.config.model.ConfigItemModel;

public interface ConfigCommandDomainServer {
    /**
     * 更新
     * @param model
     */
    void save(ConfigItemModel model);

    /**
     * 创建
     * @param configItemModel
     * @return
     */
    String create(ConfigItemModel configItemModel);

    /**
     * 更新
     * @param configItemModel
     * @return
     */
    boolean update(ConfigItemModel configItemModel);

    /**
     * 删除
     * @param id
     * @return
     */
    boolean delete(String id);
}
