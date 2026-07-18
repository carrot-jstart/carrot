package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.admin.model.AdminNodeModel;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminNodeDao extends BaseMapper<AdminNodeModel> {
}
