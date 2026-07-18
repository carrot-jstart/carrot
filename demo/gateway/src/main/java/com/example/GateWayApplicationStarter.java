package com.example;


import org.jstart.carrot.discovery.annotation.EnableCarrotDiscovery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableCarrotDiscovery
public class GateWayApplicationStarter {
    public static void main(String[] args)  {
        System.setProperty("spring.amqp.deserialization.trust.all", "true");
        SpringApplication.run(GateWayApplicationStarter.class, args);
    }
}
