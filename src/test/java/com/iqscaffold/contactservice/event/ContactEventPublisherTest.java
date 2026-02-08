package com.iqscaffold.contactservice.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import com.iqscaffold.contactservice.config.RabbitMQConfig;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class ContactEventPublisherTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @Captor
  private ArgumentCaptor<ContactEvent> eventCaptor;

  private ContactEventPublisher eventPublisher;

  @BeforeEach
  void setUp() {
    eventPublisher = new ContactEventPublisher(rabbitTemplate);
    TenantContext.setCurrentTenant("test-tenant");
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @DisplayName("Should publish contact created event")
  void shouldPublishContactCreatedEvent() {
    // Arrange
    var contact = createTestContact();

    // Act
    eventPublisher.publishContactCreated(contact);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_CREATED_ROUTING_KEY),
        eventCaptor.capture()
    );

    var event = eventCaptor.getValue();
    assert event.getEventType().equals("CONTACT_CREATED");
    assert event.getContactId().equals(1L);
    assert event.getTenantId().equals("test-tenant");
    assert event.getMetadata().containsKey("firstName");
    assert event.getMetadata().containsKey("email");
  }

  @Test
  @DisplayName("Should publish contact updated event")
  void shouldPublishContactUpdatedEvent() {
    // Arrange
    var contact = createTestContact();

    // Act
    eventPublisher.publishContactUpdated(contact);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_UPDATED_ROUTING_KEY),
        eventCaptor.capture()
    );

    var event = eventCaptor.getValue();
    assert event.getEventType().equals("CONTACT_UPDATED");
    assert event.getContactId().equals(1L);
    assert event.getTenantId().equals("test-tenant");
    assert event.getMetadata().containsKey("updatedBy");
  }

  @Test
  @DisplayName("Should publish contact deleted event")
  void shouldPublishContactDeletedEvent() {
    // Act
    eventPublisher.publishContactDeleted(1L, "john.doe@example.com");

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_DELETED_ROUTING_KEY),
        eventCaptor.capture()
    );

    var event = eventCaptor.getValue();
    assert event.getEventType().equals("CONTACT_DELETED");
    assert event.getContactId().equals(1L);
    assert event.getTenantId().equals("test-tenant");
    assert event.getMetadata().containsKey("email");
    assert event.getMetadata().get("email").equals("john.doe@example.com");
  }

  @Test
  @DisplayName("Should handle exception when publishing contact created event")
  void shouldHandleExceptionWhenPublishingContactCreatedEvent() {
    // Arrange
    var contact = createTestContact();
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate)
        .convertAndSend(any(String.class), any(String.class), any(ContactEvent.class));

    // Act - should not throw exception
    eventPublisher.publishContactCreated(contact);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_CREATED_ROUTING_KEY),
        any(ContactEvent.class)
    );
  }

  @Test
  @DisplayName("Should handle exception when publishing contact updated event")
  void shouldHandleExceptionWhenPublishingContactUpdatedEvent() {
    // Arrange
    var contact = createTestContact();
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate)
        .convertAndSend(any(String.class), any(String.class), any(ContactEvent.class));

    // Act - should not throw exception
    eventPublisher.publishContactUpdated(contact);

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_UPDATED_ROUTING_KEY),
        any(ContactEvent.class)
    );
  }

  @Test
  @DisplayName("Should handle exception when publishing contact deleted event")
  void shouldHandleExceptionWhenPublishingContactDeletedEvent() {
    // Arrange
    doThrow(new RuntimeException("RabbitMQ error"))
        .when(rabbitTemplate)
        .convertAndSend(any(String.class), any(String.class), any(ContactEvent.class));

    // Act - should not throw exception
    eventPublisher.publishContactDeleted(1L, "john.doe@example.com");

    // Assert
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.CONTACT_DELETED_ROUTING_KEY),
        any(ContactEvent.class)
    );
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
    contact.setUpdatedBy("test-user");
    contact.setCreatedAt(LocalDateTime.now());
    contact.setUpdatedAt(LocalDateTime.now());
    return contact;
  }
}
