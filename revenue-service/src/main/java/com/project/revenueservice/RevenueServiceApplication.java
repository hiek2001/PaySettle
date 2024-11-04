package com.project.revenueservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {
		"com.project.revenueservice.repository"
})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class RevenueServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevenueServiceApplication.class, args);
	}

}
