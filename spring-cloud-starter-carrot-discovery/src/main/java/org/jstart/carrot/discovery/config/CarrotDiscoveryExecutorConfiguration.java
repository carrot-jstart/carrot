package org.jstart.carrot.discovery.config;

import org.jstart.carrot.discovery.grpc.Discovery;
import org.jstart.carrot.discovery.support.CarrotDiscoveryClient;
import org.jstart.carrot.discovery.support.CarrotDiscoveryRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Carrot Discovery 客户端侧自动装配。
 * <p>
 * 注册 Discovery gRPC 客户端与注册器等核心 Bean。
 * </p>
 */
@Configuration
@EnableConfigurationProperties({ DiscoveryConfig.class })
public class CarrotDiscoveryExecutorConfiguration {
    /**
     * Discovery gRPC 客户端 Bean。
     *
     * @param discoveryConfig 客户端配置
     * @return CarrotDiscoveryClient
     */
    @Bean
    @ConditionalOnMissingBean
    public CarrotDiscoveryClient carrotDiscoveryClient(DiscoveryConfig discoveryConfig) {
        return new CarrotDiscoveryClient(discoveryConfig);
    }

    /**
     * 注册器 Bean：负责服务注册/心跳/注销等生命周期。
     *
     * @param carrotDiscoveryClient gRPC 客户端
     * @param discoveryConfig       客户端配置
     * @param environment           Spring Environment
     * @return CarrotDiscoveryRegistrar
     */
    @Bean
    @ConditionalOnMissingBean
    public CarrotDiscoveryRegistrar carrotDiscoveryRegistrar(CarrotDiscoveryClient carrotDiscoveryClient,
            DiscoveryConfig discoveryConfig,
            Environment environment) {
        return new CarrotDiscoveryRegistrar(carrotDiscoveryClient, discoveryConfig, environment);
    }

    @Bean
    @ConditionalOnClass(ReactiveDiscoveryClient.class)
    @ConditionalOnMissingBean(ReactiveDiscoveryClient.class)
    public ReactiveDiscoveryClient carrotReactiveDiscoveryClient(CarrotDiscoveryClient carrotDiscoveryClient,
            DiscoveryConfig discoveryConfig) {
        return new ReactiveDiscoveryClient() {
            @Override
            public String description() {
                return "Carrot Reactive Discovery Client";
            }

            @Override
            public Flux<ServiceInstance> getInstances(String serviceId) {
                Discovery.ServiceKey serviceKey = Discovery.ServiceKey.newBuilder()
                        .setNamespace(nullToEmpty(discoveryConfig.getNamespace()))
                        .setGroup(nullToEmpty(discoveryConfig.getGroup()))
                        .setServiceName(serviceId)
                        .build();

                return Flux.defer(() -> {
                    Discovery.ListInstancesResponse response = carrotDiscoveryClient.listInstances(serviceKey, true);
                    if (response == null || response.getInstancesCount() == 0) {
                        return Flux.empty();
                    }
                    List<ServiceInstance> instances = response.getInstancesList().stream()
                            .map(instance -> toServiceInstance(serviceId, instance))
                            .toList();
                    return Flux.fromIterable(instances);
                });
            }

            @Override
            public Flux<String> getServices() {
                return Flux.empty();
            }
        };
    }

    private static ServiceInstance toServiceInstance(String serviceId, Discovery.Instance instance) {
        Map<String, String> metadata = instance.getMetadataMap();
        boolean secure = Boolean.parseBoolean(metadata.getOrDefault("secure", "false"));
        return new DefaultServiceInstance(
                instance.getInstanceId(),
                serviceId,
                instance.getIp(),
                instance.getPort(),
                secure,
                metadata
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
