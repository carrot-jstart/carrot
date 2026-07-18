package org.jstart.carrot.console.presentation.controller;

import org.jstart.carrot.console.comm.entity.vo.ExecutedResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/health")
    public ExecutedResult<String> health() {
        return ExecutedResult.success("UP");
    }
}
