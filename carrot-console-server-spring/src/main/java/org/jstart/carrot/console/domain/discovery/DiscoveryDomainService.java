package org.jstart.carrot.console.domain.discovery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.application.dto.SearchDiscoveryInstance;
import org.jstart.carrot.console.application.dto.SearchDiscoveryService;
import org.jstart.carrot.console.comm.ConstantFactory;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.ListUtil;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.comm.utils.StringUtil;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryInstanceModel;
import org.jstart.carrot.console.domain.discovery.model.DiscoveryServiceModel;
import org.jstart.carrot.console.infrastructure.repository.DiscoveryInstanceDao;
import org.jstart.carrot.console.infrastructure.repository.DiscoveryServiceDao;
import org.jstart.carrot.scheduling.annotation.CarrotJobUnit;
import org.jstart.carrot.scheduling.constant.EJobUnitType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryDomainService implements DiscoveryQueryDomainServer, DiscoveryCommandDomainServer, CommandLineRunner {
    private final DiscoveryInstanceDao instanceDao;
    private final DiscoveryServiceDao serviceDao;

    @CarrotJobUnit(value = "discovery_clear_expired",type = EJobUnitType.FIXED_SPEED,typeValue = "30")
    public Integer deleteExpiredInstance() {
        //查询20之前的心跳的数据
        int delete = instanceDao.delete(new LambdaQueryWrapper<DiscoveryInstanceModel>()
                .lt(DiscoveryInstanceModel::getLastHeartbeatAt, System.currentTimeMillis() - 20 * 1000)
        );
        serviceDao.selectList(new LambdaQueryWrapper<>()).forEach(
                item->{
                    //查询数据量
                    Long value = instanceDao.selectCount(new LambdaQueryWrapper<DiscoveryInstanceModel>()
                            .eq(DiscoveryInstanceModel::getNamespace, item.getNamespace())
                            .eq(DiscoveryInstanceModel::getGroupName, item.getGroupName())
                            .eq(DiscoveryInstanceModel::getServiceName, item.getServiceName())
                    );
                    if(value<=0){
                        serviceDao.deleteById(item.getId());
                    }else {
                        serviceDao.updateById(new DiscoveryServiceModel()
                                .setId(item.getId())
                                .setInstanceCount(value)
                        );
                    }
                }
        );
        return delete;
    }

    @Override
    public String generateServiceId(String namespace, String groupName, String serviceName) {
        return namespace+":"+groupName+":"+serviceName;
    }

    @Override
    public void saveModel(DiscoveryInstanceModel model) {
         instanceDao.insertOrUpdate(model);
         //查询数据量
         long count = instanceDao.selectCount(new LambdaQueryWrapper<DiscoveryInstanceModel>()
                 .eq(DiscoveryInstanceModel::getNamespace, model.getNamespace())
                 .eq(DiscoveryInstanceModel::getGroupName, model.getGroupName())
                 .eq(DiscoveryInstanceModel::getServiceName, model.getServiceName())
         );
        serviceDao.insertOrUpdate(new DiscoveryServiceModel()
                .setId(this.generateServiceId(model.getNamespace(), model.getGroupName(), model.getServiceName()))
                .setNamespace(model.getNamespace())
                .setGroupName(model.getGroupName())
                .setServiceName(model.getServiceName())
                .setInstanceCount(NumericUtil.isNullOrEmpty(count)? ConstantFactory.NUM0:count)
        );
    }

    @Override
    public boolean deleteInstanceById(String id) {
        DiscoveryInstanceModel model = instanceDao.selectById(id);

        boolean res = instanceDao.deleteById(id) > 0;
        if(res){
                //查询数据量
            long count = instanceDao.selectCount(new LambdaQueryWrapper<DiscoveryInstanceModel>()
                    .eq(DiscoveryInstanceModel::getNamespace, model.getNamespace())
                    .eq(DiscoveryInstanceModel::getGroupName, model.getGroupName())
                    .eq(DiscoveryInstanceModel::getServiceName, model.getServiceName())
            );
                if(count<=0){
                    serviceDao.deleteById(this.generateServiceId(model.getNamespace(), model.getGroupName(), model.getServiceName()));
                }else{
                    //统计数量
                    serviceDao.insertOrUpdate(new DiscoveryServiceModel()
                            .setId(this.generateServiceId(model.getNamespace(), model.getGroupName(), model.getServiceName()))
                            .setNamespace(model.getNamespace())
                            .setGroupName(model.getGroupName())
                            .setServiceName(model.getServiceName())
                            .setInstanceCount(NumericUtil.isNullOrEmpty(count)?ConstantFactory.NUM0:count)
                    );
                }
        }
        return res;
    }

    @Override
    public DiscoveryInstanceModel selectInstanceById(String id) {
        return instanceDao.selectById(id);
    }

    @Override
    public PageResult<List<DiscoveryInstanceModel>> selectInstance(SearchDiscoveryInstance dto) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<DiscoveryInstanceModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<DiscoveryInstanceModel> wrapper = new LambdaQueryWrapper<>();
        //命名空间id
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespace())) {
            wrapper.eq(DiscoveryInstanceModel::getNamespace, dto.getEqNamespace());
        }
        //具体组
        if (StringUtil.isNotNullOrEmpty(dto.getEqGroup())) {
            wrapper.eq(DiscoveryInstanceModel::getGroupName, dto.getEqGroup());
        }
        //组名
        if (StringUtil.isNotNullOrEmpty(dto.getEqServiceName())) {
            wrapper.eq(DiscoveryInstanceModel::getServiceName, dto.getEqServiceName());
        }
        //模糊组
        if (StringUtil.isNotNullOrEmpty(dto.getLikeGroup())) {
            wrapper.like(DiscoveryInstanceModel::getGroupName, dto.getLikeGroup());
        }
        //模糊服务名
        if (StringUtil.isNotNullOrEmpty(dto.getLikeServiceName())) {
            wrapper.like(DiscoveryInstanceModel::getServiceName, dto.getLikeServiceName());
        }
        IPage<DiscoveryInstanceModel> listPO = instanceDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public PageResult<List<DiscoveryServiceModel>> searchService(SearchDiscoveryService dto) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<DiscoveryServiceModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<DiscoveryServiceModel> wrapper = new LambdaQueryWrapper<>();
        //命名空间id
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespace())) {
            wrapper.eq(DiscoveryServiceModel::getNamespace, dto.getEqNamespace());
        }

        //模糊组
        if (StringUtil.isNotNullOrEmpty(dto.getLikeGroup())) {
            wrapper.like(DiscoveryServiceModel::getGroupName, dto.getLikeGroup());
        }
        //模糊服务名
        if (StringUtil.isNotNullOrEmpty(dto.getLikeServiceName())) {
            wrapper.like(DiscoveryServiceModel::getServiceName, dto.getLikeServiceName());
        }
        IPage<DiscoveryServiceModel> listPO = serviceDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public void run(String... args) throws Exception {
        deleteExpiredInstance();
    }
}
