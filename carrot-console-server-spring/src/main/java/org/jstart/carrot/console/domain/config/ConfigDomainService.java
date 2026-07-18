package org.jstart.carrot.console.domain.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.application.dto.SearchConfigItem;
import org.jstart.carrot.console.comm.ConstantFactory;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.NumericUtil;
import org.jstart.carrot.console.comm.utils.StringUtil;
import org.jstart.carrot.console.domain.config.model.ConfigItemModel;
import org.jstart.carrot.console.infrastructure.repository.ConfigItemDao;
import org.jstart.carrot.scheduling.constant.KeyValue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigDomainService implements ConfigCommandDomainServer, ConfigQueryDomainServer{
    private final ConfigItemDao itemDao;
    @Override
    public ConfigItemModel selectById(String id) {
        return itemDao.selectById(id);
    }

    @Override
    public List<ConfigItemModel> findNameSpaceAndGroupAndDataIdUpdateTimeByOrSearch(List<KeyValue<String, Long>> orSearch) {
        LambdaQueryWrapper<ConfigItemModel> select = new LambdaQueryWrapper<ConfigItemModel>()
                .select(ConfigItemModel::getId, ConfigItemModel::getNamespace, ConfigItemModel::getGroupName,ConfigItemModel::getDataId, ConfigItemModel::getUpdateTime);
        orSearch.forEach(
                item -> select.or(
                        wrapper -> wrapper.eq(ConfigItemModel::getId, item.getKey())
                                .ne(ConfigItemModel::getUpdateTime, item.getValue())
                )
        );
        return itemDao.selectList(select);
    }

    @Override
    public PageResult<List<ConfigItemModel>> searchPage(SearchConfigItem dto, SFunction<ConfigItemModel, ?> ... columns) {
        if (NumericUtil.isNullOrEmpty(dto.getPageIndex())) {
            dto.setPageIndex(ConstantFactory.NUM1);
        }
        if (NumericUtil.isNullOrEmpty(dto.getPageSize())) {
            dto.setPageSize(-1);
        }
        IPage<ConfigItemModel> page = new Page<>();
        //页码
        page.setCurrent(dto.getPageIndex());
        page.setSize(dto.getPageSize());
        //构造查询条件
        LambdaQueryWrapper<ConfigItemModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(columns);
        //命名空间id
        if (StringUtil.isNotNullOrEmpty(dto.getEqNamespace())) {
            wrapper.eq(ConfigItemModel::getNamespace, dto.getEqNamespace());
        }
        //组名
        if (StringUtil.isNotNullOrEmpty(dto.getLikeGroupName())) {
            wrapper.like(ConfigItemModel::getGroupName, dto.getLikeGroupName());
        }
        //dataID
        if (StringUtil.isNotNullOrEmpty(dto.getLikeDataId())) {
            wrapper.like(ConfigItemModel::getDataId, dto.getLikeDataId());
        }
        IPage<ConfigItemModel> listPO = itemDao.selectPage(page, wrapper);
        return new PageResult<>(dto.getPageSize(), dto.getPageIndex(), listPO.getTotal(), listPO.getRecords());
    }

    @Override
    public void save(ConfigItemModel model) {
        itemDao.insertOrUpdate(model);
    }

    @Override
    public String create(ConfigItemModel configItemModel) {
        return itemDao.insert(configItemModel) > 0 ? configItemModel.getId() : null;
    }

    @Override
    public boolean update(ConfigItemModel configItemModel) {
        return itemDao.updateById(configItemModel) > 0;
    }

    @Override
    public boolean delete(String id) {
        return itemDao.deleteById(id) > 0;
    }
}
