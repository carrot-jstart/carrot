package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jstart.carrot.console.domain.scheduling.model.TaskLockModel;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulingExecuteTaskLockDao extends BaseMapper<TaskLockModel> {
}
