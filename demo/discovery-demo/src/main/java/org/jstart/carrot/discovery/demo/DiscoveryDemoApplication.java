package org.jstart.carrot.discovery.demo;

import org.jstart.carrot.discovery.annotation.EnableCarrotDiscovery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCarrotDiscovery
public class DiscoveryDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryDemoApplication.class, args);
    }
}

