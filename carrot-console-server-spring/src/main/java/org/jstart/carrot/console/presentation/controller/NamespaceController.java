package org.jstart.carrot.console.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.jstart.carrot.console.comm.utils.ParameterUtil;
import org.jstart.carrot.console.comm.validator.ParameterValidator;
import org.jstart.carrot.console.application.dto.NameSpaceDTO;
import org.jstart.carrot.console.application.service.NamespaceService;
import org.jstart.carrot.console.application.vo.NamespaceVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/namespace")
public class NamespaceController {
    private final NamespaceService namespaceService;

    /**
     * 查询命名空间
     */
    @PostMapping("/all")
    public ExecutedResult<List<NamespaceVO>> getAllNamespaces(){
        return ExecutedResult.success(namespaceService.getAllNamespaces());
    }

    /**
     * 创建命名空间
     */
    @PostMapping("/create")
    public ExecutedResult<String> createNamespace(@RequestBody NameSpaceDTO req){
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("name"),req.getName())
                .addNotNullOrEmpty(ParameterUtil.named("description"),req.getDescription())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success( namespaceService.createNamespace(req));
    }

    /**
     * 修改命名空间
     */
    @PostMapping("/modify")
    public ExecutedResult<String> modify(@RequestBody NameSpaceDTO req){
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("id"),req.getId())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return namespaceService.modifyNamespace(req)?ExecutedResult.success():ExecutedResult.failed();
    }

    /**
     * 删除命名空间
     */
    @PostMapping("/delete")
    public ExecutedResult<String> delete(String id){
        return namespaceService.deleteNamespace(id)?ExecutedResult.success():ExecutedResult.failed();
    }
}
