package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryServiceModel;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscoveryServiceDao extends BaseMapper<DiscoveryServiceModel> {
}
