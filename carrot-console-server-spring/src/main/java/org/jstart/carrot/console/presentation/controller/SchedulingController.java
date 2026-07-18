package org.jstart.carrot.console.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.dto.OrderByDTO;
import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.comm.utils.ParameterUtil;
import org.jstart.carrot.console.comm.validator.ParameterValidator;
import org.jstart.carrot.console.application.dto.SearchSchedulingExecutorNode;

import org.jstart.carrot.console.application.dto.SearchSchedulingJobRecord;
import org.jstart.carrot.console.application.dto.SearchSchedulingJobUnit;
import org.jstart.carrot.console.application.service.SchedulingService;
import org.jstart.carrot.console.application.vo.SchedulingExecutorNodeVO;
import org.jstart.carrot.console.application.vo.SchedulingJobRecordVO;
import org.jstart.carrot.console.application.vo.SchedulingJobUnitVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/scheduling")
public class SchedulingController {
    private final SchedulingService schedulingService;
    @PostMapping("/executor/node/search/page")
    public ExecutedResult<PageResult<List<SchedulingExecutorNodeVO>>> nodeSearchPage(@RequestBody SearchSchedulingExecutorNode dto) {
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("pageIndex"),dto.getPageIndex())
                .addNotNullOrEmpty(ParameterUtil.named("pageSize"),dto.getPageSize())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success(schedulingService.searchPageExecutorNode(dto));
    }

    /**
     * 获取命名空间下的执行器名称
     */
    @PostMapping("/job/unit/get/executor")
    public ExecutedResult<List<String>> getExecutorByNamespace(String namespaceId){
        return ExecutedResult.success(schedulingService.getUnitExecutorNameByNamespace(namespaceId));
    }

    /**
     * 获取命名空间下的组
     */
    @PostMapping("/job/unit/get/group")
    public ExecutedResult<List<String>> getGroupByNamespace(String namespaceId){
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("namespaceId"),namespaceId)
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success(schedulingService.getUnitGroupByNamespace(namespaceId));
    }


    /**
     * 查询执行方法
     */
    @PostMapping("/job/unit/search/page")
    public ExecutedResult<PageResult<List<SchedulingJobUnitVO>>> searchPageJobUnit(@RequestBody SearchSchedulingJobUnit req){
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("pageIndex"),req.getPageIndex())
                .addNotNullOrEmpty(ParameterUtil.named("pageSize"),req.getPageSize())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        return ExecutedResult.success(schedulingService.searchPageJobUnit(req));
    }

    /**
     * 启用调度单位
     */
    @PostMapping("/job/unit/enable")
    public ExecutedResult<String> enableJobUnit(String id){
        return schedulingService.enableJobUnit(id)?ExecutedResult.success():ExecutedResult.failed();
    }

    /**
     * 禁用调度单位
     */
    @PostMapping("/job/unit/disable")
    public ExecutedResult<String> disableJobUnit(String id){
        return schedulingService.disableJobUnit(id)?ExecutedResult.success():ExecutedResult.failed();
    }


    /**
     * 根据id查询执行方法
     */
    @PostMapping("/job/unit/get")
    public ExecutedResult<SchedulingJobUnitVO> getJobUnit(String id){
        return ExecutedResult.success(schedulingService.getUnit(id));
    }

    /**
     * 删除执行方法
     */
    @PostMapping("/job/unit/delete")
    public ExecutedResult<String> deleteJobUnit(String id){
        return schedulingService.deleteJobUnit(id)?ExecutedResult.success():ExecutedResult.failed();
    }

    /**
     * 查询执行日志
     */
    @PostMapping("/job/record/search/page")
    public ExecutedResult<PageResult<List<SchedulingJobRecordVO>>> searchJobRecord(@RequestBody SearchSchedulingJobRecord req){
        //进行参数校验
        ParameterValidator validator = new ParameterValidator()
                .addNotNullOrEmpty(ParameterUtil.named("pageIndex"),req.getPageIndex())
                .addNotNullOrEmpty(ParameterUtil.named("pageSize"),req.getPageSize())
                ;
        if(validator.validate().getIsFiled()){
            return ExecutedResult.failed(validator.validate().getErrorMsg());
        }
        req.setOrderBy(List.of(new OrderByDTO(3)));
        return ExecutedResult.success(schedulingService.searchJobRecord(req));
    }

    /**
     * 查询单个执行日志
     */
    @PostMapping("/job/record/get")
    public ExecutedResult<SchedulingJobRecordVO> getJobRecord(String id){
        return ExecutedResult.success(schedulingService.getJobRecord(id));
    }


    /**
     * 删除执行日志
     */
    @PostMapping("/job/record/delete")
    public ExecutedResult<String> deleteJobRecord(@RequestBody List<String> ids){
        return schedulingService.deleteJobRecord(ids)?ExecutedResult.success():ExecutedResult.failed();
    }

}
