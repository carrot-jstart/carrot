package org.jstart.carrot.console.domain.scheduling;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jstart.carrot.console.comm.ConstantFactory;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.comm.utils.StringUtil;
import org.jstart.carrot.console.application.dto.SearchSchedulingExecutorNode;
import org.jstart.carrot.console.application.dto.SearchSchedulingJobRecord;
import org.jstart.carrot.console.application.dto.SearchSchedulingJobUnit;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingExecuteEvent;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingPrebeforeEvent;
import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;
import org.jstart.carrot.console.infrastructure.repository.SchedulingExecutorNodeDao;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobRecordDao;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobUnitDao;
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class SchedulingDomainService implements SchedulingCommandDomainServer, SchedulingQueryDomainServer, CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(SchedulingDomainService.class);
    private final ApplicationContext applicationContext;
    private volatile boolean running = true;
    private final ConcurrentHashMap<Integer, List<SchedulingExecuteEvent>> ringData = new ConcurrentHashMap<>();
    private final SchedulingExecutorNodeDao executorNodeDao;
    private final SchedulingJobRecordDao recordDao;
    private Thread schedulerThread;
    private Thread ringConsumerThread;
    private final SchedulingJobUnitDao jobUnitDao;

    @Override
    public void saveExecutorNode(ExecutorNodeModel executorNodeModel) {
        executorNodeDao.insertOrUpdate(executorNodeModel);
    }

    @Override
    public Integer clearExpiredNode(long time) {
        return executorNodeDao.delete(new LambdaQueryWrapper<ExecutorNodeModel>()
                .lt(ExecutorNodeModel::getUpdateTime, time)
        );
    }

    @Override
    public boolean deleteExecutorNodeId(String executorNodeId) {
        return executorNodeDao.deleteById(executorNodeId) > 0;
    }

    @Override
    public boolean registerJobUnitList(List<JobUnitModel> list) {
        return !jobUnitDao.insertOrUpdate(list).isEmpty();
    }

    @Override
    public void saveShift(JobRecordModel jobRecordModel) {
        recordDao.insertOrUpdate(jobRecordModel);
    }

    @Override
    public boolean enableJobUnit(String id) {
        return jobUnitDao.updateById(new JobUnitModel()
                .setId(id)
                .setEnable(0)
        ) > 0;
    }

    @Override
    public boolean disableJobUnit(String id) {
        return jobUnitDao.updateById(new JobUnitModel()
                .setId(id)
                .setEnable(1)
        ) > 0;
    }

    @Override
    public boolean deleteJobUnit(String id) {
        return jobUnitDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteJobRecord(List<String> ids) {
        return recordDao.delete(new LambdaQueryWrapper<JobRecordModel>()
                .in(JobRecordModel::getId, ids)
        ) > 0;
    }

    @Override
    public void run(String... args) throws Exception {
        schedulerThread = new Thread(new JobScheduler(), "job-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        ringConsumerThread = new Thread(new RingConsumer(), "job-ring-handler");
        ringConsumerThread.setDaemon(true);
        ringConsumerThread.start();
        // 初始化60个空列表
        for (int i = 0; i < 60; i++) {
            ringData.put(i, new ArrayList<>());
        }
    }

    @Override
    public PageResult<List<ExecutorNodeModel>> searchPageExecutorNode(SearchSchedulingExecutorNode dto) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<ExecutorNodeModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<ExecutorNodeModel> wrapper = new LambdaQueryWrapper<>();
        //命名空间id
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespaceId())) {
            wrapper.eq(ExecutorNodeModel::getNamespaceId, dto.getEqNamespaceId());
        }
        //执行器名称
        if (StringUtil.isNotNullOrEmpty(dto.getEqExecutorName())) {
            wrapper.eq(ExecutorNodeModel::getExecutorName, dto.getEqExecutorName());
        }
        //组名
        if (StringUtil.isNotNullOrEmpty(dto.getEqGroupName())) {
            wrapper.eq(ExecutorNodeModel::getGroupName, dto.getEqGroupName());
        }
        IPage<ExecutorNodeModel> listPO = executorNodeDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public List<String> getUnitExecutorNameByNamespace(String namespaceId) {
        return jobUnitDao.getUnitExecutorNameByNamespace(namespaceId);
    }

    @Override
    public List<String> getUnitGroupByNamespace(String namespaceId) {
        return jobUnitDao.getUnitGroupByNamespace(namespaceId);
    }

    @Override
    public PageResult<List<JobUnitModel>> searchPageJobUnit(SearchSchedulingJobUnit dto) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<JobUnitModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<JobUnitModel> wrapper = new LambdaQueryWrapper<>();
        //命名空间
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespaceId())) {
            wrapper.eq(JobUnitModel::getNamespaceId, dto.getEqNamespaceId());
        }
        //执行器名称
        if (StringUtil.isNotNullOrEmpty(dto.getEqExecutorName())) {
            wrapper.like(JobUnitModel::getGroupName, dto.getEqExecutorName());
        }
        //组
        if (StringUtil.isNotNullOrEmpty(dto.getEqGroupName())) {
            wrapper.eq(JobUnitModel::getExecutorName, dto.getEqGroupName());
        }
        //名称
        if (StringUtil.isNotNullOrEmpty(dto.getLikeName())) {
            wrapper.like(JobUnitModel::getName, dto.getLikeName());
        }
        IPage<JobUnitModel> listPO = jobUnitDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public JobUnitModel getJobUnitById(String id) {
        return jobUnitDao.selectById(id);
    }

    @Override
    public PageResult<List<JobRecordModel>> searchJobRecord(SearchSchedulingJobRecord dto) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<JobRecordModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<JobRecordModel> wrapper = new LambdaQueryWrapper<>();
        //命名空间
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespaceId())) {
            wrapper.eq(JobRecordModel::getNamespaceId, dto.getEqNamespaceId());
        }
        //执行器名称
        if (StringUtil.isNotNullOrEmpty(dto.getEqExecutorName())) {
            wrapper.eq(JobRecordModel::getExecutorName, dto.getEqExecutorName());
        }
        //组
        if (StringUtil.isNotNullOrEmpty(dto.getEqGroupName())) {
            wrapper.eq(JobRecordModel::getGroupName, dto.getEqGroupName());
        }
        //调度单位id
        if (StringUtil.isNotNullOrEmpty(dto.getEqUnitId())) {
            wrapper.eq(JobRecordModel::getUnitId, dto.getEqUnitId());
        }
        //运行状态码
        if (NumericUtil.isNotNullOrEmpty(dto.getEqCode())) {
            wrapper.eq(JobRecordModel::getCode, dto.getEqCode());
        }
        //计划开始最小时间
        if (NumericUtil.isNotNullOrEmpty(dto.getPlanStartTimeMin())) {
            wrapper.ge(JobRecordModel::getPlanStartTime, dto.getPlanStartTimeMin());
        }
        //计划开始最大时间
        if (NumericUtil.isNotNullOrEmpty(dto.getPlanStartTimeMax())) {
            wrapper.le(JobRecordModel::getPlanStartTime, dto.getPlanStartTimeMax());
        }
        //处理排序
        if (ListUtil.isNotNullOrEmpty(dto.getOrderBy())) {
            dto.getOrderBy().forEach(orderBy -> {
                if (3 == orderBy.getOrderBy()) {
                    if (orderBy.getIsAsc()) {
                        wrapper.orderByAsc(JobRecordModel::getPlanStartTime);
                    } else {
                        wrapper.orderByDesc(JobRecordModel::getPlanStartTime);
                    }
                }
            });
        }
        IPage<JobRecordModel> listPO = recordDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public JobRecordModel getJobRecordById(String id) {
        return recordDao.selectById(id);
    }

    class JobScheduler implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            Integer preSecond = 5;
            try {
                sleep(preSecond * 1000L - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            long perSecondTime = System.currentTimeMillis() / 1000 * 1000;
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    applicationContext.publishEvent(new SchedulingPrebeforeEvent(this,
                            perSecondTime,
                            preSecond * 1000
                    ));
                } catch (Exception e) {
                    log.error("JobScheduler error", e);
                }
                try {
                    sleep(preSecond * 1000L - System.currentTimeMillis() % (preSecond * 1000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                //防止漂移
                perSecondTime = System.currentTimeMillis() / 1000 * 1000;
            }
        }
    }

    class RingConsumer implements Runnable {
        @Override
        public void run() {
            try {
                sleep(1000 - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
                    List<SchedulingExecuteEvent> jobShiftModelList = ringData.remove(nowSecond % 60);
                    //重置槽
                    ringData.put(nowSecond % 60, new ArrayList<>());
                    // 触发任务调度
                    if (jobShiftModelList != null) {
                        jobShiftModelList.forEach(applicationContext::publishEvent);
                    }
                } catch (Exception e) {
                    log.error("ring consumer error", e);
                }
                try {
                    sleep(1000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void addClockworkTime(int slot, SchedulingExecuteEvent event) {
        ringData.get(slot).add(event);
    }


    /**
     * 定时清理过时的执行器节点
     */
    @CarrotJobUnit(value = "clearExpiredExecutorNode", type = EJobUnitType.CORN, typeValue = "29 * * * * ? ")
    public Integer clearExpiredExecutorNode() {
        return executorNodeDao.delete(new LambdaQueryWrapper<ExecutorNodeModel>()
                .lt(ExecutorNodeModel::getUpdateTime, System.currentTimeMillis() - 60 * 1000)
        );
    }

    /**
     * 定时清理过时的执行记录
     *
     * @return
     */
    @CarrotJobUnit(value = "removeJobRecordExpiredData", type = EJobUnitType.CORN, typeValue = "0 0,10,20,30,40,50 * * * ? ")
    public Long removeExpiredData() {
        return recordDao.removeExpiredData(200, 3);
    }
}
