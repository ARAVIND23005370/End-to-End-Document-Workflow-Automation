package com.e2edocs.controller;

import com.e2edocs.dto.MessageResponse;
import com.e2edocs.dto.NotificationResponse;
import com.e2edocs.dto.UnreadCountResponse;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "u-001";
        List<NotificationResponse> notifications = notificationService.getAllNotifications(orgId, userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "u-001";
        notificationService.markAllRead(orgId, userId);
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "u-001";
        long count = notificationService.getUnreadCount(orgId, userId);
        return ResponseEntity.ok(count);
    }
}
