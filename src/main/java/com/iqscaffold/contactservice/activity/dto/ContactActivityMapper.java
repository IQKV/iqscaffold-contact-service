package com.iqscaffold.contactservice.activity.dto;

import com.iqscaffold.contactservice.activity.ContactActivity;

/**
 * Mapper for converting between ContactActivity entities and DTOs.
 */
public final class ContactActivityMapper {

  private ContactActivityMapper() {
    // Utility class
  }

  /**
   * Converts a ContactActivity entity to a response DTO.
   *
   * @param activity The contact activity entity
   * @return The response DTO
   */
  public static ContactActivityDtos.ContactActivityResponse toResponse(ContactActivity activity) {
    if (activity == null) {
      return null;
    }

    return new ContactActivityDtos.ContactActivityResponse(
        activity.getId(),
        activity.getContactId(),
        activity.getActivityType(),
        activity.getDescription(),
        activity.getTimestamp(),
        activity.getPerformedByUserId(),
        activity.getPerformedByName(),
        activity.getMetadata()
    );
  }
}
