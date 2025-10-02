package com.taller5.aggregator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.taller5.aggregator.dal.inventory",   // repos de inventory
    entityManagerFactoryRef = "inventoryEntityManagerFactory",
    transactionManagerRef = "inventoryTransactionManager"
)
public class InventoryDataSourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties inventoryDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "inventoryDataSource")
  @Primary
  public DataSource inventoryDataSource(
      @Qualifier("inventoryDataSourceProperties") DataSourceProperties props) {
    return props.initializeDataSourceBuilder().build();
  }

  @Bean(name = "inventoryEntityManagerFactory")
  @Primary
  public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("inventoryDataSource") DataSource dataSource) {

    Map<String, Object> jpaProps = new HashMap<>();
    jpaProps.put("hibernate.hbm2ddl.auto", "none");
    jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

    return builder
        .dataSource(dataSource)
        .packages("com.taller5.aggregator.dal.inventory") // tus @Entity de inventory
        .properties(jpaProps)
        .persistenceUnit("inventoryPU")
        .build();
  }

  @Bean(name = "inventoryTransactionManager")
  @Primary
  public PlatformTransactionManager inventoryTransactionManager(
      @Qualifier("inventoryEntityManagerFactory")
      LocalContainerEntityManagerFactoryBean emf) {
    return new JpaTransactionManager(emf.getObject());
  }
}
