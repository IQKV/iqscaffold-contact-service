package com.iqscaffold.contactservice.note;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ContactNote Entity Tests")
class ContactNoteTest {

  @Test
  @DisplayName("Should create note with no-arg constructor")
  void shouldCreateNoteWithNoArgConstructor() {
    // Act
    ContactNote note = new ContactNote();

    // Assert
    assertThat(note).isNotNull();
    assertThat(note.getId()).isNull();
    assertThat(note.getContactId()).isNull();
    assertThat(note.getContent()).isNull();
    assertThat(note.getCreatedAt()).isNull();
    assertThat(note.getUpdatedAt()).isNull();
    assertThat(note.getCreatedByUserId()).isNull();
    assertThat(note.getCreatedByName()).isNull();
  }

  @Test
  @DisplayName("Should create note with all-args constructor")
  void shouldCreateNoteWithAllArgsConstructor() {
    // Arrange
    Long contactId = 1L;
    String content = "This is a test note";
    String createdByUserId = "user123";
    String createdByName = "John Doe";

    // Act
    ContactNote note = new ContactNote(contactId, content, createdByUserId, createdByName);

    // Assert
    assertThat(note).isNotNull();
    assertThat(note.getContactId()).isEqualTo(contactId);
    assertThat(note.getContent()).isEqualTo(content);
    assertThat(note.getCreatedByUserId()).isEqualTo(createdByUserId);
    assertThat(note.getCreatedByName()).isEqualTo(createdByName);
  }

  @Test
  @DisplayName("Should set and get id")
  void shouldSetAndGetId() {
    // Arrange
    ContactNote note = new ContactNote();
    Long id = 100L;

    // Act
    note.setId(id);

    // Assert
    assertThat(note.getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Should set and get contactId")
  void shouldSetAndGetContactId() {
    // Arrange
    ContactNote note = new ContactNote();
    Long contactId = 1L;

    // Act
    note.setContactId(contactId);

    // Assert
    assertThat(note.getContactId()).isEqualTo(contactId);
  }

  @Test
  @DisplayName("Should set and get content")
  void shouldSetAndGetContent() {
    // Arrange
    ContactNote note = new ContactNote();
    String content = "Updated note content";

    // Act
    note.setContent(content);

    // Assert
    assertThat(note.getContent()).isEqualTo(content);
  }

  @Test
  @DisplayName("Should set and get createdAt")
  void shouldSetAndGetCreatedAt() {
    // Arrange
    ContactNote note = new ContactNote();
    LocalDateTime createdAt = LocalDateTime.now();

    // Act
    note.setCreatedAt(createdAt);

    // Assert
    assertThat(note.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("Should set and get updatedAt")
  void shouldSetAndGetUpdatedAt() {
    // Arrange
    ContactNote note = new ContactNote();
    LocalDateTime updatedAt = LocalDateTime.now();

    // Act
    note.setUpdatedAt(updatedAt);

    // Assert
    assertThat(note.getUpdatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("Should set and get createdByUserId")
  void shouldSetAndGetCreatedByUserId() {
    // Arrange
    ContactNote note = new ContactNote();
    String userId = "user456";

    // Act
    note.setCreatedByUserId(userId);

    // Assert
    assertThat(note.getCreatedByUserId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("Should set and get createdByName")
  void shouldSetAndGetCreatedByName() {
    // Arrange
    ContactNote note = new ContactNote();
    String userName = "Jane Smith";

    // Act
    note.setCreatedByName(userName);

    // Assert
    assertThat(note.getCreatedByName()).isEqualTo(userName);
  }

  @Test
  @DisplayName("Should handle long content")
  void shouldHandleLongContent() {
    // Arrange
    ContactNote note = new ContactNote();
    String longContent = "A".repeat(5000); // Max length

    // Act
    note.setContent(longContent);

    // Assert
    assertThat(note.getContent()).isEqualTo(longContent);
    assertThat(note.getContent()).hasSize(5000);
  }

  @Test
  @DisplayName("Should update content after creation")
  void shouldUpdateContentAfterCreation() {
    // Arrange
    ContactNote note = new ContactNote(1L, "Original content", "user1", "User One");
    String newContent = "Updated content";

    // Act
    note.setContent(newContent);

    // Assert
    assertThat(note.getContent()).isEqualTo(newContent);
    assertThat(note.getContactId()).isEqualTo(1L);
    assertThat(note.getCreatedByUserId()).isEqualTo("user1");
  }

  @Test
  @DisplayName("Should maintain immutable contactId after creation")
  void shouldMaintainImmutableContactIdAfterCreation() {
    // Arrange
    Long originalContactId = 1L;
    ContactNote note = new ContactNote(originalContactId, "Content", "user1", "User One");
    Long newContactId = 2L;

    // Act
    note.setContactId(newContactId);

    // Assert
    assertThat(note.getContactId()).isEqualTo(newContactId);
  }
}
