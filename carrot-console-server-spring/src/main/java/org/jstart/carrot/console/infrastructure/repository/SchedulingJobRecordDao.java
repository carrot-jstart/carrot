package org.jstart.carrot.console.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchedulingJobRecordDao extends BaseMapper<JobRecordModel> {
    /**
     * 根据取模获取任务日志
     * @param startTime
     * @param endTime
     * @param mod
     * @param modValue
     * @return
     */
    List<JobRecordModel> getRecentPlan(long startTime, long endTime, Integer mod, Integer modValue);

    /**
     * 删除过期数据
     * @param successCode 成功状态码
     * @param saveSuccessLimit 保存成功条数
     */
    Long removeExpiredData(Integer successCode,Integer saveSuccessLimit);
}
