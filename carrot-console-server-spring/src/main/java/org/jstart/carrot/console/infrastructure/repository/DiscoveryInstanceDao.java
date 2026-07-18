package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryInstanceModel;
import org.springframework.stereotype.Repository;


@Repository
public interface DiscoveryInstanceDao extends BaseMapper<DiscoveryInstanceModel> {
}
