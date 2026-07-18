package org.jstart.carrot.console.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.jstart.carrot.console.comm.entity.vo.PageResult;
import org.jstart.carrot.console.application.dto.SearchDiscoveryInstance;
import org.jstart.carrot.console.application.dto.SearchDiscoveryService;
import org.jstart.carrot.console.application.service.DiscoveryService;
import org.jstart.carrot.console.application.vo.DiscoveryInstanceVO;
import org.jstart.carrot.console.application.vo.DiscoveryServiceVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/discovery")
@RequiredArgsConstructor
public class DiscoveryController {
    private final DiscoveryService discoveryService;


    /**
     * 查询服务
     * @param dto
     * @return
     */
    @PostMapping("/service/search")
    public ExecutedResult<PageResult<List<DiscoveryServiceVO>>> search(@RequestBody SearchDiscoveryService dto) {
        return ExecutedResult.success(discoveryService.searchService(dto));
    }

    /**
     * 查询服务实例
     */
    @PostMapping("/instance/search")
    public ExecutedResult<PageResult<List<DiscoveryInstanceVO>>> searchInstances(@RequestBody SearchDiscoveryInstance dto) {
        return ExecutedResult.success(discoveryService.selectInstance(dto));
    }
}
