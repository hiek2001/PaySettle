package org.project.paysystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.project.paysystem", exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "org.project.paysystem.user")  // 최상위 패키지 스캔
@EntityScan(basePackages = "org.project.paysystem.user.entity") // User 엔티티가 위치한 패키지
@EnableJpaRepositories(basePackages = "org.project.paysystem.user.repository",
                        entityManagerFactoryRef = "entityManagerFactory",
                        transactionManagerRef = "transactionManager"
)
@EnableScheduling
public class PaySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaySystemApplication.class, args);
    }

}
