package com.taller5.aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CommonJpaConfig {

  @Bean
  public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
    var vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false); // deja DDL en manos de tus propiedades

    Map<String, Object> jpaProps = new HashMap<>();
    // Asegura el mismo dialecto que usas en application.yml y en tus configs
    jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    jpaProps.put("hibernate.hbm2ddl.auto", "none");

    // El tercer parámetro (persistenceUnitManager) puede ir null
    return new EntityManagerFactoryBuilder(vendorAdapter, jpaProps, null);
  }
}
