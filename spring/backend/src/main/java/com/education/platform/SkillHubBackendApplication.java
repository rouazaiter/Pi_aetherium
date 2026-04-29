package com.education.platform;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkillHubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillHubBackendApplication.class, args);
    }

}
