package com.iqscaffold.contactservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Test configuration for WebClient and Jackson.
 * Provides WebClient.Builder and Jackson2ObjectMapperBuilder beans for integration tests.
 */
@TestConfiguration
public class TestWebClientConfiguration {

  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder();
  }
}
