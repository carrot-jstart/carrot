package org.jstart.carrot.console.domain.scheduling.handle;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.application.event.AdminModValue;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingPrebeforeEvent;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobRecordDao;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobUnitDao;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成执行任务
 */
@Component
@RequiredArgsConstructor
public class SchedulingGeneratePlanHandle{
    private final Logger log= LoggerFactory.getLogger(SchedulingGeneratePlanHandle.class);
    private final SchedulingJobUnitDao schedulingJobUnitDao;
    private final SchedulingJobRecordDao schedulingJobRecordDao;
    private int mod;
    private int modValue;

    @EventListener
    public void GeneratePlan(SchedulingPrebeforeEvent event) {
        if(NumericUtil.isNullOrEmpty(mod)){
            return;
        }
        long startTime= event.getStartSecondTime()+3*event.getTimeSlice();
        long endTime=startTime+event.getTimeSlice();
        List<JobRecordModel> data = new ArrayList<>();
        List<JobUnitModel> jobUnits = schedulingJobUnitDao.schedule(mod, modValue);
        for (JobUnitModel item : jobUnits) {
            EJobUnitType jobUnitType = EJobUnitType.getByValue(item.getType());
            switch (jobUnitType) {
                case NONE:
                    //无
                    break;
                case FIXED_SPEED:
                    long last = item.getLastPlanTime();
                    long speed = Long.parseLong(item.getTypeValue());
                    if (speed <= 0) {
                        // 根据业务需求处理非正速度，例如直接返回或抛异常
                        log.error("{}->{}->{}->{}Invalid speed value: {}", item.getNamespaceId(), item.getExecutorName(), item.getGroupName(), item.getName(), speed);
                        continue;
                    }
                    speed=speed * 1000;
                    long runStartTime = last;
                    if (runStartTime < startTime) {
                        long diff = startTime - runStartTime;
                        long steps = (diff + speed - 1) / speed; // 向上取整
                        runStartTime += steps * speed;
                    }
                    boolean savePlanTypeLastEPlanTime = false;
                    while (runStartTime <= endTime) {
                        data.add(new JobRecordModel()
                                .setId(item.getId() + runStartTime)
                                .setNamespaceId(item.getNamespaceId())
                                .setExecutorName(item.getExecutorName())
                                .setGroupName(item.getGroupName())
                                .setUnitId(item.getId())
                                .setUnitName(item.getName())
                                .setPlanStartTime(runStartTime)
                                .setHashValue(Math.abs((item.getId() + runStartTime).hashCode()))
                        );
                        runStartTime += speed;
                        savePlanTypeLastEPlanTime = true;
                    }
                    if (savePlanTypeLastEPlanTime) {
                        schedulingJobUnitDao.updateById(new JobUnitModel()
                                .setId(item.getId())
                                .setLastPlanTime(runStartTime));
                    }
                    break;
                case CORN:
                    //cron
                    CronExpression cron = CronExpression.parse(item.getTypeValue());
                    ZoneId zoneId = ZoneId.systemDefault();
                    ZonedDateTime start = Instant.ofEpochMilli(startTime).atZone(zoneId);
                    ZonedDateTime end = Instant.ofEpochMilli(endTime).atZone(zoneId);
                    // 从开始时间的前一毫秒开始找下一个匹配（确保包含startTimestamp本身）
                    ZonedDateTime current = cron.next(start.minusNanos(1_000_000));
                    long lastPlanTime;
                    while (current != null && !current.isAfter(end)) {
                        lastPlanTime=current.toInstant().toEpochMilli();
                        data.add(new JobRecordModel()
                                .setId(item.getId() + current.toInstant().toEpochMilli())
                                .setNamespaceId(item.getNamespaceId())
                                .setExecutorName(item.getExecutorName())
                                .setGroupName(item.getGroupName())
                                .setUnitId(item.getId())
                                .setUnitName(item.getName())
                                .setPlanStartTime(current.toInstant().toEpochMilli())
                                .setHashValue(Math.abs((item.getId() + current.toInstant().toEpochMilli()).hashCode()))
                        );
                        current = cron.next(current);
                        if(lastPlanTime!=item.getLastPlanTime()){
                            schedulingJobUnitDao.updateById(new JobUnitModel()
                                    .setId(item.getId())
                                    .setLastPlanTime(lastPlanTime));
                        }
                    }
                    break;
            }
        }
        if (ListUtil.isNotNullOrEmpty(data)) {
            schedulingJobRecordDao.insertOrUpdate(data);
        }
    }

    @EventListener
    public void setMod(AdminModValue  adminModValue){
        mod=adminModValue.getMod();
        modValue=adminModValue.getModValue();
    }
}
