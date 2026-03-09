package com.iqscaffold.contactservice.note;

import jakarta.validation.Valid;
import java.util.List;

import com.iqscaffold.contactservice.note.dto.ContactNoteDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for contact note management operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Adding notes to contacts</li>
 *   <li>Retrieving contact notes</li>
 *   <li>Updating note content</li>
 *   <li>Deleting notes</li>
 * </ul>
 *
 * <h4>Authorization:</h4>
 * <ul>
 *   <li>All operations: Requires CRM_CONTACT_MANAGER, CRM_ACCESS, CRM_ADMIN, USER, ADMIN, or SUPER_ADMIN role</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/contacts/{contactId}/notes")
@Tag(name = "Contact Notes", description = "Contact note management operations")
@SecurityRequirement(name = "bearerAuth")
public class ContactNoteRestResource {

  private final ContactNoteService contactNoteService;

  public ContactNoteRestResource(final ContactNoteService contactNoteService) {
    this.contactNoteService = contactNoteService;
  }

  /**
   * Adds a new note to a contact.
   *
   * @param contactId The contact ID
   * @param request   The note creation request
   * @return The created note
   */
  @Operation(
      summary = "Add note to contact",
      description = "Creates a new note for the specified contact")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Note created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Contact not found")
  })
  @PostMapping
  @PreAuthorize("hasAnyAuthority('CRM_CONTACT_MANAGER', 'CRM_ACCESS', 'CRM_ADMIN', 'USER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ContactNoteDtos.ContactNoteResponse> addNote(
      @PathVariable Long contactId,
      @Valid @RequestBody ContactNoteDtos.CreateContactNoteRequest request) {
    String userId = getCurrentUserId();
    String userName = getCurrentUserName();

    ContactNoteDtos.ContactNoteResponse response = contactNoteService.createNote(
        contactId,
        request,
        userId,
        userName
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves all notes for a contact.
   *
   * @param contactId The contact ID
   * @return List of notes ordered by creation date descending
   */
  @Operation(
      summary = "Get contact notes",
      description = "Retrieves all notes for the specified contact, ordered by creation date descending")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Contact not found")
  })
  @GetMapping
  @PreAuthorize("hasAnyAuthority('CRM_CONTACT_MANAGER', 'CRM_ACCESS', 'CRM_ADMIN', 'USER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<ContactNoteDtos.ContactNoteResponse>> getContactNotes(
      @PathVariable Long contactId) {
    List<ContactNoteDtos.ContactNoteResponse> notes = contactNoteService.getNotesByContactId(contactId);
    return ResponseEntity.ok(notes);
  }

  /**
   * Updates an existing note.
   *
   * @param contactId The contact ID
   * @param noteId    The note ID
   * @param request   The note update request
   * @return The updated note
   */
  @Operation(
      summary = "Update note",
      description = "Updates an existing note for the specified contact")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Note updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Contact or note not found")
  })
  @PutMapping("/{noteId}")
  @PreAuthorize("hasAnyAuthority('CRM_CONTACT_MANAGER', 'CRM_ACCESS', 'CRM_ADMIN', 'USER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ContactNoteDtos.ContactNoteResponse> updateNote(
      @PathVariable Long contactId,
      @PathVariable Long noteId,
      @Valid @RequestBody ContactNoteDtos.UpdateContactNoteRequest request) {
    ContactNoteDtos.ContactNoteResponse response = contactNoteService.updateNote(
        contactId,
        noteId,
        request
    );
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a note.
   *
   * @param contactId The contact ID
   * @param noteId    The note ID
   * @return No content
   */
  @Operation(
      summary = "Delete note",
      description = "Deletes a note from the specified contact")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Note deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Contact or note not found")
  })
  @DeleteMapping("/{noteId}")
  @PreAuthorize("hasAnyAuthority('CRM_CONTACT_MANAGER', 'CRM_ACCESS', 'CRM_ADMIN', 'USER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> deleteNote(
      @PathVariable Long contactId,
      @PathVariable Long noteId) {
    contactNoteService.deleteNote(contactId, noteId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Extracts the current user ID from the JWT token.
   *
   * @return The user ID, or "system" if not available
   */
  private String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String userId = jwt.getClaimAsString("userId");
      if (userId != null) {
        return userId;
      }
      return jwt.getSubject();
    }
    return "system";
  }

  /**
   * Extracts the current user name from the JWT token.
   *
   * @return The user name, or "System" if not available
   */
  private String getCurrentUserName() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String name = jwt.getClaimAsString("name");
      if (name != null) {
        return name;
      }
      String email = jwt.getClaimAsString("email");
      if (email != null) {
        return email;
      }
    }
    return "System";
  }
}
