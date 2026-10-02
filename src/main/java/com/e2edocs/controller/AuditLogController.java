package com.e2edocs.controller;

import com.e2edocs.dto.AuditEntryResponse;
import com.e2edocs.dto.PageResponse;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AuditEntryResponse>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        PageResponse<AuditEntryResponse> response = auditService.getAuditLogs(
                orgId, search, action, page, pageSize);
        return ResponseEntity.ok(response);
    }
}
