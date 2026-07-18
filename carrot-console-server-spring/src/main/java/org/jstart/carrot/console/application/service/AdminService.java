package org.jstart.carrot.console.application.service;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.PointXyz;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.domain.admin.AdminQueryDomainServer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminQueryDomainServer adminQueryDomainServer;

    /**
     * 获取管理节点列表
     */
    public List<PointXyz<String,Integer,Double>> getNodeList() {
        return adminQueryDomainServer.getFitAdminNode()
                .stream()
                .map(item-> new PointXyz<>(
                        item.getIp() == null ? "" : item.getIp(),
                        NumericUtil.tryParseInt(item.getPort(), 0),
                        NumericUtil.tryParseDouble(item.getWeight(), 0.0D)))
                .toList();
    }

    public  boolean authorized(String accessToken){
        return adminQueryDomainServer.authorized(accessToken);
    }
}
