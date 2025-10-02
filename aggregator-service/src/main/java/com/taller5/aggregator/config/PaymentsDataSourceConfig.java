package com.taller5.aggregator.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.taller5.aggregator.dal.payments",
    entityManagerFactoryRef = "paymentsEntityManagerFactory",
    transactionManagerRef = "paymentsTransactionManager"
)
public class PaymentsDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.payments")
  public DataSourceProperties paymentsDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "paymentsDataSource")
  public DataSource paymentsDataSource(
      @Qualifier("paymentsDataSourceProperties") DataSourceProperties props) {
    return props.initializeDataSourceBuilder().build();
  }

  @Bean(name = "paymentsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean paymentsEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("paymentsDataSource") DataSource dataSource) {

    Map<String, Object> jpaProps = new HashMap<>();
    jpaProps.put("hibernate.hbm2ddl.auto", "none");
    jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

    return builder
        .dataSource(dataSource)
        .packages("com.taller5.aggregator.dal.payments")
        .properties(jpaProps)
        .persistenceUnit("paymentsPU")
        .build();
  }

  @Bean(name = "paymentsTransactionManager")
  public PlatformTransactionManager paymentsTransactionManager(
      @Qualifier("paymentsEntityManagerFactory")
      LocalContainerEntityManagerFactoryBean emf) {
    return new JpaTransactionManager(Objects.requireNonNull(emf.getObject()));
  }
}
