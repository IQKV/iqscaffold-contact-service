package com.iqscaffold.contactservice.note;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iqscaffold.contactservice.activity.ContactActivityService;
import com.iqscaffold.contactservice.activity.ContactActivityType;
import com.iqscaffold.contactservice.contact.ContactService;
import com.iqscaffold.contactservice.note.dto.ContactNoteDtos;
import com.iqscaffold.contactservice.note.dto.ContactNoteMapper;
import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ContactNoteService.
 */
@Service
@Transactional
public class ContactNoteServiceImpl implements ContactNoteService {

  private static final Logger log = LoggerFactory.getLogger(ContactNoteServiceImpl.class);

  private final ContactNoteRepository contactNoteRepository;
  private final ContactService contactService;
  private final ContactActivityService activityService;

  public ContactNoteServiceImpl(
      final ContactNoteRepository contactNoteRepository,
      final ContactService contactService,
      final ContactActivityService activityService) {
    this.contactNoteRepository = contactNoteRepository;
    this.contactService = contactService;
    this.activityService = activityService;
  }

  @Override
  public ContactNoteDtos.ContactNoteResponse createNote(
      Long contactId,
      ContactNoteDtos.CreateContactNoteRequest request,
      String createdByUserId,
      String createdByName) {
    log.debug("Creating note for contact ID: {}", contactId);

    // Verify contact exists
    contactService.getContactById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

    // Create note
    ContactNote note = ContactNoteMapper.toEntity(contactId, request, createdByUserId, createdByName);
    ContactNote savedNote = contactNoteRepository.save(note);

    // Log activity
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", savedNote.getId());
    metadata.put("contentLength", request.content().length());

    activityService.logActivity(
        contactId,
        ContactActivityType.NOTE_ADDED,
        "Note added to contact",
        createdByUserId,
        createdByName,
        metadata
    );

    log.info("Created note ID: {} for contact ID: {}", savedNote.getId(), contactId);
    return ContactNoteMapper.toResponse(savedNote);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContactNoteDtos.ContactNoteResponse> getNotesByContactId(Long contactId) {
    log.debug("Fetching notes for contact ID: {}", contactId);

    // Verify contact exists
    contactService.getContactById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

    List<ContactNote> notes = contactNoteRepository.findByContactIdOrderByCreatedAtDesc(contactId);
    return notes.stream()
        .map(ContactNoteMapper::toResponse)
        .toList();
  }

  @Override
  public ContactNoteDtos.ContactNoteResponse updateNote(
      Long contactId,
      Long noteId,
      ContactNoteDtos.UpdateContactNoteRequest request) {
    log.debug("Updating note ID: {} for contact ID: {}", noteId, contactId);

    // Verify contact exists
    contactService.getContactById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

    // Find and update note
    ContactNote note = contactNoteRepository.findById(noteId)
        .orElseThrow(() -> new ContactNotFoundException("Note not found with id: " + noteId));

    if (!note.getContactId().equals(contactId)) {
      throw new IllegalArgumentException("Note does not belong to the specified contact");
    }

    ContactNoteMapper.updateEntity(note, request);
    ContactNote updatedNote = contactNoteRepository.save(note);

    // Log activity
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", noteId);

    activityService.logActivity(
        contactId,
        ContactActivityType.NOTE_UPDATED,
        "Note updated",
        note.getCreatedByUserId(),
        note.getCreatedByName(),
        metadata
    );

    log.info("Updated note ID: {} for contact ID: {}", noteId, contactId);
    return ContactNoteMapper.toResponse(updatedNote);
  }

  @Override
  public void deleteNote(Long contactId, Long noteId) {
    log.debug("Deleting note ID: {} for contact ID: {}", noteId, contactId);

    // Verify contact exists
    contactService.getContactById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

    // Find and delete note
    ContactNote note = contactNoteRepository.findById(noteId)
        .orElseThrow(() -> new ContactNotFoundException("Note not found with id: " + noteId));

    if (!note.getContactId().equals(contactId)) {
      throw new IllegalArgumentException("Note does not belong to the specified contact");
    }

    contactNoteRepository.delete(note);

    // Log activity
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("noteId", noteId);

    activityService.logActivity(
        contactId,
        ContactActivityType.NOTE_DELETED,
        "Note deleted",
        note.getCreatedByUserId(),
        note.getCreatedByName(),
        metadata
    );

    log.info("Deleted note ID: {} for contact ID: {}", noteId, contactId);
  }
}
