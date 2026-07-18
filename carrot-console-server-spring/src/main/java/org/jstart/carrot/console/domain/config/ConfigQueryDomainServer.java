package org.jstart.carrot.console.domain.config;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.jstart.carrot.console.application.dto.SearchConfigItem;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.domain.config.model.ConfigItemModel;
import org.jstart.carrot.scheduling.constant.KeyValue;

import java.util.List;

public interface ConfigQueryDomainServer {
    /**
     * 根据id查询
     * @param id
     * @return
     */
    ConfigItemModel selectById(String id);

    /**
     * 根据namespace和group和dataId查询
     * @return
     */
    List<ConfigItemModel> findNameSpaceAndGroupAndDataIdUpdateTimeByOrSearch(List<KeyValue<String, Long>> orSearch);

    /**
     * 简单查询
     * @param dto
     * @return
     */
    PageResult<List<ConfigItemModel>> searchPage(SearchConfigItem dto, SFunction<ConfigItemModel, ?>... columns);
}
