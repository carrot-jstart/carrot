package org.jstart.carrot.console.domain.scheduling;

import org.jstart.carrot.console.application.dto.SearchSchedulingExecutorNode;
import org.jstart.carrot.console.application.dto.SearchSchedulingJobRecord;
import org.jstart.carrot.console.application.dto.SearchSchedulingJobUnit;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;

import java.util.List;

public interface SchedulingQueryDomainServer {
    /**
     * 查询执行器节点
     * @param dto
     * @return
     */
    PageResult<List<ExecutorNodeModel>> searchPageExecutorNode(SearchSchedulingExecutorNode dto);

    /**
     * 获取指定命名空间下的任务执行器名称
     * @param namespaceId
     * @return
     */
    List<String> getUnitExecutorNameByNamespace(String namespaceId);

    /**
     * 获取指定命名空间下的任务执行器组名称
     */
    List<String> getUnitGroupByNamespace(String namespaceId);

    /**
     * 查询任务单元
     * @param dto
     * @return
     */
    PageResult<List<JobUnitModel>> searchPageJobUnit(SearchSchedulingJobUnit dto);

    /**
     * 获取任务单元
     * @param id
     * @return
     */
    JobUnitModel getJobUnitById(String id);

    /**
     * 查询任务执行记录
     * @param dto
     * @return
     */
    PageResult<List<JobRecordModel>> searchJobRecord(SearchSchedulingJobRecord dto);

    /**
     * 获取任务执行记录
     * @param id
     * @return
     */
    JobRecordModel getJobRecordById(String id);
}
