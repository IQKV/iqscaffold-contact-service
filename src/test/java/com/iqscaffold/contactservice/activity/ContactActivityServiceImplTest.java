package com.iqscaffold.contactservice.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.iqscaffold.contactservice.activity.dto.ContactActivityDtos;
import com.iqscaffold.contactservice.contact.Contact;
import com.iqscaffold.contactservice.contact.ContactService;
import com.iqscaffold.contactservice.contact.ContactStatus;
import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactActivityServiceImpl Unit Tests")
class ContactActivityServiceImplTest {

  @Mock
  private ContactActivityRepository activityRepository;

  @Mock
  private ContactService contactService;

  private ContactActivityServiceImpl activityService;

  @BeforeEach
  void setUp() {
    activityService = new ContactActivityServiceImpl(
        activityRepository,
        contactService
    );
  }

  @Test
  @DisplayName("Should log activity successfully")
  void shouldLogActivity() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType activityType = ContactActivityType.NOTE_ADDED;
    String description = "Note added to contact";
    String performedByUserId = "user123";
    String performedByName = "John Doe";
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", 10L);
    metadata.put("contentLength", 100);

    ArgumentCaptor<ContactActivity> activityCaptor = ArgumentCaptor.forClass(ContactActivity.class);

    // Act
    activityService.logActivity(contactId, activityType, description, performedByUserId, performedByName, metadata);

    // Assert
    verify(activityRepository).save(activityCaptor.capture());
    ContactActivity savedActivity = activityCaptor.getValue();

