package org.jstart.carrot.scheduling;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.jstart.carrot.scheduling.constant.CarrotException;
import org.jstart.carrot.scheduling.constant.ConstantFactory;
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.config.SchedulingExecutorConfig;
import org.jstart.carrot.scheduling.constant.EResultCode;
import org.jstart.carrot.scheduling.constant.ExecutedResult;
import org.jstart.carrot.scheduling.support.SchedulingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 调度执行端 gRPC 服务实现。
 * <p>
 * 主要职责：
 * </p>
 * <ul>
 * <li>接收 Admin 下发的任务执行请求并异步执行</li>
 * <li>扫描并注册本地 {@link CarrotJobUnit} 任务单元（由
 * {@link CarrotJobAnnotationBeanPostProcessor} 完成）</li>
 * <li>向 Admin 注册执行器并定时上报心跳/负载健康度</li>
 * </ul>
 */
@Order(Integer.MIN_VALUE)
public class ExecutorServer extends SchedulingExecutorServiceGrpc.SchedulingExecutorServiceImplBase
        implements DisposableBean, CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorServer.class);

    private final Object heartbeatLock = new Object();
    private volatile boolean running = true;
    private volatile SchedulingClient.HostPort currentAdmin;
    private final SchedulingAdmin.ServiceKey serviceKey;
    private volatile StreamObserver<SchedulingAdmin.HeartbeatRequest> heartbeatRequestObserver;
    private final GrpcServer grpcServer;
    private final ScheduledExecutorService heartbeatScheduler;
    private final SchedulingExecutorConfig schedulingExecutorConfig;
    private final SchedulingClient schedulingClient;
    private final String accessToken;

    private final List<SchedulingUnit> schedulingUnitList= new ArrayList<>();

    /**
     * 任务方法缓存 key: jobName
     */
    private final Map<String, Method> jobMethodCache = new ConcurrentHashMap<>();
    /**
     * 任务 Bean 缓存 key: jobName
     */
    private final Map<String, Object> jobBeanCache = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor executor;

    /**
     * 构造方法

     */
    public ExecutorServer(GrpcServer grpcServer,
                          SchedulingExecutorConfig schedulingExecutorConfig,
                          SchedulingClient schedulingClient,String accessToken) {
        this.grpcServer = grpcServer;
        this.schedulingExecutorConfig = schedulingExecutorConfig;
        this.schedulingClient = schedulingClient;
        this.accessToken = accessToken;
        this.serviceKey= SchedulingAdmin.ServiceKey.newBuilder()
                .setNamespace(schedulingExecutorConfig.getNamespace())
                .setGroup(schedulingExecutorConfig.getGroup())
                .setServiceName(schedulingExecutorConfig.getName())
                .build();
        this.executor = new ThreadPoolExecutor(schedulingExecutorConfig.getThreadPoolCorePoolSize(),
                schedulingExecutorConfig.getThreadPoolMaxPoolSize(),
                schedulingExecutorConfig.getThreadPoolKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(schedulingExecutorConfig.getThreadPoolQueueCapacity()),
                new ThreadPoolExecutor.CallerRunsPolicy());

        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "carrot-scheduling-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * gRPC 入口：接收任务执行请求。
     * <p>
     * 为避免阻塞 gRPC 线程，方法会先立即返回响应，再将实际执行提交到内部线程池。
     * </p>
     *
     * @param request          任务请求
     * @param responseObserver gRPC 响应观察者
     */
    @Override
    public void runTask(
            SchedulingExecutor.RunTaskRequest request,
            StreamObserver<Empty> responseObserver) {
        // 提前返回
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
        // 启动线程
        executor.execute(() -> {
            try {
//                logger.info("receive task request,taskId->{},unit->{}", request.getTaskId(), request.getUnitName());
                if (!ConstantFactory.SECRET.equals(request.getSecret())) {
                    schedulingClient.sendTaskReturn(request.getTaskId(),
                            EResultCode.FORBIDDEN_ACCESS.getCode(),
                            EResultCode.FORBIDDEN_ACCESS.getMessage(),
                            "",
                            List.of());
                    return;
                }
                // 开启线程日志
                CarrotLog.startLogRecord();
                // 启动方法
                ExecutedResult<String> stringExecutedResult = this.executeJob(request.getUnitName());
                schedulingClient.sendTaskReturn(request.getTaskId(),
                        stringExecutedResult.getCode(),
                        stringExecutedResult.getMsg(),
                        stringExecutedResult.getData(),
                        CarrotLog.endLogRecord());
            } catch (Exception e) {
                e.printStackTrace();
                schedulingClient.sendTaskReturn(request.getTaskId(),
                        EResultCode.ERROR.getCode(),
                        EResultCode.ERROR.getMessage(),
                        "",
                        CarrotLog.endLogRecord());
            } finally {
                // 清理现场激励日志
                CarrotLog.clear();
            }
        });
    }

    /**
     * 计算线程池健康值，范围 [0, 1]
     *
     * @param executor     线程池
     * @param threadWeight 线程部分权重
     * @param queueWeight  队列部分权重
     * @return 健康值，1 表示完全空闲健康，0 表示完全饱和/超载
     */
    public static double computeHealth(ThreadPoolExecutor executor, double threadWeight, double queueWeight) {
        if (executor == null || executor.isShutdown()) {
            return 0.0; // 已关闭的池子健康度为 0
        }

        // 1. 线程部分剩余容量
        int maxThreads = executor.getMaximumPoolSize();
        int activeThreads = executor.getActiveCount();
        double threadRemaining;

        if (maxThreads <= 0) {
            // 理论上不会发生，兜底
            threadRemaining = 1.0;
        } else if (maxThreads == Integer.MAX_VALUE) {
            // 无界线程池，很少见，可认为线程不是瓶颈
            threadRemaining = 1.0;
        } else {
            double activeRatio = (double) activeThreads / maxThreads;
            threadRemaining = 1.0 - Math.min(1.0, activeRatio);
        }

        // 2. 队列部分剩余容量
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueSize = queue.size();
        int queueCapacity = getQueueCapacity(queue);
        double queueRemaining;

        if (queueCapacity <= 0) {
            queueRemaining = 1.0; // 队列容量未知（如 SynchronousQueue），按无积压处理
        } else if (queueCapacity == Integer.MAX_VALUE) {
            // 无界队列，永远不会满（但可能内存溢出，健康度可认为仅由线程部分决定）
            queueRemaining = 1.0;
        } else {
            double usedRatio = (double) queueSize / queueCapacity;
            queueRemaining = 1.0 - Math.min(1.0, usedRatio);
        }

        // 3. 加权平均
        double totalWeight = threadWeight + queueWeight;
        if (totalWeight <= 0) {
            return 0.0;
        }
        double health = (threadWeight * threadRemaining + queueWeight * queueRemaining) / totalWeight;

        // 限制结果在 [0, 1] 范围内
        return Math.max(0.0, Math.min(1.0, health));
    }

    /**
     * 获取队列容量（有界队列返回实际容量，无界或未知容量返回 Integer.MAX_VALUE）
     */
    private static int getQueueCapacity(BlockingQueue<?> queue) {
        // 对于 ArrayBlockingQueue, LinkedBlockingQueue 等，remainingCapacity() 会返回剩余空间，
        // 但总容量 = 剩余容量 + 当前大小
        if (queue == null) {
            return 0;
        }
        // 尝试获取总容量（通过反射或直接计算）
        // 简单方法：调用 remainingCapacity()，但需要知道总容量。
        // 这里采用常规判断：
        if (queue instanceof java.util.concurrent.ArrayBlockingQueue) {
            // ArrayBlockingQueue 容量固定
            int remaining = queue.remainingCapacity();
            return remaining + queue.size();
        } else if (queue instanceof java.util.concurrent.LinkedBlockingQueue) {
            // LinkedBlockingQueue 容量可能为 Integer.MAX_VALUE
            int remaining = queue.remainingCapacity();
            if (remaining == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return remaining + queue.size();
        } else {
            // 其他类型（如 SynchronousQueue、PriorityBlockingQueue、无界队列等）
            // 这里简单处理：若 remainingCapacity() 返回 0 且 size() > 0 则为有界？太复杂，多数情况返回
            // Integer.MAX_VALUE
            try {
                int remaining = queue.remainingCapacity();
                if (remaining == Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
                return remaining + queue.size();
            } catch (Exception e) {
                return Integer.MAX_VALUE;
            }
        }
    }

    /**
     * 执行任务
     *
     * @param jobName 任务单元名称（通常对应 {@link CarrotJobUnit#value()}）
     * @return 执行结果（成功/失败及返回信息）
     */
    public ExecutedResult<String> executeJob(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            return ExecutedResult.failed("beanName/jobName is blank", EResultCode.VALIDATE_FAILED);
        }
        Method method = jobMethodCache.get(jobName);
        if (method == null) {
            return ExecutedResult.failed("No job found for jobName=" + jobName,
                    EResultCode.VALIDATE_FAILED);
        }
        final Object targetBean;
        try {
            targetBean = jobBeanCache.get(jobName);
        } catch (CarrotException e) {
            return ExecutedResult.failed(e.getMessage(), e.getCode());
        }
        try {
            Object result = method.invoke(targetBean);
            if (result != null) {
                return ExecutedResult.success(result.toString());
            } else {
                return ExecutedResult.success();
            }
        } catch (Exception e) {
            return ExecutedResult.failed(e.getMessage(), EResultCode.ERROR);
        }
    }

    /**
     * 注册一个任务单元到本地缓存，并同步注册到 Admin。
     *
     * @param beanName   Spring Bean 名称
     * @param bean       Bean 实例
     * @param method     任务方法（要求无参）
     * @param annotation 任务单元注解
     */
    public void registerJob(String beanName, Object bean, Method method, CarrotJobUnit annotation) {
        String unitName = annotation.value();
        Parameter[] parameters = method.getParameters();
        if (parameters.length > 0) {
            logger.error("{}->{} method should be empty.", beanName, unitName);
            return;
        }
        schedulingUnitList.add(new SchedulingUnit(unitName, annotation.type(), annotation.typeValue()));
        jobBeanCache.put(unitName, bean);
        jobMethodCache.put(unitName, method);
    }

    /**
     * 容器销毁时关闭心跳任务并向 Admin 注销执行器实例。
     */
    @Override
    public void destroy() {
        running = false;
        synchronized (heartbeatLock) {
            if (heartbeatRequestObserver != null) {
                try {
                    heartbeatRequestObserver.onCompleted();
                } catch (Exception ignored) {
                }
                heartbeatRequestObserver = null;
            }
        }
        try {
            heartbeatScheduler.shutdownNow();
        } catch (Exception ignored) {
        }
    }

    /**
     * 标记心跳连接断开：关闭心跳流并清理客户端连接。
     */
    private void markHeartbeatDisconnected() {
        synchronized (heartbeatLock) {
            if (heartbeatRequestObserver != null) {
                try {
                    heartbeatRequestObserver.onCompleted();
                } catch (Exception ignored) {
                }
            }
            heartbeatRequestObserver = null;
        }
        try {
            schedulingClient.closeChannel();
        } catch (Exception ignored) {
        }
    }


    /**
     * 应用启动后启动 gRPC Server，并完成执行器注册/心跳上报。
     *
     * @param args 启动参数
     * @throws Exception 启动/注册过程中可能抛出的异常
     */
    @Override
    public void run(String... args) throws Exception {
        if (!grpcServer.start(this)) {
            return;
        }
        connectRegisterAndOpenHeartbeatStream(null);
        // 开启心跳
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            StreamObserver<SchedulingAdmin.HeartbeatRequest> observer = heartbeatRequestObserver;
            if (observer == null) {
                connectRegisterAndOpenHeartbeatStream(currentAdmin);
                observer = heartbeatRequestObserver;
            }
            if (observer == null) {
                return;
            }
            try {
                observer.onNext(SchedulingAdmin.HeartbeatRequest.newBuilder()
                        .setAccessToken(
                                accessToken == null ? "" : accessToken)
                        .setServiceKey(serviceKey)
                        .setIp(schedulingExecutorConfig.getIp())
                        .setPort(schedulingExecutorConfig.getPort())
                        .setSecret(ConstantFactory.SECRET)
                        .setWeight(computeHealth(executor,schedulingExecutorConfig.getThreadWeight(),schedulingExecutorConfig.getQueueWeight()))
                        .build());
            } catch (Exception e) {
                markHeartbeatDisconnected();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    /**
     * 选择服务端节点、执行注册并打开新的心跳流。
     *
     * @param exclude 需要排除的节点（可为 null）
     */
    private void connectRegisterAndOpenHeartbeatStream(SchedulingClient.HostPort exclude) {
        synchronized (heartbeatLock) {
            if (!running) {
                return;
            }
            if (heartbeatRequestObserver != null) {
                return;
            }
            try {
                schedulingClient.refreshServerNodes();
            } catch (Exception ignored) {
            }
            try {
                currentAdmin = exclude == null ? schedulingClient.switchToRandomServerNode()
                        : schedulingClient.switchToNextRandomServerNode(currentAdmin);
            } catch (Exception e) {
                logger.warn("carrot scheduling: select admin node failed: {}", e.getMessage(),e);
                return;
            }

            SchedulingAdmin.Reply reply = schedulingClient.register(serviceKey,schedulingUnitList);
            if (reply.getCode() != EResultCode.SUCCESS.getCode()) {
                logger.warn("carrot scheduling: register failed, admin={}:{}, code={}, msg={}",
                        currentAdmin == null ? "" : currentAdmin.host(),
                        currentAdmin == null ? -1 : currentAdmin.port(),
                        reply.getCode(),
                        reply.getMessage());
                markHeartbeatDisconnected();
                return;
            }
            logger.info("carrot scheduling: registered, admin={}:{}",
                    currentAdmin == null ? "" : currentAdmin.host(),
                    currentAdmin == null ? -1 : currentAdmin.port());

            try {
                heartbeatRequestObserver = schedulingClient.openHeartbeatStream(new StreamObserver<>() {
                    @Override
                    public void onNext(SchedulingAdmin.Reply value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.warn("carrot scheduling: heartbeat stream error: {}", t.getMessage());
                        markHeartbeatDisconnected();
                    }

                    @Override
                    public void onCompleted() {
                        logger.warn("carrot scheduling: heartbeat stream completed");
                        markHeartbeatDisconnected();
                    }
                });
                logger.info("carrot scheduling: heartbeat stream opened, admin={}:{}",
                        currentAdmin == null ? "" : currentAdmin.host(),
                        currentAdmin == null ? -1 : currentAdmin.port());
            } catch (Exception e) {
                logger.warn("carrot scheduling: open heartbeat stream failed: {}", e.getMessage());
                markHeartbeatDisconnected();
            }
        }
    }
}
