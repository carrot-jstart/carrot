package org.jstart.carrot.config.config;

import org.jstart.carrot.config.support.CarrotConfigClient;
import org.jstart.carrot.config.support.CarrotConfigManager;
import org.jstart.carrot.config.support.CarrotConfigRepository;
import org.jstart.carrot.config.support.CarrotConfigService;
import org.jstart.carrot.config.support.CarrotConfigSpringRefresher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Carrot Config 客户端侧自动装配。
 * <p>
 * 负责注册 gRPC 客户端、缓存仓库、配置管理器以及 Spring 刷新器等核心 Bean。
 * </p>
 */
@Configuration
@EnableConfigurationProperties({ CarrotConfigProperties.class })
public class CarrotConfigExecutorConfiguration {
    /**
     * gRPC 客户端 Bean。
     *
     * @param properties 配置属性
     * @return CarrotConfigClient
     */
    @Bean
    @ConditionalOnMissingBean
    public CarrotConfigClient carrotConfigClient(CarrotConfigProperties properties) {
        return new CarrotConfigClient(properties);
    }

    /**
     * 本地缓存仓库 Bean。
     *
     * @return CarrotConfigRepository
     */
    @Bean
    @ConditionalOnMissingBean
    public CarrotConfigRepository carrotConfigRepository() {
        return new CarrotConfigRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public CarrotConfigService carrotConfigSubscriber(CarrotConfigClient client, CarrotConfigProperties properties) {
        return new CarrotConfigService(client, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.cloud.carrot.config", name = "refreshEnabled", havingValue = "true", matchIfMissing = true)
    public CarrotConfigManager carrotConfigManager(CarrotConfigService subscriber,
                                                   CarrotConfigProperties properties,
                                                   ApplicationEventPublisher publisher) {
        return new CarrotConfigManager(subscriber, properties, publisher);
    }

    /**
     * Spring 刷新器 Bean，用于将变更写入 Environment 并尝试刷新 {@code @Value} 字段。
     *
     * @param applicationContext Spring 上下文
     * @param environment        Spring Environment
     * @return CarrotConfigSpringRefresher
     */
    @Bean
    @ConditionalOnMissingBean
    public CarrotConfigSpringRefresher carrotConfigSpringRefresher(ApplicationContext applicationContext,
            ConfigurableEnvironment environment) {
        return new CarrotConfigSpringRefresher(applicationContext, environment);
    }
}
