package org.jstart.carrot.config.demo.presentation;

import org.jstart.carrot.config.support.CarrotConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class ConfigDemoController {
    @Autowired
    private CarrotConfigService subscriber;

    @Value("${server.port:0}")
    private int serverPort;

    @Value("${test:}")
    private String test;

    private String latest="latest";

    @GetMapping("/test")
    public TestVO test() {
        return new TestVO(serverPort, test,latest);
    }

    @GetMapping("/subscribe")
    public String subscribe() {
        if (subscriber == null) {
            return "carrot config disabled";
        }
        subscriber.subscribe("public", "DEFAULT_GROUP", "test.text",
                (key, oldSnap, newSnap) -> {
                     latest = newSnap.content();
                }
        );
        return "ok";
    }

    public record TestVO(int serverPort, String test,String latest) {
    }
}
