package es.vargontoc.agents.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DevopsAgentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsAgentServerApplication.class, args);
    }

}
