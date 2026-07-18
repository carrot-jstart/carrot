package org.jstart.carrot.console.domain.namespace;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.domain.namespace.model.NamespaceModel;
import org.jstart.carrot.console.infrastructure.config.AdminConfig;
import org.jstart.carrot.console.infrastructure.repository.NamespaceDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NamespaceDomainService implements NamespaceQueryDomainServer, NamespaceCommandDomainServer, CommandLineRunner {
    private final NamespaceDao namespaceDao;
    private final AdminConfig adminConfig;
    @Override
    public List<NamespaceModel> getAllNamespaces() {
        return namespaceDao.selectList(new LambdaQueryWrapper<NamespaceModel>()
                        .ne(NamespaceModel::getId, adminConfig.getAdminNamespace())
                .orderByDesc(NamespaceModel::getCreateTime));
    }

    @Override
    public boolean exists(String id) {
        return namespaceDao.selectById(id)!=null;
    }

    @Override
    public String createNamespace(NamespaceModel namespaceModel) {
        namespaceDao.insert(namespaceModel);
        return namespaceModel.getId();
    }

    @Override
    public boolean modifyNamespace(NamespaceModel namespaceModel) {
        return namespaceDao.updateById(namespaceModel) > 0;
    }

    @Override
    public boolean deleteNamespace(String id) {
        return namespaceDao.deleteById(id) > 0;
    }

    @Override
    public void run(String... args) {
        namespaceDao.insertOrUpdate(new NamespaceModel()
                .setId(adminConfig.getAdminNamespace())
                .setName(adminConfig.getAdminNamespace())
                .setDescription("admin namespace")
        );
    }
}
