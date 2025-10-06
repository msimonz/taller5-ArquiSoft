package com.taller5.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import com.mysql.cj.jdbc.MysqlXADataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JtaDataSourceConfig {

  // -------------------- INVENTORY --------------------
  @Bean
  @ConfigurationProperties("datasources.inventory")
  public DataSourceProperties inventoryDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "inventoryDataSource")
  public DataSource inventoryDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties props,
                                        XADataSourceWrapper xaWrapper) throws Exception {
    MysqlXADataSource mysqlXa = new MysqlXADataSource();
    // MysqlXADataSource uses setUrl, setUser, setPassword
    String url = props.getUrl();
    // Añadir parámetros específicos para timeouts y XA
    if (!url.contains("innodb_lock_wait_timeout")) {
      url += (url.contains("?") ? "&" : "?") + "innodb_lock_wait_timeout=30";
    }
    if (!url.contains("autoReconnect")) {
      url += "&autoReconnect=true";
    }
    if (!url.contains("pinGlobalTxToPhysicalConnection")) {
      url += "&pinGlobalTxToPhysicalConnection=true";
    }
    
    mysqlXa.setUrl(url);
    mysqlXa.setUser(props.getUsername());
    mysqlXa.setPassword(props.getPassword());
    try {
    // Este setter existe en Connector/J XA datasource
    mysqlXa.setPinGlobalTxToPhysicalConnection(true);
    } catch (Throwable t) {
        // en caso raro de driver + clase sin el setter, confiar en el parámetro URL
    }
    // wrap it using Spring's XA wrapper (provided by the Narayana starter)
    return xaWrapper.wrapDataSource(mysqlXa);
  }

  @Bean(name = "inventoryEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean inventoryEntityManagerFactory(
      @Qualifier("inventoryDataSource") DataSource ds) {
    var vendor = new HibernateJpaVendorAdapter();
    var emf = new LocalContainerEntityManagerFactoryBean();
    emf.setJtaDataSource(ds);
    emf.setPackagesToScan("com.taller5.inventory.model");
    emf.setPersistenceUnitName("inventoryPU");
    emf.setJpaVendorAdapter(vendor);

    Map<String,Object> props = new HashMap<>();
    props.put("hibernate.hbm2ddl.auto","none");
    props.put("jakarta.persistence.transactionType","JTA");
    // No forzar hibernate.transaction.jta.platform aquí
    emf.setJpaPropertyMap(props);
    return emf;
  }

  @Configuration
  @EnableJpaRepositories(
      basePackages = "com.taller5.inventory.repository",
      entityManagerFactoryRef = "inventoryEntityManagerFactory",
      transactionManagerRef = "transactionManager"
  )
  static class InventoryJpaConfig {}

  // -------------------- PAYMENTS --------------------
  @Bean
  @ConfigurationProperties("datasources.payments")
  public DataSourceProperties paymentsDataSourceProperties() { return new DataSourceProperties(); }

  @Bean(name = "paymentsDataSource")
  public DataSource paymentsDataSource(@Qualifier("paymentsDataSourceProperties") DataSourceProperties props,
                                       XADataSourceWrapper xaWrapper) throws Exception {
    MysqlXADataSource mysqlXa = new MysqlXADataSource();
    String url = props.getUrl();
    // Añadir parámetros específicos para timeouts y XA
    if (!url.contains("innodb_lock_wait_timeout")) {
      url += (url.contains("?") ? "&" : "?") + "innodb_lock_wait_timeout=30";
    }
    if (!url.contains("autoReconnect")) {
      url += "&autoReconnect=true";
    }
    if (!url.contains("pinGlobalTxToPhysicalConnection")) {
      url += "&pinGlobalTxToPhysicalConnection=true";
    }
    
    mysqlXa.setUrl(url);
    mysqlXa.setUser(props.getUsername());
    mysqlXa.setPassword(props.getPassword());
    try {
    // Este setter existe en Connector/J XA datasource
    mysqlXa.setPinGlobalTxToPhysicalConnection(true);
    } catch (Throwable t) {
        // en caso raro de driver + clase sin el setter, confiar en el parámetro URL
    }
    return xaWrapper.wrapDataSource(mysqlXa);
  }

  @Bean(name = "paymentsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean paymentsEntityManagerFactory(
      @Qualifier("paymentsDataSource") DataSource ds) {
    var vendor = new HibernateJpaVendorAdapter();
    var emf = new LocalContainerEntityManagerFactoryBean();
    emf.setJtaDataSource(ds);
    emf.setPackagesToScan("com.taller5.payments.model");
    emf.setPersistenceUnitName("paymentsPU");
    emf.setJpaVendorAdapter(vendor);

    Map<String,Object> props = new HashMap<>();
    props.put("hibernate.hbm2ddl.auto","none");
    props.put("jakarta.persistence.transactionType","JTA");
    emf.setJpaPropertyMap(props);
    return emf;
  }

  @Configuration
  @EnableJpaRepositories(
      basePackages = "com.taller5.payments.repository",
      entityManagerFactoryRef = "paymentsEntityManagerFactory",
      transactionManagerRef = "transactionManager"
  )
  static class PaymentsJpaConfig {}

  // -------------------- BILLING --------------------
  @Bean
  @ConfigurationProperties("datasources.billing")
  public DataSourceProperties billingDataSourceProperties() { return new DataSourceProperties(); }

  @Bean(name = "billingDataSource")
  public DataSource billingDataSource(@Qualifier("billingDataSourceProperties") DataSourceProperties props,
                                     XADataSourceWrapper xaWrapper) throws Exception {
    MysqlXADataSource mysqlXa = new MysqlXADataSource();
    String url = props.getUrl();
    // Añadir parámetros específicos para timeouts y XA
    if (!url.contains("innodb_lock_wait_timeout")) {
      url += (url.contains("?") ? "&" : "?") + "innodb_lock_wait_timeout=30";
    }
    if (!url.contains("autoReconnect")) {
      url += "&autoReconnect=true";
    }
    if (!url.contains("pinGlobalTxToPhysicalConnection")) {
      url += "&pinGlobalTxToPhysicalConnection=true";
    }
    
    mysqlXa.setUrl(url);
    mysqlXa.setUser(props.getUsername());
    mysqlXa.setPassword(props.getPassword());
    try {
    // Este setter existe en Connector/J XA datasource
    mysqlXa.setPinGlobalTxToPhysicalConnection(true);
    } catch (Throwable t) {
        // en caso raro de driver + clase sin el setter, confiar en el parámetro URL
    }
    return xaWrapper.wrapDataSource(mysqlXa);
  }

  @Bean(name = "billingEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean billingEntityManagerFactory(
      @Qualifier("billingDataSource") DataSource ds) {
    var vendor = new HibernateJpaVendorAdapter();
    var emf = new LocalContainerEntityManagerFactoryBean();
    emf.setJtaDataSource(ds);
    emf.setPackagesToScan("com.taller5.billing.model");
    emf.setPersistenceUnitName("billingPU");
    emf.setJpaVendorAdapter(vendor);

    Map<String,Object> props = new HashMap<>();
    props.put("hibernate.hbm2ddl.auto","none");
    props.put("jakarta.persistence.transactionType","JTA");
    emf.setJpaPropertyMap(props);
    return emf;
  }

  @Configuration
  @EnableJpaRepositories(
      basePackages = "com.taller5.billing.repository",
      entityManagerFactoryRef = "billingEntityManagerFactory",
      transactionManagerRef = "transactionManager"
  )
  static class BillingJpaConfig {}
}
