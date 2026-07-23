package org.jstart.carrot.discovery.support;

import com.google.protobuf.Empty;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import org.jstart.carrot.discovery.EResultCode;
import org.jstart.carrot.discovery.config.DiscoveryConfig;
import org.jstart.carrot.discovery.grpc.Discovery;
import org.jstart.carrot.discovery.grpc.DiscoveryServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Carrot Discovery gRPC 客户端。
 * <p>
 * 提供服务注册/注销、心跳流、实例查询与订阅等能力，并支持在多个服务端节点之间随机切换。
 * </p>
 */
public class CarrotDiscoveryClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CarrotDiscoveryClient.class);

    private final DiscoveryConfig discoveryConfig;
    private final Object channelLock = new Object();

    private ManagedChannel managedChannel;
    private DiscoveryServiceGrpc.DiscoveryServiceBlockingStub blockingStub;
    private DiscoveryServiceGrpc.DiscoveryServiceStub asyncStub;
    private volatile List<HostPort> knownServerNodes = List.of();
    private volatile HostPort currentNode;

    /**
     * @param discoveryConfig discovery 客户端配置
     */
    public CarrotDiscoveryClient(DiscoveryConfig discoveryConfig) {
        this.discoveryConfig = Objects.requireNonNull(discoveryConfig, "discoveryConfig");
        refreshServerNodes();
        switchToRandomServerNode();
    }

    /**
     * 注册服务实例。
     *
     * @param serviceKey 服务标识
     * @param instance   实例信息
     * @return 执行结果
     */
    public Discovery.Reply register(Discovery.ServiceKey serviceKey, Discovery.Instance instance) {
        ensureChannel();
        Discovery.RegisterRequest request = Discovery.RegisterRequest.newBuilder()
                .setAccessToken(nullToEmpty(discoveryConfig.getAccessToken()))
                .setService(serviceKey)
                .setInstance(instance)
                .build();
        try {
            return blockingStub.register(request);
        } catch (Exception e) {
            logger.warn("carrot discovery register failed: {}", e.getMessage());
            return Discovery.Reply.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(e.getMessage())
                    .setData("")
                    .build();
        }
    }

    /**
     * 打开心跳双向流。
     *
     * @param responseObserver 服务端响应观察者
     * @return 用于写入 HeartbeatRequest 的 StreamObserver
     */
    public StreamObserver<Discovery.HeartbeatRequest> openHeartbeatStream(
            StreamObserver<Discovery.Reply> responseObserver) {
        ensureChannel();
        return asyncStub.heartbeatStream(responseObserver);
    }

    /**
     * 注销服务实例。
     *
     * @param serviceKey 服务标识
     * @param instanceId 实例 ID
     * @param ip         实例 IP
     * @param port       实例端口
     * @return 执行结果
     */
    public Discovery.Reply deregister(Discovery.ServiceKey serviceKey, String instanceId, String ip, int port) {
        ensureChannel();
        Discovery.DeregisterRequest request = Discovery.DeregisterRequest.newBuilder()
                .setAccessToken(nullToEmpty(discoveryConfig.getAccessToken()))
                .setService(serviceKey)
                .setInstanceId(nullToEmpty(instanceId))
                .setIp(nullToEmpty(ip))
                .setPort(port)
                .build();
        try {
            return blockingStub.deregister(request);
        } catch (Exception e) {
            logger.warn("carrot discovery deregister failed: {}", e.getMessage());
            return Discovery.Reply.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(e.getMessage())
                    .setData("")
                    .build();
        }
    }

    /**
     * 查询服务实例列表。
     *
     * @param serviceKey  服务标识
     * @param onlyHealthy 是否只返回健康实例
     * @return 查询响应
     */
    public Discovery.ListInstancesResponse listInstances(Discovery.ServiceKey serviceKey, boolean onlyHealthy) {
        ensureChannel();
        Discovery.ListInstancesRequest request = Discovery.ListInstancesRequest.newBuilder()
                .setAccessToken(nullToEmpty(discoveryConfig.getAccessToken()))
                .setService(serviceKey)
                .build();
        try {
            return blockingStub.listInstances(request);
        } catch (Exception e) {
            logger.warn("carrot discovery listInstances failed: {}", e.getMessage());
            return Discovery.ListInstancesResponse.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(e.getMessage())
                    .setVersion(0)
                    .build();
        }
    }

    /**
     * 订阅服务实例变更。
     *
     * @param serviceKey  服务标识
     * @param onlyHealthy 是否只订阅健康实例
     * @param onChange    变更回调（收到事件时回调实例列表）
     * @return 可关闭的订阅句柄，调用 close 取消订阅
     */
    public AutoCloseable subscribe(Discovery.ServiceKey serviceKey, boolean onlyHealthy,
            Consumer<List<Discovery.Instance>> onChange) {
        ensureChannel();
        Objects.requireNonNull(serviceKey, "serviceKey");
        Objects.requireNonNull(onChange, "onChange");

        Discovery.SubscribeRequest request = Discovery.SubscribeRequest.newBuilder()
                .setAccessToken(nullToEmpty(discoveryConfig.getAccessToken()))
                .setService(serviceKey)
                .setSinceVersion(0)
                .setOnlyHealthy(onlyHealthy)
                .build();

        ClientCall<Discovery.SubscribeRequest, Discovery.ServiceEvent> call;
        synchronized (channelLock) {
            call = managedChannel.newCall(DiscoveryServiceGrpc.getSubscribeMethod(), CallOptions.DEFAULT);
        }
        ClientCall<Discovery.SubscribeRequest, Discovery.ServiceEvent> finalCall = call;
        ClientCalls.asyncServerStreamingCall(call, request, new StreamObserver<>() {
            @Override
            public void onNext(Discovery.ServiceEvent value) {
                if (value.getCode() == EResultCode.SUCCESS.getCode()) {
                    onChange.accept(value.getInstancesList());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("carrot discovery subscribe error: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
            }
        });

        return () -> finalCall.cancel("client cancel", null);
    }

    /**
     * 刷新服务端节点列表。
     * <p>
     * 以 server-addr 作为种子节点，向其中一个节点获取集群节点列表并合并去重。
     * </p>
     *
     * @return 最新的服务端节点列表（不可变）
     */
    public List<HostPort> refreshServerNodes() {
        List<HostPort> seeds = parseServerAddrSeeds(discoveryConfig.getServerAddr());
        if (seeds.isEmpty()) {
            throw new IllegalStateException("spring.cloud.carrot.discovery.server-addr is required");
        }
        List<HostPort> merged = new ArrayList<>(seeds);

        for (HostPort seed : seeds) {
            try {
                switchTo(seed);
                Discovery.ServerListResponse response = blockingStub.getServerList(Empty.getDefaultInstance());
                if (response.getCode() == EResultCode.SUCCESS.getCode() && response.getNodesCount() > 0) {
                    for (Discovery.ServerNode node : response.getNodesList()) {
                        if (node.getHost() == null || node.getHost().isBlank() || node.getPort() <= 0) {
                            continue;
                        }
                        merged.add(new HostPort(node.getHost(), node.getPort()));
                    }
                    break;
                }
            } catch (Exception e) {
                closeChannel();
            }
        }

        this.knownServerNodes = Collections.unmodifiableList(dedup(merged));
        return knownServerNodes;
    }

    /**
     * 随机切换到一个服务端节点。
     *
     * @return 选中的节点
     */
    public HostPort switchToRandomServerNode() {
        List<HostPort> nodes = knownServerNodes;
        if (nodes == null || nodes.isEmpty()) {
            nodes = refreshServerNodes();
        }
        HostPort selected = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
        switchTo(selected);
        return selected;
    }

    /**
     * 在排除某个节点的前提下，随机切换到另一个节点（用于失败重试场景）。
     *
     * @param excluded 需要排除的节点
     * @return 选中的节点
     */
    public HostPort switchToNextRandomServerNode(HostPort excluded) {
        List<HostPort> nodes = knownServerNodes;
        if (nodes == null || nodes.isEmpty()) {
            nodes = refreshServerNodes();
        }
        if (nodes.size() == 1) {
            HostPort only = nodes.get(0);
            switchTo(only);
            return only;
        }
        HostPort selected = excluded;
        for (int i = 0; i < Math.min(10, nodes.size() * 2); i++) {
            HostPort candidate = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            if (!candidate.equals(excluded)) {
                selected = candidate;
                break;
            }
        }
        switchTo(selected);
        return selected;
    }

    /**
     * 获取当前正在使用的服务端节点。
     *
     * @return 当前节点
     */
    public HostPort getCurrentNode() {
        return currentNode;
    }

    /**
     * Spring 容器销毁时关闭连接。
     */
    @Override
    public void destroy() {
        closeChannel();
    }

    /**
     * 关闭当前 gRPC Channel，并清空 Stub。
     */
    public void closeChannel() {
        synchronized (channelLock) {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                try {
                    managedChannel.shutdownNow();
                } catch (Exception ignored) {
                }
            }
            managedChannel = null;
            blockingStub = null;
            asyncStub = null;
        }
    }

    /**
     * 确保 gRPC Channel 已创建并可用。
     */
    private void ensureChannel() {
        synchronized (channelLock) {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                return;
            }
            HostPort node = currentNode;
            if (node == null) {
                refreshServerNodes();
                switchToRandomServerNode();
                return;
            }
            managedChannel = ManagedChannelBuilder.forAddress(node.host(), node.port())
                    .usePlaintext()
                    .build();
            blockingStub = DiscoveryServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = DiscoveryServiceGrpc.newStub(managedChannel);
        }
    }

    /**
     * 切换到指定节点并重建连接与 Stub。
     *
     * @param node 目标节点
     */
    private void switchTo(HostPort node) {
        synchronized (channelLock) {
            closeChannel();
            currentNode = node;
            managedChannel = ManagedChannelBuilder.forAddress(node.host(), node.port())
                    .usePlaintext()
                    .build();
            blockingStub = DiscoveryServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = DiscoveryServiceGrpc.newStub(managedChannel);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * host:port 形式的服务端节点描述。
     *
     * @param host 主机名或 IP
     * @param port 端口
     */
    public record HostPort(String host, int port) {
        /**
         * 解析 {@code host:port} 字符串为 HostPort。
         *
         * @param serverAddr {@code host:port} 字符串
         * @return 解析后的 HostPort
         */
        static HostPort parse(String serverAddr) {
            String value = serverAddr.trim();
            int idx = value.lastIndexOf(':');
            if (idx <= 0 || idx == value.length() - 1) {
                throw new IllegalArgumentException("Invalid server-addr: " + serverAddr);
            }
            String host = value.substring(0, idx).trim();
            int port;
            try {
                port = Integer.parseInt(value.substring(idx + 1).trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid server-addr: " + serverAddr);
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid server-addr: " + serverAddr);
            }
            return new HostPort(host, port);
        }
    }

    /**
     * 解析 server-addr 配置为节点列表。
     *
     * @param serverAddr 以逗号分隔的 {@code host:port} 列表
     * @return 节点列表
     */
    private static List<HostPort> parseServerAddrSeeds(String serverAddr) {
        if (serverAddr == null || serverAddr.isBlank()) {
            return List.of();
        }
        List<HostPort> list = new ArrayList<>();
        for (String item : serverAddr.split(",")) {
            String value = item.trim();
            if (value.isEmpty()) {
                continue;
            }
            list.add(HostPort.parse(value));
        }
        return list;
    }

    /**
     * 对节点列表去重并过滤空值。
     *
     * @param list 原始列表
     * @return 去重后的列表
     */
    private static List<HostPort> dedup(List<HostPort> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<HostPort> out = new ArrayList<>();
        for (HostPort hp : list) {
            if (hp == null) {
                continue;
            }
            boolean exists = false;
            for (HostPort x : out) {
                if (x.equals(hp)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                out.add(hp);
            }
        }
        return out;
    }
}
