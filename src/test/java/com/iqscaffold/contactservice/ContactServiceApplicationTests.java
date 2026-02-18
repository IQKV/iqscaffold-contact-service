package com.iqscaffold.contactservice;

import com.iqscaffold.contactservice.config.TestWebClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebClientConfiguration.class)
class ContactServiceApplicationTests {

  @Test
  void contextLoads() {
    // This test verifies that the Spring application context loads successfully
  }
}