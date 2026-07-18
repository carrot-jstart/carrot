package org.jstart.carrot.console.application.service;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.application.dto.*;
import org.jstart.carrot.console.application.vo.SchedulingExecutorNodeVO;
import org.jstart.carrot.console.application.vo.SchedulingJobRecordVO;
import org.jstart.carrot.console.application.vo.SchedulingJobUnitVO;
import org.jstart.carrot.console.comm.entity.KeyValue;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.EResultCode;
import org.jstart.carrot.console.domain.admin.AdminQueryDomainServer;
import org.jstart.carrot.console.domain.namespace.NamespaceQueryDomainServer;
import org.jstart.carrot.console.domain.scheduling.SchedulingCommandDomainServer;
import org.jstart.carrot.console.domain.scheduling.SchedulingQueryDomainServer;
import org.jstart.carrot.console.domain.scheduling.model.ExecutorNodeModel;
import org.jstart.carrot.console.domain.scheduling.model.JobRecordModel;
import org.jstart.carrot.console.domain.scheduling.model.JobUnitModel;
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    private final SchedulingCommandDomainServer schedulingCommandDomainServer;
    private final SchedulingQueryDomainServer schedulingQueryDomainServer;
    private final AdminQueryDomainServer adminQueryDomainServer;
    private final NamespaceQueryDomainServer namespaceQueryDomainServer;

    /**
     * 取模值
     */
    private static KeyValue<Integer,Integer> mod=new KeyValue<>(1,0);



    /**
     * 获取取模值
     * key：模值，value：自己的取模值
     * @return
     */
    public KeyValue<Integer,Integer> getModValue() {
        return mod;
    }

    /**
     * 处理客户端上传的心跳
     * @param dto
     * @return
     */
    public boolean heartbeat(ServiceKey serviceKey,SchedulingHearBeatDTO dto) {
        //注册执行器
        if(!namespaceQueryDomainServer.exists(serviceKey.getNamespace())){
            return false;
        }
        schedulingCommandDomainServer.saveExecutorNode(
                new ExecutorNodeModel()
                        .setId(serviceKey.getNamespace()+":"+serviceKey.getServiceName()+":"+serviceKey.getGroup()+":"+dto.getIp()+":"+dto.getPort())
                        .setNamespaceId(serviceKey.getNamespace())
                        .setExecutorName(serviceKey.getServiceName())
                        .setGroupName(serviceKey.getGroup())
                        .setIp(dto.getIp())
                        .setUpdateTime(System.currentTimeMillis())
                        .setPort(dto.getPort())
                        .setSecret(dto.getSecret())
                        .setWeight(dto.getWeight())
        );
        return true;
    }


    /**
     * 定时清理过时的执行器节点
     */
    @CarrotJobUnit(value = "clearExpiredExecutorNode",type = EJobUnitType.CORN,typeValue = "29 * * * * ? ")
    public Integer clearExpiredExecutorNode() {
        return schedulingCommandDomainServer.clearExpiredNode(System.currentTimeMillis() - 15 * 1000);
    }

    /**
     * 注销执行器节点
     * @param accessToken
     * @param ip
     * @param port
     * @return
     */
    public EResultCode unregisterExecutorNode(String accessToken, ServiceKey serviceKey, String ip, int port) {
        if(!adminQueryDomainServer.authorized(accessToken)||!namespaceQueryDomainServer.exists(serviceKey.getNamespace())){
            return EResultCode.FORBIDDEN;
        }
        return schedulingCommandDomainServer.deleteExecutorNodeId(serviceKey.getServiceName()+":"+serviceKey.getServiceName()+":"+serviceKey.getGroup()+":"+ip+":"+port)?
                EResultCode.SUCCESS:EResultCode.FAILED
                ;
    }

    /**
     * 注册任务单元
     * @param accessToken
     * @param list
     * @return
     */
    public EResultCode registerJobUnitList(String accessToken,ServiceKey serviceKey, List<SaveJobUnit> list) {
        if(!adminQueryDomainServer.authorized(accessToken)||!namespaceQueryDomainServer.exists(serviceKey.getNamespace())){
            return EResultCode.FORBIDDEN;
        }
        return schedulingCommandDomainServer.registerJobUnitList(list.stream()
                .map(unit -> new JobUnitModel()
                        .setId(serviceKey.getNamespace()+":"+serviceKey.getServiceName()+":"+serviceKey.getGroup()+":"+unit.getName())
                        .setNamespaceId(serviceKey.getNamespace())
                        .setExecutorName(serviceKey.getServiceName())
                        .setGroupName(serviceKey.getGroup())
                        .setName(unit.getName())
                        .setType(unit.getType().getValue())
                        .setTypeValue(unit.getTypeValue())
                        .setHashValue(Math.abs((serviceKey.getNamespace()+":"+serviceKey.getServiceName()+":"+serviceKey.getGroup()+":"+unit.getName()).hashCode()))
                )
                .toList()
        )?
                EResultCode.SUCCESS:EResultCode.FAILED
                ;
    }

    /**
     * 任务执行结果回执
     * @param accessToken
     * @param taskId
     * @param code
     * @param data
     * @param message
     * @param list
     */
    public void taskReceipt(String accessToken, String taskId, int code, String data, String message, List<String> list) {
        if(!adminQueryDomainServer.authorized(accessToken)){
           return;
        }
        schedulingCommandDomainServer.saveShift(new JobRecordModel()
                .setActualEndTime(System.currentTimeMillis())
                .setId(taskId)
                .setCode(code)
                .setMessage(message)
                .setResult(data)
                .setLog(list)
        );
    }

    public PageResult<List<SchedulingExecutorNodeVO>> searchPageExecutorNode(SearchSchedulingExecutorNode dto) {
        PageResult<List<ExecutorNodeModel>> listPageResult = schedulingQueryDomainServer.searchPageExecutorNode(dto);
        return new PageResult<>(listPageResult.getLimit(), listPageResult.getPage(), listPageResult.getTotal(),
                listPageResult.getData().stream().map(item-> new SchedulingExecutorNodeVO()
                                .setId(item.getId())
                                .setNamespaceId(item.getNamespaceId())
                                .setExecutorName(item.getExecutorName())
                                .setGroupName(item.getGroupName())
                                .setIp(item.getIp())
                                .setPort(item.getPort())
                                .setSecret(item.getSecret())
                                .setUpdateTime(item.getUpdateTime())
                        )
                        .toList()
        );
    }

    public List<String> getUnitExecutorNameByNamespace(String namespaceId) {
        return schedulingQueryDomainServer.getUnitExecutorNameByNamespace(namespaceId);
    }

    public List<String> getUnitGroupByNamespace(String namespaceId) {
        return schedulingQueryDomainServer.getUnitGroupByNamespace(namespaceId);
    }

    public PageResult<List<SchedulingJobUnitVO>> searchPageJobUnit(SearchSchedulingJobUnit dto) {
        PageResult<List<JobUnitModel>> data=schedulingQueryDomainServer.searchPageJobUnit(dto);
        return new PageResult<>(data.getLimit(),data.getPage(),data.getTotal(),
                data.getData().stream().map(item-> new SchedulingJobUnitVO()
                        .setId(item.getId())
                        .setExecutorName(item.getExecutorName())
                        .setGroupName(item.getGroupName())
                        .setName(item.getName())
                        .setNamespaceId(item.getNamespaceId())
                        .setLastPlanTime(item.getLastPlanTime())
                        .setEnable(item.getEnable())
                        .setHashValue(item.getHashValue())
                        .setType(item.getType())
                        .setTypeValue(item.getTypeValue())
                ).toList()
        );
    }

    public boolean enableJobUnit(String id) {
        return schedulingCommandDomainServer.enableJobUnit(id);
    }

    public boolean disableJobUnit(String id) {
        return schedulingCommandDomainServer.disableJobUnit(id);
    }

    public SchedulingJobUnitVO getUnit(String id) {
        JobUnitModel jobUnitModel = schedulingQueryDomainServer.getJobUnitById(id);
        return new SchedulingJobUnitVO()
                .setId(jobUnitModel.getId())
                .setExecutorName(jobUnitModel.getExecutorName())
                .setGroupName(jobUnitModel.getGroupName())
                .setName(jobUnitModel.getName())
                .setNamespaceId(jobUnitModel.getNamespaceId())
                .setLastPlanTime(jobUnitModel.getLastPlanTime())
                .setEnable(jobUnitModel.getEnable())
                .setHashValue(jobUnitModel.getHashValue())
                .setType(jobUnitModel.getType())
                .setTypeValue(jobUnitModel.getTypeValue());
    }

    public boolean deleteJobUnit(String id) {
        return schedulingCommandDomainServer.deleteJobUnit(id);
    }

    /**
     * 查询任务执行记录
     * @param dto
     * @return
     */
    public PageResult<List<SchedulingJobRecordVO>> searchJobRecord(SearchSchedulingJobRecord dto) {
        PageResult<List<JobRecordModel>> data=schedulingQueryDomainServer.searchJobRecord(dto);
        return new PageResult<>(data.getLimit(),data.getPage(),data.getTotal(),
                data.getData().stream().map(item-> new SchedulingJobRecordVO()
                                .setId(item.getId())
                                .setId(item.getId())
                                .setNamespaceId(item.getNamespaceId())
                                .setExecutorName(item.getExecutorName())
                                .setGroupName(item.getGroupName())
                                .setUnitName(item.getUnitName())
                                .setUnitId(item.getUnitId())
                                .setSecret(item.getSecret())
                                .setIp(item.getIp())
                                .setPort(item.getPort())
                                .setPlanStartTime(item.getPlanStartTime())
                                .setActualStartTime(item.getActualStartTime())
                                .setActualEndTime(item.getActualEndTime())
                                .setCode(item.getCode())
                                .setMessage(item.getMessage())
                                .setResult(item.getResult())
                                .setLog(item.getLog())
                                .setHashValue(item.getHashValue())
                        )
                        .toList()
        );
    }

    public SchedulingJobRecordVO getJobRecord(String id) {
        JobRecordModel item = schedulingQueryDomainServer.getJobRecordById(id);
        return new SchedulingJobRecordVO()
                .setId(item.getId())
                .setId(item.getId())
                .setNamespaceId(item.getNamespaceId())
                .setExecutorName(item.getExecutorName())
                .setGroupName(item.getGroupName())
                .setUnitName(item.getUnitName())
                .setUnitId(item.getUnitId())
                .setSecret(item.getSecret())
                .setIp(item.getIp())
                .setPort(item.getPort())
                .setPlanStartTime(item.getPlanStartTime())
                .setActualStartTime(item.getActualStartTime())
                .setActualEndTime(item.getActualEndTime())
                .setCode(item.getCode())
                .setMessage(item.getMessage())
                .setResult(item.getResult())
                .setLog(item.getLog())
                .setHashValue(item.getHashValue());
    }

    public boolean deleteJobRecord(List<String> ids) {
        return schedulingCommandDomainServer.deleteJobRecord(ids);
    }
}
