//package org.project.paysystem.common.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class BatchConfig {
//
//    @Value("${spring.datasource.meta.url}")
//    private String metaUrl;
//
//    @Value("${spring.datasource.meta.username}")
//    private String metaUsername;
//
//    @Value("${spring.datasource.meta.password}")
//    private String metaPassword;
//
//    @Value("${spring.datasource.meta.driver-class-name}")
//    private String metaDriverClassName;
//
//    @Bean(name = "metaDataSource")
//    @Primary
//    public DataSource metaDataSource() {
//        return DataSourceBuilder.create()
//                .url(metaUrl)
//                .username(metaUsername)
//                .password(metaPassword)
//                .driverClassName(metaDriverClassName)
//                .build();
//    }
//
//    @Bean(name = "metaTransactionManager")
//    @Primary
//    public PlatformTransactionManager metaTransactionManager() {
//        return new DataSourceTransactionManager(metaDataSource());
//    }
//
//}
