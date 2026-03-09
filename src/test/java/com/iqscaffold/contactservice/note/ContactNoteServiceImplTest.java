package com.iqscaffold.contactservice.note;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.iqscaffold.contactservice.activity.ContactActivityService;
import com.iqscaffold.contactservice.activity.ContactActivityType;
import com.iqscaffold.contactservice.contact.Contact;
import com.iqscaffold.contactservice.contact.ContactService;
import com.iqscaffold.contactservice.contact.ContactStatus;
import com.iqscaffold.contactservice.note.dto.ContactNoteDtos;
import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactNoteServiceImpl Unit Tests")
class ContactNoteServiceImplTest {

  @Mock
  private ContactNoteRepository contactNoteRepository;

  @Mock
  private ContactService contactService;

  @Mock
  private ContactActivityService activityService;

  private ContactNoteServiceImpl contactNoteService;

  @BeforeEach
  void setUp() {
    contactNoteService = new ContactNoteServiceImpl(
        contactNoteRepository,
        contactService,
        activityService
    );
  }

  @Test
  @DisplayName("Should create note successfully")
  void shouldCreateNote() {
    // Arrange
    Long contactId = 1L;
    Contact contact = createTestContact(contactId);
    ContactNoteDtos.CreateContactNoteRequest request = new ContactNoteDtos.CreateContactNoteRequest(
        "Test note content"
    );
    ContactNote savedNote = new ContactNote(contactId, request.content(), "test-user", "Test User");
    savedNote.setId(10L);
    savedNote.setCreatedAt(LocalDateTime.now());
    savedNote.setUpdatedAt(LocalDateTime.now());

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.save(any(ContactNote.class))).thenReturn(savedNote);

