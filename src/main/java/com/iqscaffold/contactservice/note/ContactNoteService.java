package com.iqscaffold.contactservice.note;

import java.util.List;

import com.iqscaffold.contactservice.note.dto.ContactNoteDtos;

/**
 * Service interface for contact note operations.
 */
public interface ContactNoteService {

  /**
   * Creates a new note for a contact.
   *
   * @param contactId       The contact ID
   * @param request         The create request
   * @param createdByUserId User ID creating the note
   * @param createdByName   Name of user creating the note
   * @return The created note response
   */
  ContactNoteDtos.ContactNoteResponse createNote(
      Long contactId,
      ContactNoteDtos.CreateContactNoteRequest request,
      String createdByUserId,
      String createdByName);

  /**
   * Gets all notes for a contact.
   *
   * @param contactId The contact ID
   * @return List of note responses
   */
  List<ContactNoteDtos.ContactNoteResponse> getNotesByContactId(Long contactId);

  /**
   * Updates an existing note.
   *
   * @param contactId The contact ID
   * @param noteId    The note ID
   * @param request   The update request
   * @return The updated note response
   */
  ContactNoteDtos.ContactNoteResponse updateNote(
      Long contactId,
      Long noteId,
      ContactNoteDtos.UpdateContactNoteRequest request);

  /**
   * Deletes a note.
   *
   * @param contactId The contact ID
   * @param noteId    The note ID
   */
  void deleteNote(Long contactId, Long noteId);
}
