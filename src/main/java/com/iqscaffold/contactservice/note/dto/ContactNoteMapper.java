package com.iqscaffold.contactservice.note.dto;

import com.iqscaffold.contactservice.note.ContactNote;

/**
 * Mapper for converting between ContactNote entities and DTOs.
 */
public final class ContactNoteMapper {

  private ContactNoteMapper() {
    // Utility class
  }

  /**
   * Converts a ContactNote entity to a response DTO.
   *
   * @param note The contact note entity
   * @return The response DTO
   */
  public static ContactNoteDtos.ContactNoteResponse toResponse(ContactNote note) {
    if (note == null) {
      return null;
    }

    return new ContactNoteDtos.ContactNoteResponse(
        note.getId(),
        note.getContactId(),
        note.getContent(),
        note.getCreatedAt(),
        note.getUpdatedAt(),
        note.getCreatedByUserId(),
        note.getCreatedByName()
    );
  }

  /**
   * Creates a ContactNote entity from a create request.
   *
   * @param contactId       The contact ID
   * @param request         The create request
   * @param createdByUserId User ID creating the note
   * @param createdByName   Name of user creating the note
   * @return The contact note entity
   */
  public static ContactNote toEntity(
      Long contactId,
      ContactNoteDtos.CreateContactNoteRequest request,
      String createdByUserId,
      String createdByName) {
    return new ContactNote(
        contactId,
        request.content(),
        createdByUserId,
        createdByName
    );
  }

  /**
   * Updates a ContactNote entity from an update request.
   *
   * @param note    The existing contact note
   * @param request The update request
   */
  public static void updateEntity(
      ContactNote note,
      ContactNoteDtos.UpdateContactNoteRequest request) {
    note.setContent(request.content());
  }
}
