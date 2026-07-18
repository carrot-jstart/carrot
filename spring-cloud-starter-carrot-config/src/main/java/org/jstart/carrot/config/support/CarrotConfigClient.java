package org.jstart.carrot.config.support;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.jstart.carrot.config.EResultCode;
import org.jstart.carrot.config.comm.ServerKey;
import org.jstart.carrot.config.config.CarrotConfigProperties;
import org.jstart.carrot.config.grpc.Config;
import org.jstart.carrot.config.grpc.ConfigServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Carrot Config gRPC 客户端。
 * <p>
 * 负责与 Config Server 建立 gRPC 连接，并提供读取/发布/监听配置等能力。
 * 内部维护服务器节点列表，并支持随机切换节点以实现简单的容错。
 * </p>
 */
public class CarrotConfigClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(CarrotConfigClient.class);

    private final CarrotConfigProperties properties;
    private final Object channelLock = new Object();

    private ManagedChannel managedChannel;
    private ConfigServiceGrpc.ConfigServiceBlockingStub blockingStub;
    private ConfigServiceGrpc.ConfigServiceStub asyncStub;
    private volatile List<HostPort> knownServerNodes = List.of();
    private volatile HostPort currentNode;

    /**
     * 创建客户端并初始化可用的服务端节点列表。
     *
     * @param properties 配置属性
     */
    public CarrotConfigClient(CarrotConfigProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * 获取指定 Key 的配置内容。
     *
     * @param key 配置 Key（namespace/group/dataId）
     * @return 配置响应（失败时返回带错误码与消息的响应对象）
     */
    public Config.ConfigResponse getConfig(ServerKey key) {
        ensureChannel();
        Config.GetConfigRequest request = Config.GetConfigRequest.newBuilder()
                .setAccessToken(nullToEmpty(properties.getAccessToken()))
                .setKey(Config.ConfigKey.newBuilder()
                        .setNamespace(key.namespace())
                        .setGroup(key.group())
                        .setDataId(key.dataId())
                        .build()
                )
                .build();
        try {
            return blockingStub.getConfig(request);
        } catch (Exception e) {
            logger.warn("carrot config getConfig failed: {}", e.getMessage());
            return Config.ConfigResponse.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(e.getMessage())
                    .setKey(request.getKey())
                    .build();
        }
    }

    /**
     * 发布配置内容到配置中心。
     *
     * @param key         配置 Key（namespace/group/dataId）
     * @param content     配置文本
     * @param contentType 内容类型（例如 text/yaml、text/plain 等）
     * @return 发布结果
     */
    public EResultCode publishConfig(ServerKey key, String content, String contentType) {
        ensureChannel();
        Config.PublishConfigRequest request = Config.PublishConfigRequest.newBuilder()
                .setAccessToken(nullToEmpty(properties.getAccessToken()))
                .setKey(Config.ConfigKey.newBuilder()
                        .setNamespace(key.namespace())
                        .setGroup(key.group())
                        .setDataId(key.dataId())
                        .build()
                )
                .setContent(content == null ? "" : content)
                .setContentType(contentType == null ? "" : contentType)
                .build();
        try {
            Config.Reply reply = blockingStub.publishConfig(request);
            return EResultCode.getByCode(reply.getCode());
        } catch (Exception e) {
            logger.warn("carrot config publishConfig failed: {}", e.getMessage());
            return EResultCode.FAILED;
        }
    }

    /**
     * 打开配置监听的双向流（请求流由调用方写入，响应流由服务端推送）。
     *
     * @param responseObserver 响应观察者
     * @return 用于写入 WatchRequest 的 StreamObserver
     */
    public StreamObserver<Config.WatchRequest> openWatchStream(StreamObserver<Config.WatchResponse> responseObserver) {
        ensureChannel();
        return asyncStub.watch(responseObserver);
    }

    /**
     * 刷新服务端节点列表。
     * <p>
     * 以 {@code spring.cloud.carrot.config.server-addr} 作为种子节点，尝试从服务端获取集群节点列表并合并去重。
     * </p>
     *
     * @return 最新的服务端节点列表（不可变）
     */
    public List<HostPort> refreshServerNodes() {
        List<HostPort> seeds = parseServerAddrSeeds(properties.getServerAddr());
        if (seeds.isEmpty()) {
            throw new IllegalStateException("spring.cloud.carrot.config.server-addr is required");
        }
        List<HostPort> merged = new ArrayList<>(seeds);

        for (HostPort seed : seeds) {
            try {
                switchTo(seed);
                Config.ServerListResponse response = blockingStub.getServerList(Empty.getDefaultInstance());
                if (response.getCode() == EResultCode.SUCCESS.getCode() && response.getNodesCount() > 0) {
                    for (Config.ServerNode node : response.getNodesList()) {
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
     * <p>
     * 当未选择节点时会触发节点刷新并随机选择一个节点。
     * </p>
     */
    private void ensureChannel() {
        synchronized (channelLock) {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                return;
            }
            HostPort node = currentNode;
            if (node == null) {
                refreshServerNodes();
                if (managedChannel != null && !managedChannel.isShutdown()) {
                    return;
                }
                node = currentNode;
            }
            if (node == null) {
                node = switchToRandomServerNode();
            }
            managedChannel = ManagedChannelBuilder.forAddress(node.host(), node.port())
                    .usePlaintext()
                    .build();
            blockingStub = ConfigServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = ConfigServiceGrpc.newStub(managedChannel);
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
            blockingStub = ConfigServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = ConfigServiceGrpc.newStub(managedChannel);
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
