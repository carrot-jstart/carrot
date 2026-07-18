package com.example.job;

import jakarta.annotation.PostConstruct;
import org.jstart.carrot.config.comm.Snapshot;
import org.jstart.carrot.config.support.CarrotConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * nacos监听器
 *
 * @author wanghongjie
 */
@Component
public class RouteConfigListener {
    private static final Logger log = LoggerFactory.getLogger(RouteConfigListener.class);

    @Autowired
    RouteOperator routeOperator;
    @Autowired
    CarrotConfigService carrotConfigService;


    @PostConstruct
    public void dynamicRouteByNacosListener() {
        new Thread(
                ()->{
                    Snapshot snapshot = carrotConfigService.get(
                            "public",
                            "gateway",
                            "route-config.json"
                    );
                    if (snapshot == null) {
                        log.warn("skip initial route refresh because route config snapshot is null");
                        return;
                    }
                    // 立即更新
                    routeOperator.refreshAll(snapshot.content());
                    // 添加监听，nacos上的配置变更后会执行

                       carrotConfigService.subscribe(
                               "public",
                               "gateway",
                               "route-config.json",
                    (key, oldSnap, newSnap) -> {
                        routeOperator.refreshAll(newSnap.content());
                    }
                    );
                }
        ).start();
    }
}
