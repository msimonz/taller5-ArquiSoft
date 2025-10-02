package com.taller5.aggregator.config;

import com.mysql.cj.jdbc.MysqlXADataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;          // <-- import de @Value
import org.springframework.boot.jdbc.XADataSourceWrapper;      // <-- wrapper provisto por Snowdrop
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class XaConfig {

  private static Map<String, Object> jpaProps() {
    Map<String, Object> p = new HashMap<>();
    p.put("hibernate.hbm2ddl.auto", "none");
    p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    return p;
  }

  private static LocalContainerEntityManagerFactoryBean jtaEmf(
      DataSource ds, String unitName, String... packages) {
    var emf = new LocalContainerEntityManagerFactoryBean();
    emf.setJtaDataSource(ds);
    emf.setPackagesToScan(packages);
    emf.setPersistenceUnitName(unitName);
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setJpaPropertyMap(jpaProps());
    emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    return emf;
  }

  // ===== INVENTORY =====
  @Bean
  public DataSource inventoryDataSource(
      XADataSourceWrapper wrapper,
      @Value("${datasources.inventory.url}") String url,
      @Value("${datasources.inventory.username}") String user,
      @Value("${datasources.inventory.password}") String pass
  ) throws Exception {
    var xa = new MysqlXADataSource();
    xa.setUrl(url); xa.setUser(user); xa.setPassword(pass);
    return wrapper.wrapDataSource(xa);
  }

  @Primary
  @Bean(name = "inventoryEmf")
  public LocalContainerEntityManagerFactoryBean inventoryEmf(DataSource inventoryDataSource) {
    return jtaEmf(inventoryDataSource, "inventoryPU", "com.taller5.aggregator.inventory");
  }

  // ===== BILLING =====
  @Bean
  public DataSource billingDataSource(
      XADataSourceWrapper wrapper,
      @Value("${datasources.billing.url}") String url,
      @Value("${datasources.billing.username}") String user,
      @Value("${datasources.billing.password}") String pass
  ) throws Exception {
    var xa = new MysqlXADataSource();
    xa.setUrl(url); xa.setUser(user); xa.setPassword(pass);
    return wrapper.wrapDataSource(xa);
  }

  @Bean(name = "billingEmf")
  public LocalContainerEntityManagerFactoryBean billingEmf(DataSource billingDataSource) {
    return jtaEmf(billingDataSource, "billingPU", "com.taller5.aggregator.billing");
  }

  // ===== PAYMENTS =====
  @Bean
  public DataSource paymentsDataSource(
      XADataSourceWrapper wrapper,
      @Value("${datasources.payments.url}") String url,
      @Value("${datasources.payments.username}") String user,
      @Value("${datasources.payments.password}") String pass
  ) throws Exception {
    var xa = new MysqlXADataSource();
    xa.setUrl(url); xa.setUser(user); xa.setPassword(pass);
    return wrapper.wrapDataSource(xa);
  }

  @Bean(name = "paymentsEmf")
  public LocalContainerEntityManagerFactoryBean paymentsEmf(DataSource paymentsDataSource) {
    return jtaEmf(paymentsDataSource, "paymentsPU", "com.taller5.aggregator.payments");
  }
}
