package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulingJobUnitDao extends BaseMapper<JobUnitModel> {

    List<String> getUnitExecutorNameByNamespace(String namespaceId);

    List<String> getUnitGroupByNamespace(String namespaceId);

    List<JobUnitModel> schedule(Integer mod, Integer modValue);

}
