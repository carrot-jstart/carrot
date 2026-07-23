package org.jstart.carrot.scheduling.support;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.jstart.carrot.scheduling.SchedulingAdmin;
import org.jstart.carrot.scheduling.SchedulingAdminServiceGrpc;
import org.jstart.carrot.scheduling.SchedulingUnit;
import org.jstart.carrot.scheduling.config.SchedulingAdminConfig;
import org.jstart.carrot.scheduling.constant.EResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SchedulingClient implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingClient.class);

    private final SchedulingAdminConfig properties;
    private final Object channelLock = new Object();

    private ManagedChannel managedChannel;
    private SchedulingAdminServiceGrpc.SchedulingAdminServiceBlockingStub blockingStub;
    private SchedulingAdminServiceGrpc.SchedulingAdminServiceStub asyncStub;
    private volatile List<HostPort> knownServerNodes = List.of();
    private volatile HostPort currentNode;

    public SchedulingClient(SchedulingAdminConfig properties) {
        this.properties = properties;
    }
    @Override
    public void destroy() throws Exception {
        closeChannel();
    }

    /**
     * 打开心跳双向流
     */
    public StreamObserver<SchedulingAdmin.HeartbeatRequest> openHeartbeatStream(StreamObserver<SchedulingAdmin.Reply> responseObserver) {
        ensureChannel();
        return asyncStub.heartbeat(responseObserver);
    }

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
     * 刷新服务端节点列表。
     * <p>
     * 以 server-addr 作为种子节点，向其中一个节点获取集群节点列表并合并去重。
     * </p>
     *
     * @return 最新的服务端节点列表（不可变）
     */
    public List<HostPort> refreshServerNodes() {
        List<HostPort> seeds = parseServerAddrSeeds(properties.getServerAddr());
        if (seeds.isEmpty()) {
            throw new IllegalStateException("spring.cloud.carrot.scheduling.server-addr is required");
        }
        List<HostPort> merged = new ArrayList<>(seeds);

        for (HostPort seed : seeds) {
            try {
                switchTo(seed);
                SchedulingAdmin.AdminNodeList response = blockingStub.getAdminNodes(Empty.getDefaultInstance());
                    for (SchedulingAdmin.AdminNode node : response.getAdminNodeList()) {
                        if (node.getIp().isBlank() || node.getPort() <= 0) {
                            continue;
                        }
                        merged.add(new HostPort(node.getIp(), node.getPort()));
                    }
                    break;

            } catch (Exception e) {
                closeChannel();
            }
        }

        this.knownServerNodes = Collections.unmodifiableList(dedup(merged));
        return knownServerNodes;
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
                node = switchToRandomServerNode();
            }
            managedChannel = ManagedChannelBuilder.forAddress(node.host(), node.port())
                    .usePlaintext()
                    .build();
            blockingStub = SchedulingAdminServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = SchedulingAdminServiceGrpc.newStub(managedChannel);
        }
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
            blockingStub = SchedulingAdminServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = SchedulingAdminServiceGrpc.newStub(managedChannel);
        }
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

    public SchedulingAdmin.Reply register(SchedulingAdmin.ServiceKey serviceKey, List<SchedulingUnit> schedulingUnitList) {
        ensureChannel();
        SchedulingAdmin.RegisterUnitListRequest request = SchedulingAdmin.RegisterUnitListRequest.newBuilder()
                .setAccessToken(nullToEmpty(properties.getAccessToken()))
                .setServiceKey(serviceKey)
                .addAllUnitList(schedulingUnitList.stream()
                        .map(unit -> SchedulingAdmin.Unit.newBuilder()
                                .setName(unit.getName())
                                .setType(unit.getType().getValue())
                                .setTypeValue(unit.getTypeValue())
                                .build())
                        .toList()
                )
                .build();
        try {
            return blockingStub.registerUnitList(request);
        } catch (Exception e) {
            logger.warn("carrot scheduling register failed: {}", e.getMessage());
            return SchedulingAdmin.Reply.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(e.getMessage())
                    .setData("")
                    .build();
        }
    }

    public void sendTaskReturn(String taskId, Integer code, String message, String s, List<String> strings) {
        ensureChannel();
        SchedulingAdmin.TaskReceiptRequest request = SchedulingAdmin.TaskReceiptRequest.newBuilder()
                .setAccessToken(nullToEmpty(properties.getAccessToken()))
                .setTaskId(taskId)
                .setCode(code)
                .setMessage(message)
                .setData(null != s ? s : "")
                .addAllRecords(strings)
                .build();
        try {
            blockingStub.taskReceipt(request);
        }catch (Exception e){
            logger.warn("carrot scheduling sendTaskReturn failed: {}", e.getMessage());
        }
    }

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

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

}
