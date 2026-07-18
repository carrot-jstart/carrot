package org.jstart.carrot.discovery.demo.presentation;

import org.jstart.carrot.discovery.config.DiscoveryConfig;
import org.jstart.carrot.discovery.grpc.Discovery;
import org.jstart.carrot.discovery.support.CarrotDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/demo")
public class DiscoveryDemoController {
    private final CarrotDiscoveryClient discoveryClient;
    private final DiscoveryConfig discoveryConfig;

    public DiscoveryDemoController(CarrotDiscoveryClient discoveryClient, DiscoveryConfig discoveryConfig) {
        this.discoveryClient = Objects.requireNonNull(discoveryClient, "discoveryClient");
        this.discoveryConfig = Objects.requireNonNull(discoveryConfig, "discoveryConfig");
    }

    @GetMapping("/info")
    public InfoVO info() {
        CarrotDiscoveryClient.HostPort node = discoveryClient.getCurrentNode();
        return new InfoVO(
                discoveryConfig.getNamespace(),
                discoveryConfig.getGroup(),
                discoveryConfig.getService(),
                discoveryConfig.getIp(),
                discoveryConfig.getPort(),
                node == null ? null : node.host(),
                node == null ? null : node.port()
        );
    }

    @GetMapping("/instances")
    public List<InstanceVO> instances(@RequestParam String namespace,
                                                      @RequestParam String group,
                                                      @RequestParam String serviceName,
                                                      @RequestParam(defaultValue = "true") boolean onlyHealthy) {
        Discovery.ServiceKey key = Discovery.ServiceKey.newBuilder()
                .setNamespace(namespace)
                .setGroup(group)
                .setServiceName(serviceName)
                .build();
        Discovery.ListInstancesResponse response = discoveryClient.listInstances(key, onlyHealthy);
       return response.getInstancesList().stream()
                .map(it -> new InstanceVO(
                        it.getInstanceId(),
                        it.getIp(),
                        it.getPort(),
                        it.getWeight(),
                        it.getLastHeartbeatAt(),
                        it.getMetadataMap()
                ))
                .toList();

    }

    public record InfoVO(String namespace,
                         String group,
                         String serviceName,
                         String ip,
                         int port,
                         String currentAdminHost,
                         Integer currentAdminPort) {
    }

    public record InstanceVO(String instanceId,
                             String ip,
                             int port,
                             double weight,
                             long lastHeartbeatAt,
                             Map<String, String> metadata) {
    }
}

