package org.jstart.carrot.scheduling.config;

import org.jstart.carrot.scheduling.*;
import org.jstart.carrot.scheduling.support.SchedulingClient;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carrot Scheduling 执行端自动装配。
 * <p>
 * 根据配置选择 Admin 注册方式（例如 nacos/none），并注册任务扫描器、gRPC Server 与执行端服务实现等核心 Bean。
 * </p>
 */
@Configuration
@EnableConfigurationProperties({ SchedulingAdminConfig.class, SchedulingExecutorConfig.class })
public class CarrotSchedulingExecutorConfiguration {


    /**
     * 任务单元扫描与注册 BeanPostProcessor。
     *
     * @param executorServer 执行端服务
     * @return BeanPostProcessor
     */
    @Bean
    public static BeanPostProcessor carrotJobBeanPostProcessor(ExecutorServer executorServer) {
        return new CarrotJobAnnotationBeanPostProcessor(executorServer);
    }

    /**
     * gRPC Server Bean。
     *
     * @param schedulingExecutorConfig 执行端配置
     * @return GrpcServer
     */
    @Bean
    public static GrpcServer grpcServer(SchedulingExecutorConfig schedulingExecutorConfig) {
        return new GrpcServer(schedulingExecutorConfig.getPort());
    }

    @Bean
    public static ExecutorServer executorServer(GrpcServer grpcServer,
                                                SchedulingExecutorConfig schedulingExecutorConfig,
                                                SchedulingClient schedulingClient,
                                                SchedulingAdminConfig schedulingAdminConfig
                                                ) {
        return new ExecutorServer(grpcServer, schedulingExecutorConfig, schedulingClient,schedulingAdminConfig.getAccessToken());
    }

    @Bean
    public static SchedulingClient schedulingClient(SchedulingAdminConfig schedulingExecutorConfig) {
        return new SchedulingClient(schedulingExecutorConfig);
    }

}
