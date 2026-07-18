package org.jstart.carrot.console;

import org.jstart.carrot.scheduling.annotation.EnableCarrotScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.jstart.carrot.console")
@EnableCarrotScheduling
public class CarrotConsoleAdminApplication {
    public static void main(String[] args) {
       SpringApplication.run(CarrotConsoleAdminApplication.class, args);
    }
}
