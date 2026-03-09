package com.iqscaffold.contactservice.activity.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iqscaffold.contactservice.activity.ContactActivityType;

/**
 * DTOs for Contact Activity operations.
 */
public final class ContactActivityDtos {

  private ContactActivityDtos() {
    // Utility class
  }

  /**
   * Response DTO for contact activity.
   *
   * @param id                Activity ID
   * @param contactId         Contact ID
   * @param activityType      Type of activity
   * @param description       Activity description
   * @param timestamp         Activity timestamp
   * @param performedByUserId User ID who performed the activity
   * @param performedByName   Name of user who performed the activity
   * @param metadata          Additional metadata
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ContactActivityResponse(
      Long id,
      Long contactId,
      ContactActivityType activityType,
      String description,
      LocalDateTime timestamp,
      String performedByUserId,
      String performedByName,
      Map<String, Object> metadata
  ) {
  }
}
