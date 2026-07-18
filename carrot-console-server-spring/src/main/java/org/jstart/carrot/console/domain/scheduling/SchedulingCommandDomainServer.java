package org.jstart.carrot.console.domain.scheduling;

import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;

import java.util.List;

public interface SchedulingCommandDomainServer {
    /**
     * 保存执行器节点
     * @param executorNodeModel
     */
    void saveExecutorNode(ExecutorNodeModel executorNodeModel);

    /**
     * 清理过时的执行器节点
     * @param time
     */
    Integer clearExpiredNode(long time);

    /**
     * 删除执行器节点
     * @param executorNodeId
     * @return
     */
    boolean deleteExecutorNodeId(String executorNodeId);

    /**
     * 注册任务单元
     * @param list
     * @return
     */
    boolean registerJobUnitList(List<JobUnitModel> list);

    /**
     * 保存任务执行结果
     * @param jobRecordModel
     */
    void saveShift(JobRecordModel jobRecordModel);

    /**
     * 启用任务单元
     * @param id
     * @return
     */
    boolean enableJobUnit(String id);

    /**
     * 禁用任务单元
     * @param id
     * @return
     */
    boolean disableJobUnit(String id);

    /**
     * 删除任务单元
     * @param id
     * @return
     */
    boolean deleteJobUnit(String id);

    /**
     * 删除任务执行结果
     * @param ids
     * @return
     */
    boolean deleteJobRecord(List<String> ids);
}
