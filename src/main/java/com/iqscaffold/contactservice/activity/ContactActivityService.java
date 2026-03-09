package com.iqscaffold.contactservice.activity;

import java.util.List;
import java.util.Map;

import com.iqscaffold.contactservice.activity.dto.ContactActivityDtos;

/**
 * Service interface for contact activity operations.
 */
public interface ContactActivityService {

  /**
   * Logs an activity for a contact.
   *
   * @param contactId         The contact ID
   * @param activityType      Type of activity
   * @param description       Activity description
   * @param performedByUserId User ID who performed the activity
   * @param performedByName   Name of user who performed the activity
   * @param metadata          Additional metadata
   */
  void logActivity(
      Long contactId,
      ContactActivityType activityType,
      String description,
      String performedByUserId,
      String performedByName,
      Map<String, Object> metadata);

  /**
   * Gets all activities for a contact.
   *
   * @param contactId The contact ID
   * @return List of activity responses
   */
  List<ContactActivityDtos.ContactActivityResponse> getContactActivities(Long contactId);
}
