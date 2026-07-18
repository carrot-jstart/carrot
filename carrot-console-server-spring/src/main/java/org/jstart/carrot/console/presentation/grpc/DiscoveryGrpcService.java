package org.jstart.carrot.console.presentation.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.application.dto.DiscoveryInstanceDTO;
import org.jstart.carrot.console.application.dto.ServiceKey;
import org.jstart.carrot.console.application.service.AdminService;
import org.jstart.carrot.console.application.service.DiscoveryService;
import org.jstart.carrot.console.application.service.ResultCodeI18nService;
import org.jstart.carrot.console.application.vo.DiscoveryInstanceVO;
import org.jstart.carrot.console.domain.admin.AdminQueryDomainServer;
import org.jstart.carrot.discovery.grpc.Discovery;
import org.jstart.carrot.discovery.grpc.DiscoveryServiceGrpc;
import org.jstart.carrot.scheduling.constant.KeyValue;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
@RequiredArgsConstructor
public class DiscoveryGrpcService extends DiscoveryServiceGrpc.DiscoveryServiceImplBase {
    private final DiscoveryService discoveryService;
    private final AdminService adminService;
    private final AdminQueryDomainServer adminQueryDomainServer;
    private final ResultCodeI18nService resultCodeI18nService;

    private final Map<StreamObserver<Discovery.Reply>, Discovery.HeartbeatRequest> observerMap = new ConcurrentHashMap<>();

    @Override
    public void register(Discovery.RegisterRequest request, StreamObserver<Discovery.Reply> responseObserver) {
        if (!adminQueryDomainServer.authorized(request.getAccessToken())) {
            responseObserver.onNext(reply(EResultCode.UNAUTHORIZED, ""));
            responseObserver.onCompleted();
            return;
        }
        String instanceId = discoveryService.register(new ServiceKey()
                .setNamespace(request.getService().getNamespace())
                .setServiceName(request.getService().getServiceName())
                .setGroup(request.getService().getGroup()),
                new DiscoveryInstanceDTO()
                        .setInstanceId(request.getInstance().getInstanceId())
                        .setIp(request.getInstance().getIp())
                        .setPort(request.getInstance().getPort())
                        .setWeight(request.getInstance().getWeight())
                        .setMetadata(request.getInstance().getMetadataMap()));
        responseObserver.onNext(reply(EResultCode.SUCCESS, instanceId));
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Discovery.HeartbeatRequest> heartbeatStream(
            StreamObserver<Discovery.Reply> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Discovery.HeartbeatRequest request) {
                observerMap.put(responseObserver, request);
                if (!adminService.authorized(request.getAccessToken())) {
                    responseObserver.onNext(reply(EResultCode.UNAUTHORIZED, ""));
                    return;
                }
                boolean ok = discoveryService.heartbeat(
                        new ServiceKey()
                                .setNamespace(request.getService().getNamespace())
                                .setGroup(request.getService().getGroup())
                                .setServiceName(request.getService().getServiceName()),
                        request.getInstanceId(),
                        request.getIp(),
                        request.getPort());
                responseObserver.onNext(ok ? reply(EResultCode.SUCCESS, "") : reply(EResultCode.NOT_FOUND, ""));
            }

            @Override
            public void onError(Throwable t) {
                try {
                    observerMap.remove(responseObserver);
                    responseObserver.onCompleted();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                // 触发注销
                Discovery.HeartbeatRequest request = observerMap.remove(responseObserver);
                discoveryService.deregister(new ServiceKey()
                        .setNamespace(request.getService().getNamespace())
                        .setGroup(request.getService().getGroup())
                        .setServiceName(request.getService().getServiceName()),
                        request.getInstanceId(),
                        request.getIp(),
                        request.getPort());
            }
        };
    }

    @Override
    public void deregister(Discovery.DeregisterRequest request, StreamObserver<Discovery.Reply> responseObserver) {
        if (!adminService.authorized(request.getAccessToken())) {
            responseObserver.onNext(reply(EResultCode.UNAUTHORIZED, ""));
            responseObserver.onCompleted();
            return;
        }
        boolean ok = discoveryService.deregister(
                new ServiceKey()
                        .setNamespace(request.getService().getNamespace())
                        .setGroup(request.getService().getGroup())
                        .setServiceName(request.getService().getServiceName()),
                request.getInstanceId(),
                request.getIp(),
                request.getPort());
        responseObserver.onNext(ok ? reply(EResultCode.SUCCESS, "") : reply(EResultCode.NOT_FOUND, ""));
        responseObserver.onCompleted();
    }

