package com.shield.config;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;


/**
 * 门禁SQL_SERVER数据库配置
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "chepaiparkSqlserverEntityManagerFactory",
    transactionManagerRef = "chepaiparkSqlserverTransactionManager",
    basePackages = {"com.shield.chepaipark.repository"}
)
public class ChePaiParkSqlserverDatasourceConfig {

    @Bean(name = "chepaiparkSqlserverDataSource")
    @ConfigurationProperties(prefix = "spring.chepaipark-sqlserver-datasource")
    public DataSource dataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "chepaiparkSqlserverJpaVendorAdapter")
    @ConfigurationProperties(prefix = "spring.chepaipark-sqlserver-jpa")
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean(name = "chepaiparkSqlserverEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("chepaiparkSqlserverDataSource") DataSource dataSource,
        @Qualifier("chepaiparkSqlserverJpaVendorAdapter") JpaVendorAdapter jpaVendorAdapter
    ) {
        LocalContainerEntityManagerFactoryBean factoryBean = builder
            .dataSource(dataSource)
            .packages("com.shield.chepaipark.domain")
            .persistenceUnit("chepaipark")
            .build();
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        return factoryBean;
    }

    @Bean(name = "chepaiparkSqlserverTransactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("chepaiparkSqlserverEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
