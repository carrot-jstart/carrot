package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.config.model.ConfigItemModel;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigItemDao extends BaseMapper<ConfigItemModel> {
}
