package org.jstart.carrot.console.application.service;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.application.dto.CreateConfigItem;
import org.jstart.carrot.console.application.dto.ModifyConfigItem;
import org.jstart.carrot.console.application.dto.SearchConfigItem;
import org.jstart.carrot.console.application.dto.ServiceKey;
import org.jstart.carrot.console.application.vo.ConfigItemInfoVO;
import org.jstart.carrot.console.application.vo.ConfigItemSimpleVO;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.domain.config.ConfigCommandDomainServer;
import org.jstart.carrot.console.domain.config.ConfigQueryDomainServer;
import org.jstart.carrot.console.domain.config.model.ConfigItemModel;
import org.jstart.carrot.scheduling.constant.KeyValue;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigCommandDomainServer configCommandDomainServer;
    private final ConfigQueryDomainServer configQueryDomainServer;
    public ConfigItemInfoVO find(ServiceKey key) {
        ConfigItemModel model = configQueryDomainServer.selectById(buildId(key));
        return new ConfigItemInfoVO()
                .setId(model.getId())
                .setNamespace(model.getNamespace())
                .setGroupName(model.getGroupName())
                .setDataId(model.getDataId())
                .setContent(model.getContent())
                .setMd5(model.getMd5())
                .setContentType(model.getContentType())
                .setCreateTime(model.getCreateTime())
                .setUpdateTime(model.getUpdateTime());
    }


    public void publish(ServiceKey serviceKey, String content, String md5, String contentType) {
        long now = System.currentTimeMillis();
        String id = buildId(serviceKey);
        String normalizedContent = content == null ? "" : content;
        String normalizedMd5 = (md5 == null || md5.isBlank()) ? md5Hex(normalizedContent) : md5.trim();
        String normalizedType = contentType == null ? "" : contentType.trim();
        ConfigItemModel created = new ConfigItemModel();
        created.setId(id);
        created.setNamespace(serviceKey.getNamespace());
        created.setGroupName(serviceKey.getGroup());
        created.setDataId(serviceKey.getServiceName());
        created.setContent(normalizedContent);
        created.setMd5(normalizedMd5);
        created.setContentType(normalizedType);
        created.setCreateTime(now);
        created.setUpdateTime(now);
        configCommandDomainServer.save(created);
    }

    public ConfigItemInfoVO getById(String id) {
        ConfigItemModel model = configQueryDomainServer.selectById(id);
        return new ConfigItemInfoVO()
                .setId(model.getId())
                .setNamespace(model.getNamespace())
                .setGroupName(model.getGroupName())
                .setDataId(model.getDataId())
                .setContent(model.getContent())
                .setMd5(model.getMd5())
                .setContentType(model.getContentType())
                .setCreateTime(model.getCreateTime())
                .setUpdateTime(model.getUpdateTime());

    }

    /**
     *
     * @param key
     * @return
     */
    public static String buildId(ServiceKey key) {
        return key.getNamespace() + "@" + key.getGroup() + "@" + key.getServiceName();
    }

    public static String md5Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[digest.length * 2];
            char[] alphabet = "0123456789abcdef".toCharArray();
            for (int i = 0; i < digest.length; i++) {
                int v = digest[i] & 0xFF;
                hex[i * 2] = alphabet[v >>> 4];
                hex[i * 2 + 1] = alphabet[v & 0x0F];
            }
            return new String(hex);
        } catch (Exception e) {
            return "";
        }
    }

    public  List<KeyValue<ServiceKey,Long>> findChanged(List<KeyValue<ServiceKey, Long>> list) {
        List<KeyValue<String, Long>> orSearch = list.stream().map(item -> new KeyValue<>(buildId(item.getKey()), item.getValue()))
                .toList();
     return configQueryDomainServer.findNameSpaceAndGroupAndDataIdUpdateTimeByOrSearch(orSearch)
             .stream()
             .map(item-> new KeyValue<>(new ServiceKey()
                     .setNamespace(item.getNamespace())
                     .setGroup(item.getGroupName())
                     .setServiceName(item.getDataId())
                     , item.getUpdateTime()))
             .toList();
    }

    public PageResult<List<ConfigItemSimpleVO>> searchSimplePage(SearchConfigItem dto) {
        PageResult<List<ConfigItemModel>> listPageResult = configQueryDomainServer.searchPage(dto,
                ConfigItemModel::getId,
                ConfigItemModel::getNamespace,
                ConfigItemModel::getGroupName,
                ConfigItemModel::getDataId,
                ConfigItemModel::getContentType,
                ConfigItemModel::getUpdateTime
        );
        return new PageResult<>(listPageResult.getLimit(), listPageResult.getPage(), listPageResult.getTotal(),
                listPageResult.getData().stream()
                        .map(item -> new ConfigItemSimpleVO()
                                .setId(item.getId())
                                .setNamespace(item.getNamespace())
                                .setGroupName(item.getGroupName())
                                .setDataId(item.getDataId())
                                .setContentType(item.getContentType())
                                .setUpdateTime(item.getUpdateTime())
                        )
                        .toList()
                );
    }

    /**
     * 添加
     * @param dto
     * @return
     */
    public String add(CreateConfigItem dto) {
      return configCommandDomainServer.create(new ConfigItemModel()
              .setId(buildId(new ServiceKey().setNamespace(dto.getNamespace()).setGroup(dto.getGroupName()).setServiceName(dto.getDataId())))
              .setNamespace(dto.getNamespace())
              .setGroupName(dto.getGroupName())
              .setDataId(dto.getDataId())
              .setContent(dto.getContent())
              .setMd5(md5Hex(dto.getContent()))
              .setContentType(dto.getContentType())
              .setCreateTime(System.currentTimeMillis())
              .setUpdateTime(System.currentTimeMillis())
      );
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    public boolean modify(ModifyConfigItem dto) {
        return configCommandDomainServer.update(new ConfigItemModel()
                .setId(dto.getId())
                .setContent(dto.getContent())
                .setMd5(md5Hex(dto.getContent()))
                .setContentType(dto.getContentType())
                .setUpdateTime(System.currentTimeMillis())
        );
    }

    public boolean delete(String id) {
        return configCommandDomainServer.delete(id);
    }
}
