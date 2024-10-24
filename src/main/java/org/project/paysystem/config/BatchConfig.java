package org.project.paysystem.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {

    @Value("${spring.datasource.data.url}")
    private String url;

    @Value("${spring.datasource.data.username}")
    private String username;

    @Value("${spring.datasource.data.password}")
    private String password;

    @Value("${spring.datasource.data.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.meta.url}")
    private String metaUrl;

    @Value("${spring.datasource.meta.username}")
    private String metaUsername;

    @Value("${spring.datasource.meta.password}")
    private String metaPassword;

    @Value("${spring.datasource.meta.driver-class-name}")
    private String metaDriverClassName;

    @Bean
    @Primary
    public DataSource batchDataSource() {
        return DataSourceBuilder.create()
                .url(metaUrl)
                .username(metaUsername)
                .password(metaPassword)
                .driverClassName(metaDriverClassName)
                .build();
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
        jpaTransactionManager.setDataSource(dataSource());
        return jpaTransactionManager;
    }
}
