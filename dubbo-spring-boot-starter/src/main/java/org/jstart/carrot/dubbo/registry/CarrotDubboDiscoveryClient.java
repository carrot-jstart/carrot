package org.jstart.carrot.dubbo.registry;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NegotiationType;
import org.apache.dubbo.common.URL;
import org.jstart.carrot.discovery.grpc.Discovery;
import org.jstart.carrot.discovery.grpc.DiscoveryServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dubbo Carrot registry 所使用的 Discovery gRPC 客户端。
 */
final class CarrotDubboDiscoveryClient {
    private static final Logger logger = LoggerFactory.getLogger(CarrotDubboDiscoveryClient.class);

    private final URL registryUrl;
    private final String accessToken;
    private final Object channelLock = new Object();
    private volatile NegotiationType negotiationType = NegotiationType.PLAINTEXT;

    private volatile List<HostPort> knownServerNodes = List.of();
    private volatile HostPort currentNode;
    private ManagedChannel managedChannel;
    private DiscoveryServiceGrpc.DiscoveryServiceBlockingStub blockingStub;
    private DiscoveryServiceGrpc.DiscoveryServiceStub asyncStub;

    CarrotDubboDiscoveryClient(URL registryUrl) {
        this.registryUrl = Objects.requireNonNull(registryUrl, "registryUrl");
        this.accessToken = registryUrl.getParameter("accessToken", "");
        this.knownServerNodes = parseSeedNodes(registryUrl);
        if (this.knownServerNodes.isEmpty()) {
            throw new IllegalArgumentException("Invalid carrot registry address: " + registryUrl);
        }
    }

    Discovery.Reply register(Discovery.ServiceKey serviceKey, Discovery.Instance instance) {
        ensureChannel();
        Discovery.RegisterRequest request = Discovery.RegisterRequest.newBuilder()
                .setAccessToken(accessToken)
                .setService(serviceKey)
                .setInstance(instance)
                .build();
        try {
            return blockingStub.register(request);
        } catch (Exception e) {
            Exception ex = e;
            if (maybeSwitchToPlaintextUpgrade(ex)) {
                try {
                    ensureChannel();
                    return blockingStub.register(request);
                } catch (Exception e2) {
                    ex = e2;
                }
            }
            logger.warn("carrot dubbo register failed: {}", ex.getMessage());
            closeChannel();
            return Discovery.Reply.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(ex.getMessage() == null ? "register failed" : ex.getMessage())
                    .build();
        }
    }

    Discovery.Reply deregister(Discovery.ServiceKey serviceKey, String instanceId, String ip, int port) {
        ensureChannel();
        Discovery.DeregisterRequest request = Discovery.DeregisterRequest.newBuilder()
                .setAccessToken(accessToken)
                .setService(serviceKey)
                .setInstanceId(instanceId == null ? "" : instanceId)
                .setIp(ip == null ? "" : ip)
                .setPort(port)
                .build();
        try {
            return blockingStub.deregister(request);
        } catch (Exception e) {
            Exception ex = e;
            if (maybeSwitchToPlaintextUpgrade(ex)) {
                try {
                    ensureChannel();
                    return blockingStub.deregister(request);
                } catch (Exception e2) {
                    ex = e2;
                }
            }
            logger.warn("carrot dubbo deregister failed: {}", ex.getMessage());
            closeChannel();
            return Discovery.Reply.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(ex.getMessage() == null ? "deregister failed" : ex.getMessage())
                    .build();
        }
    }

    Discovery.ListInstancesResponse listInstances(Discovery.ServiceKey serviceKey) {
        ensureChannel();
        Discovery.ListInstancesRequest request = Discovery.ListInstancesRequest.newBuilder()
                .setAccessToken(accessToken)
                .setService(serviceKey)
                .build();
        try {
            return blockingStub.listInstances(request);
        } catch (Exception e) {
            e.printStackTrace();
            Exception ex = e;
            if (maybeSwitchToPlaintextUpgrade(ex)) {
                try {
                    ensureChannel();
                    return blockingStub.listInstances(request);
                } catch (Exception e2) {
                    ex = e2;
                }
            }
            logger.warn("carrot dubbo listInstances failed: {}", ex.getMessage());
            closeChannel();
            return Discovery.ListInstancesResponse.newBuilder()
                    .setCode(EResultCode.FAILED.getCode())
                    .setMessage(ex.getMessage() == null ? "list instances failed" : ex.getMessage())
                    .build();
        }
    }

