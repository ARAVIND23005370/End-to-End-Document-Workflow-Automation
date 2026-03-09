package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.model.AuditLog;
import com.project.documentworkflow.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuditController
 *
 * FEATURE: Audit everything.
 * Every action (upload, approve, reject, forward, email) is logged.
 *
 * GET /api/audit           → All audit logs (ADMIN only)
 * GET /api/audit/{docId}   → Audit trail for a specific document
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping
    public ApiResponse<List<AuditLog>> getAllLogs() {
        return new ApiResponse<>(true, auditService.getAllLogs(), null);
    }

    @GetMapping("/{documentId}")
    public ApiResponse<List<AuditLog>> getLogsForDocument(@PathVariable Long documentId) {
        return new ApiResponse<>(true, auditService.getLogsForDocument(documentId), null);
    }
}
