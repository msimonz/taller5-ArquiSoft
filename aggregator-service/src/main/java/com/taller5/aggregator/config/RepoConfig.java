package com.taller5.aggregator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.taller5.aggregator.inventory",
    entityManagerFactoryRef = "inventoryEmf",
    transactionManagerRef = "transactionManager" // JTA
)
class InvRepoConfig {}

@Configuration
@EnableJpaRepositories(
    basePackages = "com.taller5.aggregator.billing",
    entityManagerFactoryRef = "billingEmf",
    transactionManagerRef = "transactionManager"
)
class BillRepoConfig {}

@Configuration
@EnableJpaRepositories(
    basePackages = "com.taller5.aggregator.payments",
    entityManagerFactoryRef = "paymentsEmf",
    transactionManagerRef = "transactionManager"
)
class PayRepoConfig {}
