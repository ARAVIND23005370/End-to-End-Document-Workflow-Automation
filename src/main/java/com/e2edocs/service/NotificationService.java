package com.e2edocs.service;

import com.e2edocs.dto.NotificationResponse;
import com.e2edocs.entity.Notification;
import com.e2edocs.entity.enums.NotificationType;
import com.e2edocs.entity.enums.Priority;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(String organizationId, String userId) {
        return notificationRepository.findByOrganizationIdOrderByTimestampDesc(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(String organizationId, String userId) {
        notificationRepository.markAllAsReadForOrg(organizationId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String organizationId, String userId) {
        return notificationRepository.countByOrganizationIdAndReadFalse(organizationId);
    }

    @Transactional
    public Notification createNotification(String organizationId, String userId, String title, String message,
                                          NotificationType type, String documentId, String documentName, Priority priority) {
        String id = "n-" + UUID.randomUUID().toString().substring(0, 8);
        Notification notification = new Notification(id, organizationId, userId, title, message, type);
        notification.setDocumentId(documentId);
        notification.setDocumentName(documentName);
        notification.setPriority(priority);
        notification.setTimestamp(Instant.now());
        return notificationRepository.save(notification);
    }

    public NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                n.getDocumentId(),
                n.getDocumentName(),
                n.getTimestamp(),
                n.getPriority()
        );
    }
}
