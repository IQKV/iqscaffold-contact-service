package com.iqscaffold.contactservice.activity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ContactActivity Entity Tests")
class ContactActivityTest {

  @Test
  @DisplayName("Should create activity with no-arg constructor")
  void shouldCreateActivityWithNoArgConstructor() {
    // Act
    ContactActivity activity = new ContactActivity();

    // Assert
    assertThat(activity).isNotNull();
    assertThat(activity.getId()).isNull();
    assertThat(activity.getContactId()).isNull();
    assertThat(activity.getActivityType()).isNull();
    assertThat(activity.getDescription()).isNull();
    assertThat(activity.getTimestamp()).isNull();
    assertThat(activity.getPerformedByUserId()).isNull();
    assertThat(activity.getPerformedByName()).isNull();
    assertThat(activity.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should create activity with all-args constructor")
  void shouldCreateActivityWithAllArgsConstructor() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType activityType = ContactActivityType.NOTE_ADDED;
    String description = "Note added to contact";
    LocalDateTime timestamp = LocalDateTime.now();
    String performedByUserId = "user123";
    String performedByName = "John Doe";
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", 10L);

    // Act
    ContactActivity activity = new ContactActivity(
        contactId,
        activityType,
        description,
        timestamp,
        performedByUserId,
        performedByName,
        metadata
    );

    // Assert
    assertThat(activity).isNotNull();
    assertThat(activity.getContactId()).isEqualTo(contactId);
    assertThat(activity.getActivityType()).isEqualTo(activityType);
    assertThat(activity.getDescription()).isEqualTo(description);
    assertThat(activity.getTimestamp()).isEqualTo(timestamp);
    assertThat(activity.getPerformedByUserId()).isEqualTo(performedByUserId);
    assertThat(activity.getPerformedByName()).isEqualTo(performedByName);
    assertThat(activity.getMetadata()).isEqualTo(metadata);
  }

  @Test
  @DisplayName("Should set and get id")
  void shouldSetAndGetId() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    Long id = 100L;

    // Act
    activity.setId(id);

    // Assert
    assertThat(activity.getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Should set and get contactId")
  void shouldSetAndGetContactId() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    Long contactId = 1L;

    // Act
    activity.setContactId(contactId);

    // Assert
    assertThat(activity.getContactId()).isEqualTo(contactId);
  }

  @Test
  @DisplayName("Should set and get activityType")
  void shouldSetAndGetActivityType() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    ContactActivityType activityType = ContactActivityType.STATUS_CHANGED;

    // Act
    activity.setActivityType(activityType);

    // Assert
    assertThat(activity.getActivityType()).isEqualTo(activityType);
  }

  @Test
  @DisplayName("Should set and get description")
  void shouldSetAndGetDescription() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    String description = "Status changed from NEW to ACTIVE";

    // Act
    activity.setDescription(description);

    // Assert
    assertThat(activity.getDescription()).isEqualTo(description);
  }

  @Test
  @DisplayName("Should set and get timestamp")
  void shouldSetAndGetTimestamp() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    LocalDateTime timestamp = LocalDateTime.now();

    // Act
    activity.setTimestamp(timestamp);

    // Assert
    assertThat(activity.getTimestamp()).isEqualTo(timestamp);
  }

  @Test
  @DisplayName("Should set and get performedByUserId")
  void shouldSetAndGetPerformedByUserId() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    String userId = "user456";

    // Act
    activity.setPerformedByUserId(userId);

    // Assert
    assertThat(activity.getPerformedByUserId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("Should set and get performedByName")
  void shouldSetAndGetPerformedByName() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    String userName = "Jane Smith";

    // Act
    activity.setPerformedByName(userName);

    // Assert
    assertThat(activity.getPerformedByName()).isEqualTo(userName);
  }

  @Test
  @DisplayName("Should set and get metadata")
  void shouldSetAndGetMetadata() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("key1", "value1");
    metadata.put("key2", 123);

    // Act
    activity.setMetadata(metadata);

    // Assert
    assertThat(activity.getMetadata()).isEqualTo(metadata);
    assertThat(activity.getMetadata()).containsEntry("key1", "value1");
    assertThat(activity.getMetadata()).containsEntry("key2", 123);
  }

  @Test
  @DisplayName("Should handle null metadata")
  void shouldHandleNullMetadata() {
    // Arrange
    ContactActivity activity = new ContactActivity();

    // Act
    activity.setMetadata(null);

    // Assert
    assertThat(activity.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should create activity with null metadata in constructor")
  void shouldCreateActivityWithNullMetadataInConstructor() {
    // Arrange
    Long contactId = 1L;
    ContactActivityType activityType = ContactActivityType.CONTACT_CREATED;
    String description = "Contact created";
    LocalDateTime timestamp = LocalDateTime.now();
    String performedByUserId = "user123";
    String performedByName = "John Doe";

    // Act
    ContactActivity activity = new ContactActivity(
        contactId,
        activityType,
        description,
        timestamp,
        performedByUserId,
        performedByName,
        null
    );

    // Assert
    assertThat(activity).isNotNull();
    assertThat(activity.getMetadata()).isNull();
  }

  @Test
  @DisplayName("Should support all activity types")
  void shouldSupportAllActivityTypes() {
    // Arrange
    ContactActivity activity = new ContactActivity();
    ContactActivityType[] allTypes = ContactActivityType.values();

    // Act & Assert
    for (ContactActivityType type : allTypes) {
      activity.setActivityType(type);
      assertThat(activity.getActivityType()).isEqualTo(type);
    }
  }
}