    StreamObserver<Discovery.HeartbeatRequest> openHeartbeatStream(StreamObserver<Discovery.Reply> responseObserver) {
        ensureChannel();
        return asyncStub.heartbeatStream(responseObserver);
    }

    List<HostPort> refreshServerNodes() {
        List<HostPort> seeds = parseSeedNodes(registryUrl);
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

    HostPort switchToRandomServerNode() {
        List<HostPort> nodes = knownServerNodes == null || knownServerNodes.isEmpty()
                ? parseSeedNodes(registryUrl)
                : knownServerNodes;
        HostPort selected = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
        switchTo(selected);
        return selected;
    }

    HostPort switchToNextRandomServerNode(HostPort excluded) {
        List<HostPort> nodes = knownServerNodes == null || knownServerNodes.isEmpty()
                ? parseSeedNodes(registryUrl)
                : knownServerNodes;
        if (nodes.size() == 1) {
            HostPort only = nodes.get(0);
            switchTo(only);
            return only;
        }
        HostPort selected = excluded;
        for (int i = 0; i < Math.min(nodes.size() * 2, 10); i++) {
            HostPort candidate = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            if (!candidate.equals(excluded)) {
                selected = candidate;
                break;
            }
        }
        switchTo(selected);
        return selected;
    }

    HostPort getCurrentNode() {
        return currentNode;
    }

    void closeChannel() {
        synchronized (channelLock) {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                try {
                    managedChannel.shutdownNow();
                } catch (Exception e) {
                    logger.warn("carrot dubbo close channel failed: {}", e.getMessage());
                }
            }
            managedChannel = null;
            blockingStub = null;
            asyncStub = null;
        }
    }

    void destroy() {
        closeChannel();
    }

    private void ensureChannel() {
        synchronized (channelLock) {
            if (managedChannel != null && !managedChannel.isShutdown()) {
                return;
            }
            HostPort node = currentNode;
            if (node == null) {
                switchToRandomServerNode();
                return;
            }
            managedChannel = newChannel(node, negotiationType);
            blockingStub = DiscoveryServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = DiscoveryServiceGrpc.newStub(managedChannel);
        }
    }

    private void switchTo(HostPort node) {
        synchronized (channelLock) {
            closeChannel();
            currentNode = node;
            managedChannel = newChannel(node, negotiationType);
            blockingStub = DiscoveryServiceGrpc.newBlockingStub(managedChannel);
            asyncStub = DiscoveryServiceGrpc.newStub(managedChannel);
        }
    }

    private static ManagedChannel newChannel(HostPort node, NegotiationType negotiationType) {
        return NettyChannelBuilder.forAddress(node.host(), node.port())
                .negotiationType(negotiationType)
                .build();
    }

    private boolean maybeSwitchToPlaintextUpgrade(Exception e) {
        if (negotiationType != NegotiationType.PLAINTEXT) {
            return false;
        }
        String message = e == null ? null : e.getMessage();
        if (message == null || !message.contains("unexpected EOS")) {
            return false;
        }
        negotiationType = NegotiationType.PLAINTEXT_UPGRADE;
        closeChannel();
        return true;
    }

    private static List<HostPort> parseSeedNodes(URL url) {
        List<HostPort> nodes = new ArrayList<>();
        if (url.getHost() != null && !url.getHost().isBlank() && url.getPort() > 0) {
            nodes.add(new HostPort(url.getHost(), url.getPort()));
        }
        String backup = url.getParameter("backup", "");
        if (!backup.isBlank()) {
            for (String item : backup.split(",")) {
                if (item == null || item.isBlank()) {
                    continue;
                }
                nodes.add(HostPort.parse(item.trim()));
            }
        }
        return dedup(nodes);
    }

    private static List<HostPort> dedup(List<HostPort> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        List<HostPort> out = new ArrayList<>();
        for (HostPort node : nodes) {
            if (node == null) {
                continue;
            }
            boolean exists = false;
            for (HostPort item : out) {
                if (item.equals(node)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                out.add(node);
            }
        }
        return out;
    }

    record HostPort(String host, int port) {
        static HostPort parse(String address) {
            int index = address.lastIndexOf(':');
            if (index <= 0 || index >= address.length() - 1) {
                throw new IllegalArgumentException("Invalid host:port value: " + address);
            }
            String host = address.substring(0, index).trim();
            int port = Integer.parseInt(address.substring(index + 1).trim());
            if (host.isBlank() || port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid host:port value: " + address);
            }
            return new HostPort(host, port);
        }
    }
}
