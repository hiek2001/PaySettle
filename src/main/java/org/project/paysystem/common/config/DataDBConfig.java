//package org.project.paysystem.common.config;
//
//import jakarta.persistence.EntityManagerFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//import java.util.Map;
//
//@Configuration
//@EnableJpaRepositories(
//        basePackages = {
//                "org.project.paysystem.user.repository",
//                "org.project.paysystem.revenue.repository",
//                "org.project.paysystem.streaming.repository"
//        },
//        entityManagerFactoryRef = "entityManagerFactory",    // 비즈니스 DB용 EntityManagerFactory
//        transactionManagerRef = "transactionManager"         // 비즈니스 DB용 TransactionManager
//)
//@RequiredArgsConstructor
//public class DataDBConfig {
//
//    private final JpaProperties jpaProperties;
//    private final HibernateProperties hibernateProperties;
//
//    @Value("${spring.datasource.data.url}")
//    private String url;
//
//    @Value("${spring.datasource.data.username}")
//    private String username;
//
//    @Value("${spring.datasource.data.password}")
//    private String password;
//
//    @Value("${spring.datasource.data.driver-class-name}")
//    private String driverClassName;
//
//    @Bean(name = "dataSource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .url(url)
//                .username(username)
//                .password(password)
//                .driverClassName(driverClassName)
//                .build();
//    }
//
//    @Bean(name = "entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
//        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
//                jpaProperties.getProperties(), new HibernateSettings());
//
//        properties.put("hibernate.show_sql", true);  // SQL 로그 출력
//        properties.put("hibernate.format_sql", true);  // SQL 포맷팅
//        properties.put("hibernate.highlight_sql", true);
//
//        return builder
//                .dataSource(dataSource())
//                .packages("org.project.paysystem.entity")  // 엔티티 클래스 패키지 설정
//                .persistenceUnit("dataPU")  // 비즈니스 데이터베이스에 대한 Persistence Unit 설정
//                .properties(properties)
//                .build();
//    }
//
//    @Bean(name="transactionManager")
//    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//    }
