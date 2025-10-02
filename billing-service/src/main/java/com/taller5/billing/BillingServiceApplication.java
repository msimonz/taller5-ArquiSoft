package com.taller5.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.taller5.billing.client")
public class BillingServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(BillingServiceApplication.class, args);
  }
}