    assertThat(savedActivity).isNotNull();
    assertThat(savedActivity.getContactId()).isEqualTo(contactId);
    assertThat(savedActivity.getActivityType()).isEqualTo(activityType);
    assertThat(savedActivity.getDescription()).isEqualTo(description);
    assertThat(savedActivity.getPerformedByUserId()).isEqualTo(performedByUserId);
    assertThat(savedActivity.getPerformedByName()).isEqualTo(performedByName);
    assertThat(savedActivity.getMetadata()).isEqualTo(metadata);
    assertThat(savedActivity.getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("Should log activity with null metadata")
  void shouldLogActivityWithNullMetadata() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType activityType = ContactActivityType.CONTACT_CREATED;
    String description = "Contact created";
    String performedByUserId = "user123";
    String performedByName = "John Doe";

    ArgumentCaptor<ContactActivity> activityCaptor = ArgumentCaptor.forClass(ContactActivity.class);

    // Act
    activityService.logActivity(contactId, activityType, description, performedByUserId, performedByName, null);

    // Assert
    verify(activityRepository).save(activityCaptor.capture());
    ContactActivity savedActivity = activityCaptor.getValue();

    assertThat(savedActivity).isNotNull();
    assertThat(savedActivity.getContactId()).isEqualTo(contactId);
    assertThat(savedActivity.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should log activity with empty metadata")
  void shouldLogActivityWithEmptyMetadata() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType activityType = ContactActivityType.STATUS_CHANGED;
    String description = "Status changed";
    String performedByUserId = "user123";
    String performedByName = "John Doe";
    Map<String, Object> metadata = new HashMap<>();

    ArgumentCaptor<ContactActivity> activityCaptor = ArgumentCaptor.forClass(ContactActivity.class);

    // Act
    activityService.logActivity(contactId, activityType, description, performedByUserId, performedByName, metadata);

    // Assert
    verify(activityRepository).save(activityCaptor.capture());
    ContactActivity savedActivity = activityCaptor.getValue();

    assertThat(savedActivity).isNotNull();
    assertThat(savedActivity.getMetadata()).isEmpty();
  }

  @Test
  @DisplayName("Should get contact activities successfully")
  void shouldGetContactActivities() {
    // Arrange
    Long contactId = 1L;
    Contact contact = createTestContact(contactId);

    ContactActivity activity1 = new ContactActivity(
        contactId,
        ContactActivityType.CONTACT_CREATED,
        "Contact created",
        LocalDateTime.now().minusDays(2),
        "user1",
        "User One",
        null
    );
    activity1.setId(1L);

    ContactActivity activity2 = new ContactActivity(
        contactId,
        ContactActivityType.NOTE_ADDED,
        "Note added",
        LocalDateTime.now().minusDays(1),
        "user2",
        "User Two",
        Map.of("noteId", 10L)
    );
    activity2.setId(2L);

    ContactActivity activity3 = new ContactActivity(
        contactId,
        ContactActivityType.STATUS_CHANGED,
        "Status changed to ACTIVE",
        LocalDateTime.now(),
        "user1",
        "User One",
        Map.of("oldStatus", "NEW", "newStatus", "ACTIVE")
    );
    activity3.setId(3L);

    List<ContactActivity> activities = List.of(activity3, activity2, activity1); // Descending order

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(activityRepository.findByContactIdOrderByTimestampDesc(contactId)).thenReturn(activities);

    // Act
    List<ContactActivityDtos.ContactActivityResponse> result = activityService.getContactActivities(contactId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);
    assertThat(result.get(0).activityType()).isEqualTo(ContactActivityType.STATUS_CHANGED);
    assertThat(result.get(1).activityType()).isEqualTo(ContactActivityType.NOTE_ADDED);
    assertThat(result.get(2).activityType()).isEqualTo(ContactActivityType.CONTACT_CREATED);
    assertThat(result.get(0).description()).isEqualTo("Status changed to ACTIVE");
    assertThat(result.get(1).metadata()).containsEntry("noteId", 10L);
    verify(contactService).getContactById(contactId);
    verify(activityRepository).findByContactIdOrderByTimestampDesc(contactId);
  }

  @Test
  @DisplayName("Should return empty list when contact has no activities")
  void shouldReturnEmptyListWhenContactHasNoActivities() {
    // Arrange
    Long contactId = 1L;
    Contact contact = createTestContact(contactId);

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(activityRepository.findByContactIdOrderByTimestampDesc(contactId)).thenReturn(List.of());

    // Act
    List<ContactActivityDtos.ContactActivityResponse> result = activityService.getContactActivities(contactId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(contactService).getContactById(contactId);
    verify(activityRepository).findByContactIdOrderByTimestampDesc(contactId);
  }

  @Test
  @DisplayName("Should throw exception when getting activities for non-existent contact")
  void shouldThrowExceptionWhenGettingActivitiesForNonExistentContact() {
    // Arrange
    Long contactId = 999L;
    when(contactService.getContactById(contactId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> activityService.getContactActivities(contactId))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(activityRepository, org.mockito.Mockito.never()).findByContactIdOrderByTimestampDesc(any());
  }

  @Test
  @DisplayName("Should log different activity types")
  void shouldLogDifferentActivityTypes() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType[] activityTypes = {
        ContactActivityType.CONTACT_CREATED,
        ContactActivityType.CONTACT_UPDATED,
        ContactActivityType.STATUS_CHANGED,
        ContactActivityType.NOTE_ADDED,
        ContactActivityType.NOTE_UPDATED,
        ContactActivityType.NOTE_DELETED,
        ContactActivityType.LEAD_SCORE_UPDATED,
        ContactActivityType.COMPANY_ASSOCIATED,
        ContactActivityType.EMAIL_SENT,
        ContactActivityType.CALL_MADE,
        ContactActivityType.MEETING_SCHEDULED,
        ContactActivityType.CONVERTED_FROM_LEAD
    };

    // Act & Assert
    for (ContactActivityType activityType : activityTypes) {
      activityService.logActivity(
          contactId,
          activityType,
          "Test activity: " + activityType,
          "user123",
          "Test User",
          null
      );
    }

    verify(activityRepository, org.mockito.Mockito.times(activityTypes.length)).save(any(ContactActivity.class));
  }

  @Test
  @DisplayName("Should preserve metadata when logging activity")
  void shouldPreserveMetadataWhenLoggingActivity() {
    // Arrange
    Long contactId = 1L;
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", 10L);
    metadata.put("contentLength", 500);
    metadata.put("oldValue", "OLD");
    metadata.put("newValue", "NEW");
    metadata.put("timestamp", LocalDateTime.now().toString());

    ArgumentCaptor<ContactActivity> activityCaptor = ArgumentCaptor.forClass(ContactActivity.class);

    // Act
    activityService.logActivity(
        contactId,
        ContactActivityType.NOTE_UPDATED,
        "Note updated with metadata",
        "user123",
        "Test User",
        metadata
    );

    // Assert
    verify(activityRepository).save(activityCaptor.capture());
    ContactActivity savedActivity = activityCaptor.getValue();

    assertThat(savedActivity.getMetadata()).isNotNull();
    assertThat(savedActivity.getMetadata()).hasSize(5);
    assertThat(savedActivity.getMetadata()).containsEntry("noteId", 10L);
    assertThat(savedActivity.getMetadata()).containsEntry("contentLength", 500);
    assertThat(savedActivity.getMetadata()).containsEntry("oldValue", "OLD");
    assertThat(savedActivity.getMetadata()).containsEntry("newValue", "NEW");
    assertThat(savedActivity.getMetadata()).containsKey("timestamp");
  }

  private Contact createTestContact(Long id) {
    Contact contact = new Contact();
    contact.setId(id);
    contact.setFirstName("John");
    contact.setLastName("Doe");
    contact.setEmail("john.doe@example.com");
    contact.setPhone("+1234567890");
    contact.setJobTitle("Developer");
    contact.setStatus(ContactStatus.ACTIVE);
    contact.setLeadScore(75);
    contact.setCreatedBy("system");
    contact.setUpdatedBy("system");
    contact.setCreatedAt(LocalDateTime.now());
    contact.setUpdatedAt(LocalDateTime.now());
    return contact;
  }
}
