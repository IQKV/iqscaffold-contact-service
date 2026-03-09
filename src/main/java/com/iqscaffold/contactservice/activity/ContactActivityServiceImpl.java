package com.iqscaffold.contactservice.activity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.iqscaffold.contactservice.activity.dto.ContactActivityDtos;
import com.iqscaffold.contactservice.activity.dto.ContactActivityMapper;
import com.iqscaffold.contactservice.contact.ContactService;
import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ContactActivityService.
 */
@Service
@Transactional
public class ContactActivityServiceImpl implements ContactActivityService {

  private static final Logger log = LoggerFactory.getLogger(ContactActivityServiceImpl.class);

  private final ContactActivityRepository activityRepository;
  private final ContactService contactService;

  public ContactActivityServiceImpl(
      final ContactActivityRepository activityRepository,
      final ContactService contactService) {
    this.activityRepository = activityRepository;
    this.contactService = contactService;
  }

  @Override
  public void logActivity(
      Long contactId,
      ContactActivityType activityType,
      String description,
      String performedByUserId,
      String performedByName,
      Map<String, Object> metadata) {
    log.debug("Logging activity {} for contact ID: {}", activityType, contactId);

    ContactActivity activity = new ContactActivity(
        contactId,
        activityType,
        description,
        LocalDateTime.now(),
        performedByUserId,
        performedByName,
        metadata
    );

    activityRepository.save(activity);
    log.info("Logged activity {} for contact ID: {}", activityType, contactId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContactActivityDtos.ContactActivityResponse> getContactActivities(Long contactId) {
    log.debug("Fetching activities for contact ID: {}", contactId);

    // Verify contact exists
    contactService.getContactById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found with id: " + contactId));

    List<ContactActivity> activities = activityRepository.findByContactIdOrderByTimestampDesc(contactId);
    return activities.stream()
        .map(ContactActivityMapper::toResponse)
        .toList();
  }
}
