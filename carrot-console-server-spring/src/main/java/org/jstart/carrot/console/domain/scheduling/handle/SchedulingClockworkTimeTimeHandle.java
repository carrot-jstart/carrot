package org.jstart.carrot.console.domain.scheduling.handle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.comm.entity.PointXyz;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.application.event.AdminModValue;
import org.jstart.carrot.console.domain.scheduling.SchedulingDomainService;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingExecuteEvent;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingPrebeforeEvent;
import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.infrastructure.repository.SchedulingExecutorNodeDao;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobRecordDao;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  定时任务执行时间处理
 */
@Component
@RequiredArgsConstructor
public class SchedulingClockworkTimeTimeHandle{
    private final SchedulingDomainService schedulingDomainService;
    private final SchedulingJobRecordDao schedulingJobRecordDao;
    private final SchedulingExecutorNodeDao schedulingExecutorNodeDao;

    private int mod;
    private int modValue;
    @EventListener
    public void ClockworkTimeTime(SchedulingPrebeforeEvent event) {
        if(NumericUtil.isNullOrEmpty(mod)){
            return;
        }
        //将待执行任务加入到环形队列中
        List<JobRecordModel> jobRecordModelList = schedulingJobRecordDao.getRecentPlan(
                event.getStartSecondTime()+event.getTimeSlice(),event.getStartSecondTime()+event.getTimeSlice()*2,
                mod, modValue
        );
        if(jobRecordModelList.isEmpty()){
           return;
        }
        List<PointXyz<String,String,String>> list = jobRecordModelList.stream()
                .map(item -> new PointXyz<String,String,String>()
                        .setValueX(item.getNamespaceId())
                        .setValueY(item.getExecutorName())
                        .setValueZ(item.getGroupName())
                )
                .distinct()
                .toList();
        if(ListUtil.isNotNullOrEmpty(list)){
            List<ExecutorNodeModel> nodeModelList = schedulingExecutorNodeDao.selectList(
                    new LambdaQueryWrapper<ExecutorNodeModel>()
                            .in(ExecutorNodeModel::getNamespaceId, list.stream().map(PointXyz::getValueX).distinct().toList())
                            .in(ExecutorNodeModel::getExecutorName, list.stream().map(PointXyz::getValueY).distinct().toList())
                            .in(ExecutorNodeModel::getGroupName, list.stream().map(PointXyz::getValueZ).distinct().toList())
                            //按照权重进行降序
                            .orderByDesc(ExecutorNodeModel::getWeight)
            );
            Map<PointXyz<String, String, String>, ExecutorNodeModel> executorNode= new HashMap<>();
            list.forEach(item -> nodeModelList.stream()
                    .filter(node -> node.getNamespaceId().equals(item.getValueX())&&
                            node.getExecutorName().equals(item.getValueY()) &&
                            node.getGroupName().equals(item.getValueZ()))
                    .findFirst()
                    .ifPresent(node -> executorNode.put(item,node)));

            List<JobRecordModel> noFundExecuteNodelList = new ArrayList<>();
            jobRecordModelList.forEach(item -> {
                PointXyz<String, String, String> mapKey = new PointXyz<>(item.getNamespaceId(), item.getExecutorName(), item.getGroupName());
                if(executorNode.containsKey(mapKey)){
                    ExecutorNodeModel executorNodeModel = executorNode.get(mapKey);
                    schedulingDomainService.addClockworkTime((int) ((item.getPlanStartTime() % 60000) / 1000),new SchedulingExecuteEvent(
                            this,
                            item.getId(),
                            executorNodeModel.getIp(),
                            executorNodeModel.getPort(),
                            executorNodeModel.getSecret(),
                            item.getUnitId(),
                            item.getUnitName()
                    ));
                }else {
                    item.setCode(EResultCode.NOT_FOUND.getCode());
                    item.setMessage(EResultCode.NOT_FOUND.getMessage());
                    noFundExecuteNodelList.add(item);
                }
            });
            if(ListUtil.isNotNullOrEmpty(noFundExecuteNodelList)){
                schedulingJobRecordDao.updateById(noFundExecuteNodelList);
            }
        }
    }


    @EventListener
    public void setMod(AdminModValue adminModValue){
        mod=adminModValue.getMod();
        modValue=adminModValue.getModValue();
    }
}
