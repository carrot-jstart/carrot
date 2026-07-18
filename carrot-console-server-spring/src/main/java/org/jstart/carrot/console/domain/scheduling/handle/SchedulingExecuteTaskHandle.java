package org.jstart.carrot.console.domain.scheduling.handle;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.domain.scheduling.event.SchedulingExecuteEvent;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.TaskLockModel;
import org.jstart.carrot.console.infrastructure.repository.SchedulingExecuteTaskLockDao;
import org.jstart.carrot.console.infrastructure.repository.SchedulingJobRecordDao;
import org.jstart.carrot.scheduling.SchedulingExecutor;
import org.jstart.carrot.scheduling.SchedulingExecutorServiceGrpc;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务执行处理
 */
@Component
@RequiredArgsConstructor
public class SchedulingExecuteTaskHandle implements ApplicationListener<SchedulingExecuteEvent> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SchedulingExecuteTaskHandle.class);
    private final SchedulingExecuteTaskLockDao schedulingExecuteTaskLockDao;
    private final SchedulingJobRecordDao schedulingJobRecordDao;

    @Override
    public void onApplicationEvent(SchedulingExecuteEvent event) {
        //利用主键唯一尝试对任务加锁
        try {
            schedulingExecuteTaskLockDao.insert(new TaskLockModel().setValue(event.getId()));
        } catch (Exception e) {
            //加锁失败，直接返回
            return;
        }

        ManagedChannel managedChannel = null;
        JobRecordModel jobRecordModel = new JobRecordModel();
        try {
            jobRecordModel.setId(event.getId());
            jobRecordModel.setIp(event.getIp());
            jobRecordModel.setPort(event.getPort());
            jobRecordModel.setSecret(event.getSecret());
            jobRecordModel.setActualStartTime(System.currentTimeMillis());
            managedChannel = ManagedChannelBuilder.forAddress(event.getIp(), event.getPort())
                    .usePlaintext()
                    .build();
            SchedulingExecutorServiceGrpc.SchedulingExecutorServiceBlockingStub stub = SchedulingExecutorServiceGrpc
                    .newBlockingStub(managedChannel);
            SchedulingExecutor.RunTaskRequest request = SchedulingExecutor.RunTaskRequest.newBuilder()
                    .setSecret(event.getSecret())
                    .setTaskId(event.getId())
                    .setUnitName(event.getUnitName())
                    .build();
            stub.runTask(request);
            jobRecordModel.setCode(EResultCode.SUCCESS.getCode());
            jobRecordModel.setMessage(EResultCode.SUCCESS.getMessage());
        } catch (StatusRuntimeException e) {
            jobRecordModel.setCode(EResultCode.ERROR.getCode());
            jobRecordModel.setMessage(e.getStatus().toString());
            logger.warn("runTask failed, {}:{}, taskId={}, status={}",
                    event.getIp(),
                    event.getPort(),
                    event.getId(),
                    e.getStatus());
        } catch (Exception e) {
            jobRecordModel.setCode(EResultCode.ERROR.getCode());
            jobRecordModel.setMessage(e.getMessage());
            logger.warn("runTask failed, {}:{}, taskId={}, error={}",
                    event.getIp(),
                    event.getPort(),
                    event.getId(),
                    e.getMessage());
        } finally {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                managedChannel.shutdownNow();
                try {
                    if (!managedChannel.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.warn("Channel did not terminate in time");
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            //更新任务记录
            schedulingJobRecordDao.updateById(jobRecordModel);
            //删除锁
            schedulingExecuteTaskLockDao.deleteById(event.getId());
        }
    }

}
