package com.iqscaffold.contactservice.config;

import javax.sql.DataSource;

import com.iqscaffold.contactservice.tenancy.SchemaNameResolver;
import com.iqscaffold.contactservice.tenancy.TenantLiquibaseRunner;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test configuration for multi-tenancy support in tests.
 * Disables multi-tenancy for integration tests to simplify test setup.
 */
@TestConfiguration
@Profile("test")
public class TestTenantConfiguration {

  @Bean
  public SchemaNameResolver schemaNameResolver() {
    return new SchemaNameResolver("", "PUBLIC");
  }

  /**
   * Disable multi-tenancy for tests by overriding the Hibernate properties.
   * This allows tests to run without tenant schema configuration.
   */
  @Bean
  @Primary
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
    return hibernateProperties -> {
      // Explicitly disable multi-tenancy for tests
      hibernateProperties.put("hibernate.multiTenancy", "NONE");
    };
  }

  @Bean
  public TenantLiquibaseRunner tenantLiquibaseRunner() {
    return org.mockito.Mockito.mock(TenantLiquibaseRunner.class);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
