package org.jstart.carrot.dubbo.demo.consumer.web;

import org.apache.dubbo.config.annotation.DubboReference;
import org.jstart.carrot.dubbo.demo.api.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    @DubboReference(group = "dubbo", check = false)
    private DemoService demoService;

    @GetMapping("/demo")
    public String demo(@RequestParam(defaultValue = "xxxx") String name) {
        return demoService.hello(name);
    }
}
