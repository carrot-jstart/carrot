package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulingExecutorNodeDao extends BaseMapper<ExecutorNodeModel> {
}
