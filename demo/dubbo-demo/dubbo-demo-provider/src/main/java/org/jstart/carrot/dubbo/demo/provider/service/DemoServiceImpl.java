package org.jstart.carrot.dubbo.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.jstart.carrot.dubbo.demo.api.DemoService;

@DubboService
public class DemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        String actualName = (name == null || name.isBlank()) ? "xxxx" : name;
        return "hello " + actualName;
    }
}
