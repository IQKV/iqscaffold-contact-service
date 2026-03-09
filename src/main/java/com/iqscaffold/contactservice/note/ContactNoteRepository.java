package com.iqscaffold.contactservice.note;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ContactNote entity operations.
 */
@Repository
public interface ContactNoteRepository extends JpaRepository<ContactNote, Long> {

  /**
   * Finds all notes for a specific contact, ordered by creation date descending.
   *
   * @param contactId The contact ID
   * @return List of notes for the contact
   */
  List<ContactNote> findByContactIdOrderByCreatedAtDesc(Long contactId);

  /**
   * Deletes all notes for a specific contact.
   *
   * @param contactId The contact ID
   */
  void deleteByContactId(Long contactId);

  /**
   * Counts notes for a specific contact.
   *
   * @param contactId The contact ID
   * @return Number of notes
   */
  long countByContactId(Long contactId);
}
