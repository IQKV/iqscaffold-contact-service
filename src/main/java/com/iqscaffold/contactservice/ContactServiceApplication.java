package com.iqscaffold.contactservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.DependsOn;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.iqscaffold.contactservice.config")
@DependsOn("systemLiquibaseInitializer")
public class ContactServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ContactServiceApplication.class, args);
  }
}