package com.shield.config;


import com.zaxxer.hikari.HikariDataSource;
import io.github.jhipster.config.JHipsterConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "sqlserverEntityManagerFactory",
    transactionManagerRef = "sqlserverTransactionManager",
    basePackages = {"com.shield.sqlserver.repository"}
)
//@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class SqlserverDatasourceConfig {

    @Bean(name = "sqlserverDataSource")
    @ConfigurationProperties(prefix = "spring.sqlserver-datasource")
    public DataSource dataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "sqlserverJpaVendorAdapter")
    @ConfigurationProperties(prefix = "spring.sqlserver-jpa")
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean(name = "sqlserverEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("sqlserverDataSource") DataSource dataSource,
        @Qualifier("sqlserverJpaVendorAdapter") JpaVendorAdapter jpaVendorAdapter
    ) {
//        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
//        jpaVendorAdapter.setGenerateDdl(false);
//        jpaVendorAdapter.setShowSql(true);
//        jpaVendorAdapter.setDatabase(Database.SQL_SERVER);
//        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.SQLServer2012Dialect");
//
//        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
//
//        factoryBean.setDataSource(customerDataSource());
//        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
//        factoryBean.setPackagesToScan(CustomerConfig.class.getPackage().getName());

        LocalContainerEntityManagerFactoryBean factoryBean = builder
            .dataSource(dataSource)
            .packages("com.shield.sqlserver.domain")
            .persistenceUnit("sqlserver")
            .build();
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        return factoryBean;
    }

    @Bean(name = "sqlserverTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("sqlserverEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
