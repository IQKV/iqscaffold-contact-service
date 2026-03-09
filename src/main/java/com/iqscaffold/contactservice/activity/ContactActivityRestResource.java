package com.iqscaffold.contactservice.activity;

import java.util.List;

import com.iqscaffold.contactservice.activity.dto.ContactActivityDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for contact activity operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Retrieving activity timeline for a contact</li>
 * </ul>
 *
 * <h4>Authorization:</h4>
 * <ul>
 *   <li>Activity viewing: Requires CRM_CONTACT_MANAGER, CRM_ACCESS, CRM_ADMIN, USER, ADMIN, or SUPER_ADMIN role</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/contacts/{contactId}/activities")
@Tag(name = "Contact Activities", description = "Contact activity log operations")
@SecurityRequirement(name = "bearerAuth")
public class ContactActivityRestResource {

  private final ContactActivityService activityService;

  public ContactActivityRestResource(final ContactActivityService activityService) {
    this.activityService = activityService;
  }

  /**
   * Retrieves the activity timeline for a specific contact.
   * <p>
   * Returns all activities for the contact sorted by timestamp in descending order
   * (newest first). Activities include contact creation, updates, notes, status changes,
   * and other significant events.
   *
   * @param contactId The contact ID
   * @return List of activities for the contact
   */
  @Operation(
      summary = "Get contact activity timeline",
      description = "Retrieves the complete activity timeline for a contact, including all "
                    + "interactions and state changes. Activities are sorted by timestamp in descending "
                    + "order (newest first).")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Activities retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Contact not found")
  })
  @GetMapping
  @PreAuthorize("hasAnyAuthority('CRM_CONTACT_MANAGER', 'CRM_ACCESS', 'CRM_ADMIN', 'USER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<ContactActivityDtos.ContactActivityResponse>> getContactActivities(
      @PathVariable Long contactId) {
    List<ContactActivityDtos.ContactActivityResponse> activities = activityService.getContactActivities(contactId);
    return ResponseEntity.ok(activities);
  }
}
