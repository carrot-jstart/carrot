package org.jstart.carrot.console.presentation.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.comm.entity.PointXyz;
import org.jstart.carrot.console.application.dto.SaveJobUnit;
import org.jstart.carrot.console.application.dto.SchedulingHearBeatDTO;
import org.jstart.carrot.console.application.dto.ServiceKey;
import org.jstart.carrot.console.application.service.AdminService;
import org.jstart.carrot.console.application.service.ResultCodeI18nService;
import org.jstart.carrot.console.application.service.SchedulingService;
import org.jstart.carrot.scheduling.SchedulingAdmin;
import org.jstart.carrot.scheduling.SchedulingAdminServiceGrpc;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.springframework.grpc.server.service.GrpcService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@RequiredArgsConstructor
public class SchedulingAdminGrpcService extends SchedulingAdminServiceGrpc.SchedulingAdminServiceImplBase {
        private final SchedulingService schedulingService;
        private final AdminService adminService;
        private final ResultCodeI18nService resultCodeI18nService;
        private final Map<StreamObserver<SchedulingAdmin.Reply>, SchedulingAdmin.HeartbeatRequest> heartbeatObserverMap = new ConcurrentHashMap<>();

        @Override
        public void getAdminNodes(Empty request, StreamObserver<SchedulingAdmin.AdminNodeList> responseObserver) {
                List<PointXyz<String, Integer, Double>> adminNodes = adminService.getNodeList();
                SchedulingAdmin.AdminNodeList adminNodeList = SchedulingAdmin.AdminNodeList.newBuilder()
                                .addAllAdminNode(
                                                adminNodes.stream()
                                                                .filter(node -> node.getValueX() != null
                                                                                && !node.getValueX().isBlank()
                                                                                && node.getValueY() != null
                                                                                && node.getValueY() > 0
                                                                                && node.getValueY() <= 65535)
                                                                .map(node -> SchedulingAdmin.AdminNode.newBuilder()
                                                                                .setIp(node.getValueX())
                                                                                .setPort(node.getValueY())
                                                                                .setWeight(node.getValueZ())
                                                                                .build())
                                                                .toList())
                                .build();
                responseObserver.onNext(adminNodeList);
                responseObserver.onCompleted();
        }

        /**
         * 强制子类必须实现此方法
         *
         * @return
         */
        @Override
        public StreamObserver<SchedulingAdmin.HeartbeatRequest> heartbeat(
                        StreamObserver<SchedulingAdmin.Reply> responseObserver) {
                return new StreamObserver<>() {
                        @Override
                        public void onNext(SchedulingAdmin.HeartbeatRequest request) {
                                heartbeatObserverMap.put(responseObserver, request);
                                if(!adminService.authorized(request.getAccessToken())){
                                        responseObserver.onNext(reply(EResultCode.UNAUTHORIZED, ""));
                                        return;
                                }
                                boolean ok = schedulingService.heartbeat(
                                                new ServiceKey()
                                                                .setNamespace(request.getServiceKey().getNamespace())
                                                                .setGroup(request.getServiceKey().getGroup())
                                                                .setServiceName(request.getServiceKey()
                                                                                .getServiceName()),
                                                new SchedulingHearBeatDTO()
                                                                .setIp(request.getIp())
                                                                .setPort(request.getPort())
                                                                .setSecret(request.getSecret())
                                                                .setWeight(request.getWeight()));
                                responseObserver.onNext(ok ? reply(EResultCode.SUCCESS, "") : reply(EResultCode.NOT_FOUND, ""));

                        }

                        @Override
                        public void onError(Throwable throwable) {
                                try {
                                        heartbeatObserverMap.remove(responseObserver);
                                        responseObserver.onCompleted();
                                } catch (Exception ignored) {
                                }
                        }

                        @Override
                        public void onCompleted() {
                                try {
                                        responseObserver.onCompleted();
                                }catch (Exception ignored){}
                                // 移除
                                SchedulingAdmin.HeartbeatRequest remove = heartbeatObserverMap.remove(responseObserver);
                                schedulingService.unregisterExecutorNode(
                                                remove.getAccessToken(),
                                                new ServiceKey()
                                                                .setNamespace(remove.getServiceKey().getNamespace())
                                                                .setGroup(remove.getServiceKey().getGroup())
                                                                .setServiceName(remove.getServiceKey()
                                                                                .getServiceName()),
                                                remove.getIp(),
                                                remove.getPort());
                        }
                };
        }

        @Override
        public void registerUnitList(SchedulingAdmin.RegisterUnitListRequest request,
                        StreamObserver<SchedulingAdmin.Reply> responseObserver) {
                List<SaveJobUnit> list = request.getUnitListList().stream()
                                .map(unit -> new SaveJobUnit()
                                                .setName(unit.getName())
                                                .setType(EJobUnitType.getByValue(unit.getType()))
                                                .setTypeValue(unit.getTypeValue()))
                                .toList();
                if (list.isEmpty()) {
                        SchedulingAdmin.Reply reply = SchedulingAdmin.Reply.newBuilder()
                                        .setCode(EResultCode.SUCCESS.getCode())
                                        .setMessage(resultCodeI18nService.getMessage(EResultCode.SUCCESS,
                                                        resultCodeI18nService.getDefaultLocale()))
                                        .setData("")
                                        .build();
                        responseObserver.onNext(reply);
                        responseObserver.onCompleted();
                }
                EResultCode eResultCode = schedulingService.registerJobUnitList(request.getAccessToken(),
                                new ServiceKey()
                                                .setNamespace(request.getServiceKey().getNamespace())
                                                .setGroup(request.getServiceKey().getGroup())
                                                .setServiceName(request.getServiceKey().getServiceName()),
                                list);
                SchedulingAdmin.Reply reply = SchedulingAdmin.Reply.newBuilder()
                                .setCode(eResultCode.getCode())
                                .setMessage(resultCodeI18nService.getMessage(eResultCode,
                                                resultCodeI18nService.getDefaultLocale()))
                                .setData("")
                                .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
        }

        @Override
        public void unregister(SchedulingAdmin.UnregisterRequest request,
                        StreamObserver<SchedulingAdmin.Reply> responseObserver) {
                EResultCode eResultCode = schedulingService.unregisterExecutorNode(request.getAccessToken(),
                                new ServiceKey()
                                                .setNamespace(request.getServiceKey().getNamespace())
                                                .setGroup(request.getServiceKey().getGroup())
                                                .setServiceName(request.getServiceKey().getServiceName()),
                                request.getIp(),
                                request.getPort());
                SchedulingAdmin.Reply reply = SchedulingAdmin.Reply.newBuilder()
                                .setCode(eResultCode.getCode())
                                .setMessage(resultCodeI18nService.getMessage(eResultCode,
                                                resultCodeI18nService.getDefaultLocale()))
                                .setData("")
                                .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
        }

        @Override
        public void taskReceipt(SchedulingAdmin.TaskReceiptRequest request, StreamObserver<Empty> responseObserver) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                schedulingService.taskReceipt(request.getAccessToken(),
                                request.getTaskId(),
                                request.getCode(),
                                request.getData(),
                                request.getMessage(),
                                request.getRecordsList().stream()
                                                .map(record -> new String(record.getBytes(StandardCharsets.UTF_8)))
                                                .toList());
        }

        private SchedulingAdmin.Reply reply(EResultCode code, String data) {
                return SchedulingAdmin.Reply.newBuilder()
                        .setCode(code.getCode())
                        .setMessage(resultCodeI18nService.getMessage(code, resultCodeI18nService.getDefaultLocale()))
                        .setData(data == null ? "" : data)
                        .build();
        }
}
