package com.iqscaffold.contactservice.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ContactActivity entity operations.
 */
@Repository
public interface ContactActivityRepository extends JpaRepository<ContactActivity, Long> {

  /**
   * Finds all activities for a specific contact, ordered by timestamp descending.
   *
   * @param contactId The contact ID
   * @return List of activities for the contact
   */
  List<ContactActivity> findByContactIdOrderByTimestampDesc(Long contactId);

  /**
   * Deletes all activities for a specific contact.
   *
   * @param contactId The contact ID
   */
  void deleteByContactId(Long contactId);

  /**
   * Counts activities for a specific contact.
   *
   * @param contactId The contact ID
   * @return Number of activities
   */
  long countByContactId(Long contactId);
}
