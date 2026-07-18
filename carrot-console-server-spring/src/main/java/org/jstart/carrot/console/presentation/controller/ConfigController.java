package org.jstart.carrot.console.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.ParameterUtil;
import org.jstart.carrot.console.comm.validator.ParameterValidator;
import org.jstart.carrot.console.application.dto.CreateConfigItem;
import org.jstart.carrot.console.application.dto.ModifyConfigItem;
import org.jstart.carrot.console.application.dto.SearchConfigItem;
import org.jstart.carrot.console.application.service.ConfigService;
import org.jstart.carrot.console.application.vo.ConfigItemInfoVO;
import org.jstart.carrot.console.application.vo.ConfigItemSimpleVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/config")
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigService configService;

    /**
     * 查询配置项
     */
    @PostMapping("/get")
    public ExecutedResult<ConfigItemInfoVO> get(String id){
        return ExecutedResult.success(configService.getById(id));
    }

    /**
     * 查询
     */
    @PostMapping("/search/simple/page")
    public ExecutedResult<PageResult<List<ConfigItemSimpleVO>>> searchSimplePage(@RequestBody SearchConfigItem req){
        //参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("pageIndex"),req.getPageIndex())
                .addNotNullOrEmpty(ParameterUtil.named("pageSize"),req.getPageSize())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success(configService.searchSimplePage(req));
    }

    /**
     * 添加 配置项
     */
    @PostMapping("/add")
    public ExecutedResult<String> add(@RequestBody CreateConfigItem req){
        //参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("namespace"),req.getNamespace())
                .addNotNullOrEmpty(ParameterUtil.named("groupName"),req.getGroupName())
                .addNotNullOrEmpty(ParameterUtil.named("dataId"),req.getDataId())
                .addNotNullOrEmpty(ParameterUtil.named("content"),req.getContent())
                .addNotNullOrEmpty(ParameterUtil.named("contentType"),req.getContentType())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success(configService.add(req));
    }

    /**
     * 修改配置项
     */
    @PostMapping("/modify")
    public ExecutedResult<String> modify(@RequestBody ModifyConfigItem req){
        //参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("id"),req.getId())
                .addNotNullOrEmpty(ParameterUtil.named("content"),req.getContent())
                .addNotNullOrEmpty(ParameterUtil.named("contentType"),req.getContentType())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return configService.modify(req)?ExecutedResult.success():ExecutedResult.failed();
    }

    /**
     * 删除配置项
     */
    @PostMapping("/delete")
    public ExecutedResult<Boolean> delete(String id){
        return configService.delete(id)?ExecutedResult.success():ExecutedResult.failed();
    }
}
