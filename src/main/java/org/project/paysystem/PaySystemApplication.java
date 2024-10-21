package org.project.paysystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.project.paysystem.repository")
public class PaySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaySystemApplication.class, args);
    }

}
