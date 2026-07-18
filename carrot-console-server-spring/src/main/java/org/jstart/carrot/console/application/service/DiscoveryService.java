package org.jstart.carrot.console.application.service;

import org.jstart.carrot.console.application.dto.DiscoveryInstanceDTO;
import org.jstart.carrot.console.application.dto.SearchDiscoveryInstance;
import org.jstart.carrot.console.application.dto.SearchDiscoveryService;
import org.jstart.carrot.console.application.dto.ServiceKey;
import org.jstart.carrot.console.application.vo.DiscoveryInstanceVO;
import org.jstart.carrot.console.application.vo.DiscoveryServiceVO;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.domain.discovery.DiscoveryCommandDomainServer;
import org.jstart.carrot.console.domain.discovery.DiscoveryQueryDomainServer;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryInstanceModel;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryServiceModel;
import org.jstart.carrot.scheduling.constant.KeyValue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscoveryService {
    private final DiscoveryQueryDomainServer discoveryQueryDomainServer;
    private final DiscoveryCommandDomainServer discoveryCommandDomainServer;

    public DiscoveryService(DiscoveryQueryDomainServer discoveryQueryDomainServer,
                            DiscoveryCommandDomainServer discoveryCommandDomainServer) {
        this.discoveryQueryDomainServer = discoveryQueryDomainServer;
        this.discoveryCommandDomainServer = discoveryCommandDomainServer;
    }

    public String register(ServiceKey serviceKey, DiscoveryInstanceDTO instance) {
        long now = System.currentTimeMillis();

        String instanceId = normalizeInstanceId(instance.getInstanceId(), instance.getIp(), instance.getPort());
        String id = buildId(serviceKey, instanceId);

        DiscoveryInstanceModel created = new DiscoveryInstanceModel();
        created.setId(id);
        created.setNamespace(serviceKey.getNamespace());
        created.setGroupName(serviceKey.getGroup());
        created.setServiceName(serviceKey.getServiceName());
        created.setInstanceId(instanceId);
        created.setIp(instance.getIp());
        created.setPort(instance.getPort());
        created.setWeight(instance.getWeight());
        created.setMetadata(instance.getMetadata());
        created.setLastHeartbeatAt(now);
        created.setCreateTime(now);
        created.setUpdateTime(now);
        discoveryCommandDomainServer.saveModel(created);
        return instanceId;
    }

    private static String buildId(ServiceKey serviceKey, String instanceId) {
        return serviceKey.getNamespace() + ":" + serviceKey.getGroup() + ":" + serviceKey.getServiceName() + ":" + instanceId;
    }

    private static String normalizeInstanceId(String instanceId, String ip, int port) {
        if (instanceId != null && !instanceId.isBlank()) {
            return instanceId.trim();
        }
        String host = ip == null ? "" : ip.trim();
        if (host.isEmpty() || port <= 0) {
            return "";
        }
        return host + ":" + port;
    }


    public boolean heartbeat(ServiceKey service, String instanceId, String ip, int port) {
        long now = System.currentTimeMillis();
        String normalizedId = normalizeInstanceId(instanceId, ip, port);
        String id = buildId(service, normalizedId);

        DiscoveryInstanceModel model = discoveryQueryDomainServer.selectInstanceById(id);
        if (model == null) {
            return false;
        }
        model.setLastHeartbeatAt(now);
        model.setUpdateTime(now);
        discoveryCommandDomainServer.saveModel(model);
        return true;
    }

    public boolean deregister(ServiceKey serviceKey, String instanceId, String ip, int port) {

        String normalizedId = normalizeInstanceId(instanceId, ip, port);
        String id = buildId(serviceKey, normalizedId);
        return discoveryCommandDomainServer.deleteInstanceById(id);
    }

    public KeyValue<Long, List<DiscoveryInstanceVO>> listInstances(ServiceKey serviceKey) {


        SearchDiscoveryInstance searchDiscoveryInstance = new SearchDiscoveryInstance();
        searchDiscoveryInstance.setEqNamespace(serviceKey.getNamespace());
        searchDiscoveryInstance.setEqGroup(serviceKey.getGroup());
        searchDiscoveryInstance.setEqServiceName(serviceKey.getServiceName());
        List<DiscoveryInstanceModel> list = discoveryQueryDomainServer.selectInstance(searchDiscoveryInstance).getData();
        if (list == null) {
            list = List.of();
        }
        long version = 0;
        List<DiscoveryInstanceVO> instances = new ArrayList<>(list.size());
        for (DiscoveryInstanceModel m : list) {
            if (NumericUtil.isNotNullOrEmpty(m.getUpdateTime())) {
                version = Math.max(version, m.getUpdateTime());
            }
            instances.add(new DiscoveryInstanceVO()
                    .setInstanceId(m.getInstanceId() == null ? "" : m.getInstanceId())
                    .setIp(m.getIp() == null ? "" : m.getIp())
                    .setPort(m.getPort() == null ? 0 : m.getPort())
                    .setWeight(m.getWeight() == null ? 1.0 : m.getWeight())

                    .setLastHeartbeatAt(m.getLastHeartbeatAt() == null ? 0 : m.getLastHeartbeatAt())
                    .setMetadata(nullToEmptyMap(m.getMetadata()))
            );
        }
        return new KeyValue<>(version, instances);
    }


    public PageResult<List<DiscoveryServiceVO>> searchService(SearchDiscoveryService dto) {
        PageResult<List<DiscoveryServiceModel>> listPageResult = discoveryQueryDomainServer.searchService(dto);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPageResult.getTotal(), listPageResult.getData().stream().map(item-> new DiscoveryServiceVO()
                .setNamespace(item.getNamespace())
                .setGroup(item.getGroupName())
                .setServiceName(item.getServiceName())
                .setInstanceCount(item.getInstanceCount())
        ).collect(Collectors.toList()));
    }


    public PageResult<List<DiscoveryInstanceVO>> selectInstance(SearchDiscoveryInstance dto) {
        PageResult<List<DiscoveryInstanceModel>> listPageResult = discoveryQueryDomainServer.selectInstance(dto);
        List<DiscoveryInstanceModel> data = listPageResult.getData();
        if (data == null) {
            data = List.of();
        }
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPageResult.getTotal(), data.stream().map(item-> new DiscoveryInstanceVO()
                .setInstanceId(item.getInstanceId())
                .setIp(item.getIp())
                .setPort(item.getPort())
                .setWeight(item.getWeight())
                .setMetadata(nullToEmptyMap(item.getMetadata()))
                .setLastHeartbeatAt(item.getLastHeartbeatAt())
        ).collect(Collectors.toList()));
    }

    private static Map<String, String> nullToEmptyMap(Map<String, String> metadata) {
        return metadata == null ? Collections.emptyMap() : metadata;
    }
}