    // Act
    ContactNoteDtos.ContactNoteResponse result = contactNoteService.createNote(
        contactId, request, "test-user", "Test User"
    );

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.contactId()).isEqualTo(contactId);
    assertThat(result.content()).isEqualTo("Test note content");
    assertThat(result.createdByName()).isEqualTo("Test User");
    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).save(any(ContactNote.class));
    verify(activityService).logActivity(
        eq(contactId),
        eq(ContactActivityType.NOTE_ADDED),
        anyString(),
        eq("test-user"),
        eq("Test User"),
        anyMap()
    );
  }

  @Test
  @DisplayName("Should throw exception when creating note for non-existent contact")
  void shouldThrowExceptionWhenCreatingNoteForNonExistentContact() {
    // Arrange
    Long contactId = 999L;
    ContactNoteDtos.CreateContactNoteRequest request = new ContactNoteDtos.CreateContactNoteRequest(
        "Test note content"
    );

    when(contactService.getContactById(contactId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.createNote(contactId, request, "test-user", "Test User"))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository, never()).save(any(ContactNote.class));
    verify(activityService, never()).logActivity(anyLong(), any(), anyString(), anyString(), anyString(), anyMap());
  }

  @Test
  @DisplayName("Should get notes by contact ID successfully")
  void shouldGetNotesByContactId() {
    // Arrange
    Long contactId = 1L;
    Contact contact = createTestContact(contactId);
    ContactNote note1 = new ContactNote(contactId, "First note", "user1", "User One");
    note1.setId(10L);
    note1.setCreatedAt(LocalDateTime.now().minusDays(1));
    note1.setUpdatedAt(LocalDateTime.now().minusDays(1));

    ContactNote note2 = new ContactNote(contactId, "Second note", "user2", "User Two");
    note2.setId(11L);
    note2.setCreatedAt(LocalDateTime.now());
    note2.setUpdatedAt(LocalDateTime.now());

    List<ContactNote> notes = List.of(note2, note1); // Descending order

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findByContactIdOrderByCreatedAtDesc(contactId)).thenReturn(notes);

    // Act
    List<ContactNoteDtos.ContactNoteResponse> result = contactNoteService.getNotesByContactId(contactId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0).content()).isEqualTo("Second note");
    assertThat(result.get(1).content()).isEqualTo("First note");
    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findByContactIdOrderByCreatedAtDesc(contactId);
  }

  @Test
  @DisplayName("Should return empty list when contact has no notes")
  void shouldReturnEmptyListWhenContactHasNoNotes() {
    // Arrange
    Long contactId = 1L;
    Contact contact = createTestContact(contactId);

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findByContactIdOrderByCreatedAtDesc(contactId)).thenReturn(List.of());

    // Act
    List<ContactNoteDtos.ContactNoteResponse> result = contactNoteService.getNotesByContactId(contactId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findByContactIdOrderByCreatedAtDesc(contactId);
  }

  @Test
  @DisplayName("Should throw exception when getting notes for non-existent contact")
  void shouldThrowExceptionWhenGettingNotesForNonExistentContact() {
    // Arrange
    Long contactId = 999L;
    when(contactService.getContactById(contactId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.getNotesByContactId(contactId))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository, never()).findByContactIdOrderByCreatedAtDesc(any());
  }

  @Test
  @DisplayName("Should update note successfully")
  void shouldUpdateNote() {
    // Arrange
    Long contactId = 1L;
    Long noteId = 10L;
    Contact contact = createTestContact(contactId);
    ContactNote existingNote = new ContactNote(contactId, "Original content", "user1", "User One");
    existingNote.setId(noteId);
    existingNote.setCreatedAt(LocalDateTime.now().minusDays(1));
    existingNote.setUpdatedAt(LocalDateTime.now().minusDays(1));

    ContactNoteDtos.UpdateContactNoteRequest request = new ContactNoteDtos.UpdateContactNoteRequest(
        "Updated content"
    );

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.of(existingNote));
    when(contactNoteRepository.save(any(ContactNote.class))).thenReturn(existingNote);

    // Act
    ContactNoteDtos.ContactNoteResponse result = contactNoteService.updateNote(contactId, noteId, request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo("Updated content");
    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository).save(existingNote);
    verify(activityService).logActivity(
        eq(contactId),
        eq(ContactActivityType.NOTE_UPDATED),
        anyString(),
        eq("user1"),
        eq("User One"),
        anyMap()
    );
  }

  @Test
  @DisplayName("Should throw exception when updating note for non-existent contact")
  void shouldThrowExceptionWhenUpdatingNoteForNonExistentContact() {
    // Arrange
    Long contactId = 999L;
    Long noteId = 10L;
    ContactNoteDtos.UpdateContactNoteRequest request = new ContactNoteDtos.UpdateContactNoteRequest(
        "Updated content"
    );

    when(contactService.getContactById(contactId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.updateNote(contactId, noteId, request))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository, never()).findById(any());
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent note")
  void shouldThrowExceptionWhenUpdatingNonExistentNote() {
    // Arrange
    Long contactId = 1L;
    Long noteId = 999L;
    Contact contact = createTestContact(contactId);
    ContactNoteDtos.UpdateContactNoteRequest request = new ContactNoteDtos.UpdateContactNoteRequest(
        "Updated content"
    );

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.updateNote(contactId, noteId, request))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when updating note with wrong contact ID")
  void shouldThrowExceptionWhenUpdatingNoteWithWrongContactId() {
    // Arrange
    Long contactId = 1L;
    Long wrongContactId = 2L;
    Long noteId = 10L;
    Contact contact = createTestContact(wrongContactId);
    ContactNote note = new ContactNote(contactId, "Original content", "user1", "User One");
    note.setId(noteId);

    ContactNoteDtos.UpdateContactNoteRequest request = new ContactNoteDtos.UpdateContactNoteRequest(
        "Updated content"
    );

    when(contactService.getContactById(wrongContactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.updateNote(wrongContactId, noteId, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not belong to");

    verify(contactService).getContactById(wrongContactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete note successfully")
  void shouldDeleteNote() {
    // Arrange
    Long contactId = 1L;
    Long noteId = 10L;
    Contact contact = createTestContact(contactId);
    ContactNote note = new ContactNote(contactId, "Note to delete", "user1", "User One");
    note.setId(noteId);

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

    // Act
    contactNoteService.deleteNote(contactId, noteId);

    // Assert
    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository).delete(note);
    verify(activityService).logActivity(
        eq(contactId),
        eq(ContactActivityType.NOTE_DELETED),
        anyString(),
        eq("user1"),
        eq("User One"),
        anyMap()
    );
  }

  @Test
  @DisplayName("Should throw exception when deleting note for non-existent contact")
  void shouldThrowExceptionWhenDeletingNoteForNonExistentContact() {
    // Arrange
    Long contactId = 999L;
    Long noteId = 10L;

    when(contactService.getContactById(contactId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.deleteNote(contactId, noteId))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository, never()).findById(any());
    verify(contactNoteRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent note")
  void shouldThrowExceptionWhenDeletingNonExistentNote() {
    // Arrange
    Long contactId = 1L;
    Long noteId = 999L;
    Contact contact = createTestContact(contactId);

    when(contactService.getContactById(contactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.deleteNote(contactId, noteId))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("not found");

    verify(contactService).getContactById(contactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should throw exception when deleting note with wrong contact ID")
  void shouldThrowExceptionWhenDeletingNoteWithWrongContactId() {
    // Arrange
    Long contactId = 1L;
    Long wrongContactId = 2L;
    Long noteId = 10L;
    Contact contact = createTestContact(wrongContactId);
    ContactNote note = new ContactNote(contactId, "Note to delete", "user1", "User One");
    note.setId(noteId);

    when(contactService.getContactById(wrongContactId)).thenReturn(Optional.of(contact));
    when(contactNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

    // Act & Assert
    assertThatThrownBy(() -> contactNoteService.deleteNote(wrongContactId, noteId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not belong to");

    verify(contactService).getContactById(wrongContactId);
    verify(contactNoteRepository).findById(noteId);
    verify(contactNoteRepository, never()).delete(any());
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
