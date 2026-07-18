package org.jstart.carrot.console.domain.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jstart.carrot.console.application.event.AdminModValue;
import org.jstart.carrot.console.domain.admin.model.AdminNodeModel;
import org.jstart.carrot.console.domain.scheduling.SchedulingDomainService;
import org.jstart.carrot.console.infrastructure.config.AdminConfig;
import org.jstart.carrot.console.infrastructure.repository.AdminNodeDao;
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.lang.Thread.sleep;

@Service
public class AdminDomainService implements AdminQueryDomainServer, CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(SchedulingDomainService.class);
    private final ApplicationContext applicationContext;
    private final AdminNodeDao adminNodeDao;
    private Double weight=0.0;
    private final AdminConfig adminConfig;
    private final Integer grpcPort;
    public AdminDomainService(AdminNodeDao adminNodeDao, AdminConfig adminConfig, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.adminNodeDao = adminNodeDao;
        this.adminConfig = adminConfig;
        Thread schedulerThread = new Thread(new Server(), "admin-server");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        grpcPort= Integer.valueOf(Objects.requireNonNull(applicationContext.getEnvironment().getProperty("spring.grpc.server.port")));
    }

    @Override
    public List<AdminNodeModel> getFitAdminNode() {
        return adminNodeDao.selectList(new LambdaQueryWrapper<AdminNodeModel>()
                .select(AdminNodeModel::getIp, AdminNodeModel::getPort, AdminNodeModel::getWeight)
                .orderByDesc(AdminNodeModel::getWeight)
        );
    }

    @Override
    public boolean authorized(String accessToken) {
        return adminConfig.getAccessToken().equals(accessToken);
    }

    @Override
    public void run(String... args) throws Exception {
        clearExecutorAdmin();
        adminNodeDao.insertOrUpdate(new AdminNodeModel()
                .setId(adminConfig.getIp()+":"+grpcPort)
                .setIp(adminConfig.getIp())
                .setPort(grpcPort)
                .setWeight(weight)
                .setAtTime(System.currentTimeMillis())
        );
    }

    class Server implements Runnable {
        @Override
        public void run() {
            try {
                sleep(10 * 1000L - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //存储自己的admin信息
                    adminNodeDao.insertOrUpdate(new AdminNodeModel()
                                    .setId(adminConfig.getIp()+":"+grpcPort)
                            .setIp(adminConfig.getIp())
                            .setPort(grpcPort)
                            .setWeight(weight)
                            .setAtTime(System.currentTimeMillis())
                    );
                    //计算mod值并发布
                    List<AdminNodeModel> adminNodeModels = adminNodeDao.selectList(new LambdaQueryWrapper<AdminNodeModel>()
                            .orderByDesc(AdminNodeModel::getId)
                    );
                    int adminNodeSize = adminNodeModels.size();
                    //判断自己在的位置
                    int index=0;
                    for (int i = 0; i < adminNodeSize; i++) {
                        if ((adminConfig.getIp()+":"+grpcPort).equals(adminNodeModels.get(i).getId())) {
                            index = i;
                            break;
                        }
                    }
                    //为避免任务丢失采用两个admin分配同一个值
                    if((adminNodeSize%2!=0)&&( index==adminNodeSize-1)){
                        applicationContext.publishEvent(new AdminModValue(this,1,0));
                    }else {
                        applicationContext.publishEvent(new AdminModValue(this,adminNodeSize/2,index%(adminNodeSize/2)));
                    }
                } catch (Exception e) {
                    log.error("admin server error", e);
                }
                try {
                    sleep(10 * 1000L - System.currentTimeMillis() % (10 * 1000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    /**
     * 定时清理过期的admin
     */
    @CarrotJobUnit( value = "clearExpiredAdmin",type = EJobUnitType.FIXED_SPEED, typeValue = "15")
    public long clearExecutorAdmin() {
        return adminNodeDao.delete(new LambdaQueryWrapper<AdminNodeModel>()
                .lt(AdminNodeModel::getAtTime, System.currentTimeMillis() - 15 * 1000L));
    }
}
