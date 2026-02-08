package com.iqscaffold.contactservice.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.iqscaffold.contactservice.event.ContactEventPublisher;
import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import com.iqscaffold.contactservice.webhook.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

  @Mock
  private ContactRepository contactRepository;

  @Mock
  private ContactEventPublisher eventPublisher;

  @Mock
  private WebhookService webhookService;

  @Captor
  private ArgumentCaptor<Contact> contactCaptor;

  private ContactServiceImpl contactService;

  @BeforeEach
  void setUp() {
    contactService = new ContactServiceImpl(contactRepository, eventPublisher, webhookService);
  }

  @Test
  @DisplayName("Should create contact successfully")
  void shouldCreateContact() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.save(any(Contact.class))).thenReturn(contact);

    // Act
    var result = contactService.createContact(contact);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    verify(contactRepository).save(contact);
    verify(eventPublisher).publishContactCreated(contact);
    verify(webhookService).triggerWebhooks("contact.created", contact);
  }

  @Test
  @DisplayName("Should get contact by id")
  void shouldGetContactById() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    // Act
    var result = contactService.getContactById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
    verify(contactRepository).findById(1L);
  }

  @Test
  @DisplayName("Should get contact by email")
  void shouldGetContactByEmail() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(contact));

    // Act
    var result = contactService.getContactByEmail("john.doe@example.com");

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    verify(contactRepository).findByEmail("john.doe@example.com");
  }

  @Test
  @DisplayName("Should get all contacts with pagination")
  void shouldGetAllContactsWithPagination() {
    // Arrange
    var contacts = List.of(createTestContact());
    var page = new PageImpl<>(contacts);
    var pageable = PageRequest.of(0, 10);
    when(contactRepository.findAll(pageable)).thenReturn(page);

    // Act
    Page<Contact> result = contactService.getAllContacts(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(contactRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should get contacts by status")
  void shouldGetContactsByStatus() {
    // Arrange
    var contacts = List.of(createTestContact());
    var page = new PageImpl<>(contacts);
    var pageable = PageRequest.of(0, 10);
    when(contactRepository.findByStatus(ContactStatus.ACTIVE, pageable)).thenReturn(page);

    // Act
    Page<Contact> result = contactService.getContactsByStatus(ContactStatus.ACTIVE, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(contactRepository).findByStatus(ContactStatus.ACTIVE, pageable);
  }

  @Test
  @DisplayName("Should search contacts")
  void shouldSearchContacts() {
    // Arrange
    var contacts = List.of(createTestContact());
    var page = new PageImpl<>(contacts);
    var pageable = PageRequest.of(0, 10);
    when(contactRepository.searchContacts("john", pageable)).thenReturn(page);

    // Act
    Page<Contact> result = contactService.searchContacts("john", pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(contactRepository).searchContacts("john", pageable);
  }

  @Test
  @DisplayName("Should get contacts by company")
  void shouldGetContactsByCompany() {
    // Arrange
    var contacts = List.of(createTestContact());
    when(contactRepository.findByCompanyId(1L)).thenReturn(contacts);

    // Act
    var result = contactService.getContactsByCompany(1L);

    // Assert
    assertThat(result).hasSize(1);
    verify(contactRepository).findByCompanyId(1L);
  }

  @Test
  @DisplayName("Should update contact successfully")
  void shouldUpdateContact() {
    // Arrange
    var existingContact = createTestContact();
    var updatedContact = createTestContact();
    updatedContact.setFirstName("Jane");

    when(contactRepository.findById(1L)).thenReturn(Optional.of(existingContact));
    when(contactRepository.save(any(Contact.class))).thenReturn(existingContact);

    // Act
    var result = contactService.updateContact(1L, updatedContact);

    // Assert
    assertThat(result).isNotNull();
    verify(contactRepository).findById(1L);
    verify(contactRepository).save(contactCaptor.capture());
    assertThat(contactCaptor.getValue().getFirstName()).isEqualTo("Jane");
    verify(eventPublisher).publishContactUpdated(any(Contact.class));
    verify(webhookService).triggerWebhooks(anyString(), any(Contact.class));
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent contact")
  void shouldThrowExceptionWhenUpdatingNonExistentContact() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactService.updateContact(999L, contact))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("Contact not found");
  }

  @Test
  @DisplayName("Should delete contact successfully")
  void shouldDeleteContact() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    // Act
    contactService.deleteContact(1L);

    // Assert
    verify(contactRepository).findById(1L);
    verify(contactRepository).deleteById(1L);
    verify(eventPublisher).publishContactDeleted(1L, "john.doe@example.com");
    verify(webhookService).triggerWebhooks("contact.deleted", contact);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent contact")
  void shouldThrowExceptionWhenDeletingNonExistentContact() {
    // Arrange
    when(contactRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactService.deleteContact(999L))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("Contact not found");
  }

  @Test
  @DisplayName("Should check if contact exists by email")
  void shouldCheckIfContactExistsByEmail() {
    // Arrange
    when(contactRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

    // Act
    var result = contactService.existsByEmail("john.doe@example.com");

    // Assert
    assertThat(result).isTrue();
    verify(contactRepository).existsByEmail("john.doe@example.com");
  }

  @Test
  @DisplayName("Should get contact count by status")
  void shouldGetContactCountByStatus() {
    // Arrange
    when(contactRepository.countByStatus(ContactStatus.ACTIVE)).thenReturn(5L);

    // Act
    var result = contactService.getContactCountByStatus(ContactStatus.ACTIVE);

    // Assert
    assertThat(result).isEqualTo(5L);
    verify(contactRepository).countByStatus(ContactStatus.ACTIVE);
  }

  @Test
  @DisplayName("Should update lead score")
  void shouldUpdateLeadScore() {
    // Arrange
    var contact = createTestContact();
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(contactRepository.save(any(Contact.class))).thenReturn(contact);

    // Act
    var result = contactService.updateLeadScore(1L, 90);

    // Assert
    assertThat(result).isNotNull();
    verify(contactRepository).findById(1L);
    verify(contactRepository).save(contactCaptor.capture());
    assertThat(contactCaptor.getValue().getLeadScore()).isEqualTo(90);
    verify(eventPublisher).publishContactUpdated(any(Contact.class));
  }

  @Test
  @DisplayName("Should bulk create contacts")
  void shouldBulkCreateContacts() {
    // Arrange
    var contact1 = createTestContact();
    var contact2 = createTestContact();
    contact2.setId(2L);
    contact2.setEmail("jane.doe@example.com");

    when(contactRepository.save(any(Contact.class)))
        .thenReturn(contact1)
        .thenReturn(contact2);

    // Act
    var result = contactService.bulkCreateContacts(List.of(contact1, contact2));

    // Assert
    assertThat(result).hasSize(2);
    verify(contactRepository, times(2)).save(any(Contact.class));
    verify(eventPublisher, times(2)).publishContactCreated(any(Contact.class));
  }

  @Test
  @DisplayName("Should bulk update status")
  void shouldBulkUpdateStatus() {
    // Arrange
    var contact1 = createTestContact();
    var contact2 = createTestContact();
    contact2.setId(2L);

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact1));
    when(contactRepository.findById(2L)).thenReturn(Optional.of(contact2));
    when(contactRepository.save(any(Contact.class)))
        .thenReturn(contact1)
        .thenReturn(contact2);

    // Act
    var result = contactService.bulkUpdateStatus(List.of(1L, 2L), ContactStatus.INACTIVE, "test-user");

    // Assert
    assertThat(result).hasSize(2);
    verify(contactRepository, times(2)).save(any(Contact.class));
    verify(eventPublisher, times(2)).publishContactUpdated(any(Contact.class));
  }

  @Test
  @DisplayName("Should bulk delete contacts")
  void shouldBulkDeleteContacts() {
    // Arrange
    var contact1 = createTestContact();
    var contact2 = createTestContact();
    contact2.setId(2L);

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact1));
    when(contactRepository.findById(2L)).thenReturn(Optional.of(contact2));

    // Act
    var result = contactService.bulkDeleteContacts(List.of(1L, 2L));

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(1L)).isTrue();
    assertThat(result.get(2L)).isTrue();
    verify(contactRepository, times(2)).deleteById(any(Long.class));
    verify(eventPublisher, times(2)).publishContactDeleted(any(Long.class), anyString());
  }

  @Test
  @DisplayName("Should bulk update lead scores")
  void shouldBulkUpdateLeadScores() {
    // Arrange
    var contact1 = createTestContact();
    var contact2 = createTestContact();
    contact2.setId(2L);

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact1));
    when(contactRepository.findById(2L)).thenReturn(Optional.of(contact2));
    when(contactRepository.save(any(Contact.class)))
        .thenReturn(contact1)
        .thenReturn(contact2);

    // Act
    var result = contactService.bulkUpdateLeadScores(Map.of(1L, 80, 2L, 90));

    // Assert
    assertThat(result).hasSize(2);
    verify(contactRepository, times(2)).save(any(Contact.class));
    verify(eventPublisher, times(2)).publishContactUpdated(any(Contact.class));
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
    contact.setNotes("Test notes");
    contact.setCreatedBy("test-user");
    contact.setUpdatedBy("test-user");
    contact.setCreatedAt(LocalDateTime.now());
    contact.setUpdatedAt(LocalDateTime.now());
    return contact;
  }
}
