package org.jstart.carrot.discovery.demo.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testControler {
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}