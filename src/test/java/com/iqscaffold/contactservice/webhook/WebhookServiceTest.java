package com.iqscaffold.contactservice.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.iqscaffold.contactservice.contact.Contact;
import com.iqscaffold.contactservice.contact.ContactStatus;
import com.iqscaffold.contactservice.tenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

  @Mock
  private WebhookRepository webhookRepository;

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private WebClient.RequestBodySpec requestBodySpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @Captor
  private ArgumentCaptor<Webhook> webhookCaptor;

  private WebhookService webhookService;

  @BeforeEach
  void setUp() {
    webhookService = new WebhookService(webhookRepository, webClientBuilder);
    TenantContext.setCurrentTenant("test-tenant");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @DisplayName("Should create webhook successfully")
  void shouldCreateWebhook() {
    // Arrange
    var webhook = createTestWebhook();
    when(webhookRepository.save(any(Webhook.class))).thenReturn(webhook);

    // Act
    var result = webhookService.createWebhook(webhook);

    // Assert
    assertThat(result).isNotNull();
    verify(webhookRepository).save(webhook);
  }

  @Test
  @DisplayName("Should get webhook by id")
  void shouldGetWebhookById() {
    // Arrange
    var webhook = createTestWebhook();
    when(webhookRepository.findById(1L)).thenReturn(Optional.of(webhook));

    // Act
    var result = webhookService.getWebhookById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(webhookRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when webhook not found")
  void shouldThrowExceptionWhenWebhookNotFound() {
    // Arrange
    when(webhookRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> webhookService.getWebhookById(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Webhook not found");
  }

  @Test
  @DisplayName("Should get all webhooks")
  void shouldGetAllWebhooks() {
    // Arrange
    var webhooks = List.of(createTestWebhook());
    when(webhookRepository.findAll()).thenReturn(webhooks);

    // Act
    var result = webhookService.getAllWebhooks();

    // Assert
    assertThat(result).hasSize(1);
    verify(webhookRepository).findAll();
  }

  @Test
  @DisplayName("Should update webhook successfully")
  void shouldUpdateWebhook() {
    // Arrange
    var existingWebhook = createTestWebhook();
    var updatedWebhook = createTestWebhook();
    updatedWebhook.setName("Updated Name");

    when(webhookRepository.findById(1L)).thenReturn(Optional.of(existingWebhook));
    when(webhookRepository.save(any(Webhook.class))).thenReturn(existingWebhook);

    // Act
    var result = webhookService.updateWebhook(1L, updatedWebhook);

    // Assert
    assertThat(result).isNotNull();
    verify(webhookRepository).findById(1L);
    verify(webhookRepository).save(webhookCaptor.capture());
    assertThat(webhookCaptor.getValue().getName()).isEqualTo("Updated Name");
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent webhook")
  void shouldThrowExceptionWhenUpdatingNonExistentWebhook() {
    // Arrange
    var webhook = createTestWebhook();
    when(webhookRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> webhookService.updateWebhook(999L, webhook))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Webhook not found");
  }

  @Test
  @DisplayName("Should delete webhook successfully")
  void shouldDeleteWebhook() {
    // Act
    webhookService.deleteWebhook(1L);

    // Assert
    verify(webhookRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should not trigger webhooks when no active webhooks found")
  void shouldNotTriggerWebhooksWhenNoActiveWebhooksFound() {
    // Arrange
    var contact = createTestContact();
    when(webhookRepository.findActiveWebhooksByEvent("contact.created"))
        .thenReturn(Collections.emptyList());

    // Act
    webhookService.triggerWebhooks("contact.created", contact);

    // Assert
    verify(webhookRepository).findActiveWebhooksByEvent("contact.created");
    verify(webClientBuilder, never()).baseUrl(anyString());
  }

  private Webhook createTestWebhook() {
    var webhook = new Webhook();
    webhook.setId(1L);
    webhook.setName("Test Webhook");
    webhook.setUrl("https://example.com/webhook");
    webhook.setSecret("test-secret");
    webhook.setEvents("contact.created,contact.updated");
    webhook.setActive(true);
    webhook.setRetryCount(3);
    webhook.setTimeoutSeconds(30);
    webhook.setDescription("Test webhook");
    webhook.setCreatedBy("test-user");
    webhook.setUpdatedBy("test-user");
    webhook.setCreatedAt(LocalDateTime.now());
    webhook.setUpdatedAt(LocalDateTime.now());
    return webhook;
  }

  private Contact createTestContact() {
    var contact = new Contact();
    contact.setId(1L);
    contact.setFirstName("John");
    contact.setLastName("Doe");
    contact.setEmail("john.doe@example.com");
    contact.setPhone("+1234567890");
    contact.setJobTitle("Developer");
    contact.setCompanyId(1L);
    contact.setStatus(ContactStatus.ACTIVE);
    contact.setLeadScore(75);
    contact.setCreatedBy("test-user");
    contact.setCreatedAt(LocalDateTime.now());
    contact.setUpdatedAt(LocalDateTime.now());
    return contact;
  }
}
