package org.jstart.carrot.discovery.support;

import org.jstart.carrot.discovery.EResultCode;
import org.jstart.carrot.discovery.config.DiscoveryConfig;
import org.jstart.carrot.discovery.grpc.Discovery;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Carrot Discovery 的服务注册器。
 * <p>
 * 在应用启动完成后执行：
 * </p>
 * <ul>
 *     <li>根据配置构造 ServiceKey 与 Instance 信息并向服务端注册</li>
 *     <li>建立心跳双向流并按固定间隔发送心跳</li>
 *     <li>当心跳流断开或异常时，切换节点并重新注册/重连</li>
 * </ul>
 */
public class CarrotDiscoveryRegistrar implements CommandLineRunner, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CarrotDiscoveryRegistrar.class);

    private final CarrotDiscoveryClient discoveryClient;
    private final DiscoveryConfig discoveryConfig;
    private final Environment environment;

    private final ScheduledExecutorService heartbeatScheduler;
    private final Object heartbeatLock = new Object();
    private volatile Discovery.ServiceKey serviceKey;
    private volatile String instanceId;
    private volatile String ip;
    private volatile int port;
    private volatile Discovery.Instance instance;
    private volatile StreamObserver<Discovery.HeartbeatRequest> heartbeatRequestObserver;
    private volatile CarrotDiscoveryClient.HostPort currentAdmin;
    private volatile boolean running = true;

    /**
     * @param discoveryClient discovery gRPC 客户端
     * @param discoveryConfig 客户端配置
     * @param environment     Spring Environment
     */
    public CarrotDiscoveryRegistrar(CarrotDiscoveryClient discoveryClient, DiscoveryConfig discoveryConfig,
            Environment environment) {
        this.discoveryClient = Objects.requireNonNull(discoveryClient, "discoveryClient");
        this.discoveryConfig = Objects.requireNonNull(discoveryConfig, "discoveryConfig");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "carrot-discovery-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 应用启动后执行注册与心跳初始化。
     *
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        String serviceName = discoveryConfig.getService();
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = environment.getProperty("spring.application.name");
        }
        if (serviceName == null || serviceName.isBlank()) {
            logger.warn("carrot discovery: serviceName is empty, skip register");
            return;
        }
        String namespace = (discoveryConfig.getNamespace() == null || discoveryConfig.getNamespace().isBlank())
                ? "public"
                : discoveryConfig.getNamespace();
        String group = (discoveryConfig.getGroup() == null || discoveryConfig.getGroup().isBlank()) ? "DEFAULT_GROUP"
                : discoveryConfig.getGroup();
        this.serviceKey = Discovery.ServiceKey.newBuilder()
                .setNamespace(namespace)
                .setGroup(group)
                .setServiceName(serviceName)
                .build();

        this.ip = (discoveryConfig.getIp() == null || discoveryConfig.getIp().isBlank()) ? "127.0.0.1"
                : discoveryConfig.getIp();
        this.port = resolvePort(discoveryConfig.getPort());
        if (this.port <= 0) {
            logger.warn("carrot discovery: port <= 0, skip register");
            return;
        }

        this.instanceId = UUID.randomUUID().toString();

        this.instance = Discovery.Instance.newBuilder()
                .setInstanceId(instanceId)
                .setIp(ip)
                .setPort(port)
                .setWeight(discoveryConfig.getWeight())
                .putAllMetadata(discoveryConfig.getMetadata() == null ? Map.of() : discoveryConfig.getMetadata())
                .build();

        logger.info("carrot discovery: start register, service={}/{}/{}, instance={} {}:{}",
                namespace, group, serviceName, instanceId, ip, port);

        connectRegisterAndOpenHeartbeatStream(null);

        int interval = 5;
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            StreamObserver<Discovery.HeartbeatRequest> observer = heartbeatRequestObserver;
            if (observer == null) {
                connectRegisterAndOpenHeartbeatStream(currentAdmin);
                observer = heartbeatRequestObserver;
            }
            if (observer == null) {
                return;
            }
            try {
                observer.onNext(Discovery.HeartbeatRequest.newBuilder()
                        .setAccessToken(
                                discoveryConfig.getAccessToken() == null ? "" : discoveryConfig.getAccessToken())
                        .setService(serviceKey)
                        .setInstanceId(instanceId)
                        .setIp(ip)
                        .setPort(port)
                        .build());
            } catch (Exception e) {
                markHeartbeatDisconnected();
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    /**
     * 容器销毁时停止心跳、注销实例并释放资源。
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
        if (serviceKey != null && instanceId != null && port > 0) {
            try {
                discoveryClient.deregister(serviceKey, instanceId, ip, port);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 选择服务端节点、执行注册并打开新的心跳流。
     *
     * @param exclude 需要排除的节点（可为 null）
     */
    private void connectRegisterAndOpenHeartbeatStream(CarrotDiscoveryClient.HostPort exclude) {
        synchronized (heartbeatLock) {
            if (!running) {
                return;
            }
            if (heartbeatRequestObserver != null) {
                return;
            }
            try {
                discoveryClient.refreshServerNodes();
            } catch (Exception ignored) {
            }
            try {
                currentAdmin = exclude == null ? discoveryClient.switchToRandomServerNode()
                        : discoveryClient.switchToNextRandomServerNode(exclude);
            } catch (Exception e) {
                logger.warn("carrot discovery: select admin node failed: {}", e.getMessage());
                return;
            }

            Discovery.Reply reply = discoveryClient.register(serviceKey, instance);
            if (reply.getCode() != EResultCode.SUCCESS.getCode()) {
                logger.warn("carrot discovery: register failed, admin={}:{}, code={}, msg={}",
                        currentAdmin == null ? "" : currentAdmin.host(),
                        currentAdmin == null ? -1 : currentAdmin.port(),
                        reply.getCode(),
                        reply.getMessage());
                markHeartbeatDisconnected();
                return;
            }
            logger.info("carrot discovery: registered, admin={}:{}, instanceId={}",
                    currentAdmin == null ? "" : currentAdmin.host(),
                    currentAdmin == null ? -1 : currentAdmin.port(),
                    reply.getData());

            try {
                heartbeatRequestObserver = discoveryClient.openHeartbeatStream(new StreamObserver<>() {
                    @Override
                    public void onNext(Discovery.Reply value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.warn("carrot discovery: heartbeat stream error: {}", t.getMessage());
                        markHeartbeatDisconnected();
                    }

                    @Override
                    public void onCompleted() {
                        logger.warn("carrot discovery: heartbeat stream completed");
                        markHeartbeatDisconnected();
                    }
                });
                logger.info("carrot discovery: heartbeat stream opened, admin={}:{}",
                        currentAdmin == null ? "" : currentAdmin.host(),
                        currentAdmin == null ? -1 : currentAdmin.port());
            } catch (Exception e) {
                logger.warn("carrot discovery: open heartbeat stream failed: {}", e.getMessage());
                markHeartbeatDisconnected();
            }
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
            discoveryClient.closeChannel();
        } catch (Exception ignored) {
        }
    }

    /**
     * 解析实例端口：
     * <p>
     * 优先使用配置端口；否则依次尝试 {@code server.port} 与 {@code spring.grpc.server.port}。
     * </p>
     *
     * @param configuredPort 配置的端口
     * @return 可用端口，无法解析时返回 -1
     */
    private int resolvePort(int configuredPort) {
        if (configuredPort > 0) {
            return configuredPort;
        }
        String serverPort = environment.getProperty("server.port");
        if (serverPort != null && !serverPort.isBlank()) {
            try {
                return Integer.parseInt(serverPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String grpcPort = environment.getProperty("spring.grpc.server.port");
        if (grpcPort != null && !grpcPort.isBlank()) {
            try {
                return Integer.parseInt(grpcPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}
