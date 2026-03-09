package com.iqscaffold.contactservice.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing an activity log entry for a contact.
 * <p>
 * Activities track all significant events and changes related to a contact,
 * providing a complete audit trail and timeline of interactions.
 */
@Entity
@Table(name = "contact_activities")
public class ContactActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "contact_id", nullable = false)
  private Long contactId;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false, length = 50)
  private ContactActivityType activityType;

  @Column(name = "description", nullable = false, length = 1000)
  private String description;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Column(name = "performed_by_user_id", nullable = false, length = 255)
  private String performedByUserId;

  @Column(name = "performed_by_name", nullable = false, length = 255)
  private String performedByName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata;

  // Constructors
  public ContactActivity() {
  }

  public ContactActivity(
      Long contactId,
      ContactActivityType activityType,
      String description,
      LocalDateTime timestamp,
      String performedByUserId,
      String performedByName,
      Map<String, Object> metadata) {
    this.contactId = contactId;
    this.activityType = activityType;
    this.description = description;
    this.timestamp = timestamp;
    this.performedByUserId = performedByUserId;
    this.performedByName = performedByName;
    this.metadata = metadata;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getContactId() {
    return contactId;
  }

  public void setContactId(Long contactId) {
    this.contactId = contactId;
  }

  public ContactActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(ContactActivityType activityType) {
    this.activityType = activityType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getPerformedByUserId() {
    return performedByUserId;
  }

  public void setPerformedByUserId(String performedByUserId) {
    this.performedByUserId = performedByUserId;
  }

  public String getPerformedByName() {
    return performedByName;
  }

  public void setPerformedByName(String performedByName) {
    this.performedByName = performedByName;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }
}
