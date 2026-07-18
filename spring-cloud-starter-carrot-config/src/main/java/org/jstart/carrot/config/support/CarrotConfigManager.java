package org.jstart.carrot.config.support;

import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.comm.Subscription;
import org.jstart.carrot.config.config.CarrotConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;

/**
 * 配置管理器
 * <p>
 *     配置管理器，用于监听配置变更并触发 Spring 环境更新/字段刷新等后续动作。
 *     配置变更会发布 {@link CarrotConfigChangedEvent} 事件，用于触发 Spring 环境更新/字段刷新等后续动作。
 */
public class CarrotConfigManager implements CommandLineRunner, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CarrotConfigManager.class);

    private final CarrotConfigService subscriber;
    private final CarrotConfigProperties properties;
    private final ApplicationEventPublisher publisher;
    private final List<Subscription> subscriptions = new java.util.ArrayList<>();


    public CarrotConfigManager(CarrotConfigService subscriber,
                               CarrotConfigProperties properties,
                               ApplicationEventPublisher publisher) {
        this.subscriber = Objects.requireNonNull(subscriber, "subscriber");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    @Override
    public void run(String... args) {
        if (!properties.isRefreshEnabled()) {
            return;
        }

        List<ServerKey> files = properties.getFiles();
        if (files == null || files.isEmpty()) {
            return;
        }

        for (ServerKey file : files) {
            if (file == null || file.dataId() == null || file.dataId().trim().isEmpty()) {
                continue;
            }
             subscriber.subscribe(
                    file.namespace(),
                    file.group(),
                    file.dataId(),
                    (key, oldSnapshot, newSnapshot) -> publisher.publishEvent(
                            new CarrotConfigChangedEvent(this, key, oldSnapshot, newSnapshot)));
        }
    }
    @Override
    public void destroy() {
        for (Subscription sub : List.copyOf(subscriptions)) {
            if (sub == null) {
                continue;
            }
            try {
                sub.close();
            } catch (Exception e) {
                logger.warn("carrot config manager close subscription error: {}", e.getMessage());
            }
        }
        subscriptions.clear();
    }

}
