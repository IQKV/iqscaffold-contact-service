package com.iqscaffold.contactservice.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTOs for Contact Note operations.
 */
public final class ContactNoteDtos {

  private ContactNoteDtos() {
    // Utility class
  }

  /**
   * Request DTO for creating a contact note.
   *
   * @param content Note content (required, max 5000 chars)
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CreateContactNoteRequest(
      @NotBlank(message = "Content is required")
      @Size(max = 5000, message = "Content must not exceed 5000 characters")
      String content
  ) {
  }

  /**
   * Request DTO for updating a contact note.
   *
   * @param content Note content (required, max 5000 chars)
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record UpdateContactNoteRequest(
      @NotBlank(message = "Content is required")
      @Size(max = 5000, message = "Content must not exceed 5000 characters")
      String content
  ) {
  }

  /**
   * Response DTO for contact note.
   *
   * @param id              Note ID
   * @param contactId       Contact ID
   * @param content         Note content
   * @param createdAt       Creation timestamp
   * @param updatedAt       Last update timestamp
   * @param createdByUserId User ID who created the note
   * @param createdByName   Name of user who created the note
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ContactNoteResponse(
      Long id,
      Long contactId,
      String content,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String createdByUserId,
      String createdByName
  ) {
  }
}