    @Override
    public void getServerList(Empty request, StreamObserver<Discovery.ServerListResponse> responseObserver) {
        List<Discovery.ServerNode> nodes = adminService.getNodeList().stream()
                .map(node -> Discovery.ServerNode.newBuilder()
                        .setHost(node.getValueX())
                        .setPort(node.getValueY())
                        .build())
                .toList();
        responseObserver.onNext(Discovery.ServerListResponse.newBuilder()
                .setCode(EResultCode.SUCCESS.getCode())
                .setMessage(
                        resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                .addAllNodes(nodes)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listInstances(Discovery.ListInstancesRequest request,
            StreamObserver<Discovery.ListInstancesResponse> responseObserver) {

        if (!adminService.authorized(request.getAccessToken())) {
            responseObserver.onNext(Discovery.ListInstancesResponse.newBuilder()
                    .setCode(EResultCode.UNAUTHORIZED.getCode())
                    .setMessage(resultCodeI18nService.getMessage(EResultCode.UNAUTHORIZED,
                            resultCodeI18nService.getDefaultLocale()))
                    .setVersion(0)
                    .build());
            responseObserver.onCompleted();
            return;
        }
        KeyValue<Long, List<DiscoveryInstanceVO>> snapshot = discoveryService.listInstances(new ServiceKey()
                .setNamespace(request.getService().getNamespace())
                .setGroup(request.getService().getGroup())
                .setServiceName(request.getService().getServiceName()));
        responseObserver.onNext(Discovery.ListInstancesResponse.newBuilder()
                .setCode(EResultCode.SUCCESS.getCode())
                .setMessage(
                        resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                .setVersion(snapshot.getKey())
                .addAllInstances(snapshot.getValue().stream()
                        .map(instance -> {
                            Discovery.Instance.Builder builder = Discovery.Instance.newBuilder()
                                .setInstanceId(instance.getInstanceId())
                                .setIp(instance.getIp())
                                .setPort(instance.getPort())
                                .setWeight(instance.getWeight())
                                .setLastHeartbeatAt(instance.getLastHeartbeatAt());
                            if (instance.getMetadata() != null) {
                                builder.putAllMetadata(instance.getMetadata());
                            }
                            return builder.build();
                        })
                        .toList())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void subscribe(Discovery.SubscribeRequest request, StreamObserver<Discovery.ServiceEvent> responseObserver) {
        if (!adminService.authorized(request.getAccessToken())) {
            responseObserver.onNext(Discovery.ServiceEvent.newBuilder()
                    .setCode(EResultCode.UNAUTHORIZED.getCode())
                    .setMessage(resultCodeI18nService.getMessage(EResultCode.UNAUTHORIZED,
                            resultCodeI18nService.getDefaultLocale()))
                    .setService(request.getService())
                    .setVersion(0)
                    .build());
            responseObserver.onCompleted();
            return;
        }
        AtomicLong lastVersion = new AtomicLong(-1);
        KeyValue<Long, List<DiscoveryInstanceVO>> snapshot = discoveryService.listInstances(new ServiceKey()
                .setNamespace(request.getService().getNamespace())
                .setGroup(request.getService().getGroup())
                .setServiceName(request.getService().getServiceName()));
        if (lastVersion.get() == snapshot.getKey()) {
            return;
        }
        lastVersion.set(snapshot.getKey());
        responseObserver.onNext(Discovery.ServiceEvent.newBuilder()
                .setCode(EResultCode.SUCCESS.getCode())
                .setMessage(
                        resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                .setService(request.getService())
                .setVersion(snapshot.getKey())
                .addAllInstances(snapshot.getValue().stream()
                        .map(instance -> {
                            Discovery.Instance.Builder builder = Discovery.Instance.newBuilder()
                                .setInstanceId(instance.getInstanceId())
                                .setIp(instance.getIp())
                                .setPort(instance.getPort())
                                .setWeight(instance.getWeight())
                                .setLastHeartbeatAt(instance.getLastHeartbeatAt());
                            if (instance.getMetadata() != null) {
                                builder.putAllMetadata(instance.getMetadata());
                            }
                            return builder.build();
                        })
                        .toList())
                .build());
    }

    private Discovery.Reply reply(EResultCode code, String data) {
        return Discovery.Reply.newBuilder()
                .setCode(code.getCode())
                .setMessage(resultCodeI18nService.getMessage(code, resultCodeI18nService.getDefaultLocale()))
                .setData(data == null ? "" : data)
                .build();
    }
}
