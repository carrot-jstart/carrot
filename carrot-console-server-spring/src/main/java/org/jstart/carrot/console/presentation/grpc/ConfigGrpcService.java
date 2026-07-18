package org.jstart.carrot.console.presentation.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.comm.utils.StringUtil;
import org.jstart.carrot.config.grpc.Config;
import org.jstart.carrot.config.grpc.ConfigServiceGrpc;
import org.jstart.carrot.console.application.dto.ServiceKey;
import org.jstart.carrot.console.application.service.ConfigService;
import org.jstart.carrot.console.application.service.ResultCodeI18nService;
import org.jstart.carrot.console.application.vo.ConfigItemInfoVO;
import org.jstart.carrot.console.domain.admin.AdminQueryDomainServer;
import org.jstart.carrot.scheduling.constant.KeyValue;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class ConfigGrpcService extends ConfigServiceGrpc.ConfigServiceImplBase{
    private final AdminQueryDomainServer adminQueryDomainServer;
    private final ConfigService configService;
    private final ResultCodeI18nService resultCodeI18nService;

    @Override
    public void getServerList(Empty request, StreamObserver<Config.ServerListResponse> responseObserver) {
        responseObserver.onNext(Config.ServerListResponse.newBuilder()
                .setCode(EResultCode.SUCCESS.getCode())
                .setMessage(resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                .addAllNodes(adminQueryDomainServer.getFitAdminNode().stream()
                        .map(item-> Config.ServerNode.newBuilder()
                                .setHost(item.getIp())
                                .setPort(item.getPort())
                                .build())
                        .toList())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getConfig(Config.GetConfigRequest request, StreamObserver<Config.ConfigResponse> responseObserver) {
        if (!adminQueryDomainServer.authorized(request.getAccessToken())) {
            responseObserver.onNext(Config.ConfigResponse.newBuilder()
                    .setCode(EResultCode.UNAUTHORIZED.getCode())
                    .setMessage(resultCodeI18nService.getMessage(EResultCode.UNAUTHORIZED, resultCodeI18nService.getDefaultLocale()))
                    .setKey(request.getKey())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        ConfigItemInfoVO model = configService.find(new ServiceKey()
                .setNamespace(request.getKey().getNamespace())
                .setGroup(request.getKey().getGroup())
                .setServiceName(request.getKey().getDataId())
        );
        if (model == null) {
            responseObserver.onNext(Config.ConfigResponse.newBuilder()
                    .setCode(EResultCode.NOT_FOUND.getCode())
                    .setMessage(resultCodeI18nService.getMessage(EResultCode.NOT_FOUND, resultCodeI18nService.getDefaultLocale()))
                    .setKey(request.getKey())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(Config.ConfigResponse.newBuilder()
                .setCode(EResultCode.SUCCESS.getCode())
                .setMessage(resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                .setKey(Config.ConfigKey.newBuilder()
                        .setNamespace(model.getNamespace() == null ? "" : model.getNamespace())
                        .setGroup(model.getGroupName() == null ? "" : model.getGroupName())
                        .setDataId(model.getDataId() == null ? "" : model.getDataId())
                        .build())
                .setLastModified(model.getUpdateTime() == null ? 0L : model.getUpdateTime())
                .setContent(model.getContent() == null ? "" : model.getContent())
                .setMd5(model.getMd5() == null ? "" : model.getMd5())
                .setContentType(model.getContentType() == null ? "" : model.getContentType())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void publishConfig(Config.PublishConfigRequest request, StreamObserver<Config.Reply> responseObserver) {
        if (!adminQueryDomainServer.authorized(request.getAccessToken())) {
            responseObserver.onNext(reply(EResultCode.UNAUTHORIZED, ""));
            responseObserver.onCompleted();
            return;
        }
        try {
            configService.publish(new ServiceKey()
                    .setNamespace(request.getKey().getNamespace())
                    .setGroup(request.getKey().getGroup())
                    .setServiceName(request.getKey().getDataId())
                    , request.getContent(), request.getMd5(), request.getContentType());
            responseObserver.onNext(reply(EResultCode.SUCCESS, ""));
        } catch (IllegalArgumentException e) {
            responseObserver.onNext(reply(EResultCode.VALIDATE_FAILED, e.getMessage()));
        } catch (Exception e) {
            responseObserver.onNext(reply(EResultCode.ERROR, e.getMessage()));
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Config.WatchRequest> watch(StreamObserver<Config.WatchResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Config.WatchRequest request) {
                if (!adminQueryDomainServer.authorized(request.getAccessToken())) {
                    responseObserver.onNext(Config.WatchResponse.newBuilder()
                            .setCode(EResultCode.UNAUTHORIZED.getCode())
                            .setMessage(resultCodeI18nService.getMessage(EResultCode.UNAUTHORIZED, resultCodeI18nService.getDefaultLocale()))
                            .build());
                    responseObserver.onCompleted();
                    return;
                }
                List<KeyValue<ServiceKey, Long>> list = request.getConfigsList().stream()
                        .map(item -> new KeyValue<>(new ServiceKey()
                                .setNamespace(item.getKey().getNamespace())
                                .setGroup(item.getKey().getGroup())
                                .setServiceName(item.getKey().getDataId()), item.getLastModified()))
                        .toList();
                if(ListUtil.isNullOrEmpty(list)){
                    return;
                }
                List<KeyValue<ServiceKey,Long>> changed = configService.findChanged(list);
                if(ListUtil.isNotNullOrEmpty(changed)){
                    responseObserver.onNext(Config.WatchResponse.newBuilder()
                            .setCode(EResultCode.SUCCESS.getCode())
                            .setMessage(resultCodeI18nService.getMessage(EResultCode.SUCCESS, resultCodeI18nService.getDefaultLocale()))
                            .addAllChanged(changed.stream()
                                    .map(item-> Config.ConfigMeta.newBuilder()
                                            .setKey(Config.ConfigKey.newBuilder()
                                                    .setNamespace(StringUtil.isNullOrEmpty(item.getKey().getNamespace()) ? "" : item.getKey().getNamespace())
                                                    .setGroup( StringUtil.isNullOrEmpty(item.getKey().getGroup()) ? "" : item.getKey().getGroup())
                                                    .setDataId(StringUtil.isNullOrEmpty(item.getKey().getServiceName()) ? "" : item.getKey().getServiceName())
                                                    .build()
                                            )
                                            .setLastModified(NumericUtil.isNullOrEmpty(item.getValue()) ? 0L : item.getValue())
                                            .build()
                                    )
                                    .toList()
                            ).build());
                };
            }

            @Override
            public void onError(Throwable t) {
                try {
                    responseObserver.onCompleted();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private Config.Reply reply(EResultCode code, String data) {
        return Config.Reply.newBuilder()
                .setCode(code.getCode())
                .setMessage(resultCodeI18nService.getMessage(code, resultCodeI18nService.getDefaultLocale()))
                .setData(data == null ? "" : data)
                .build();
    }
}
