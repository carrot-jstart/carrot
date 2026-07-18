package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.namespace.model.NamespaceModel;
import org.springframework.stereotype.Repository;

@Repository
public interface NamespaceDao extends BaseMapper<NamespaceModel> {
}
