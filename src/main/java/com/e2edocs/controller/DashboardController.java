package com.e2edocs.controller;

import com.e2edocs.dto.AuditEntryResponse;
import com.e2edocs.dto.ChartDataPointResponse;
import com.e2edocs.dto.DashboardStatsResponse;
import com.e2edocs.dto.DocumentResponse;
import com.e2edocs.entity.Document;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.DashboardService;
import com.e2edocs.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public DashboardController(
            DashboardService dashboardService,
            DocumentRepository documentRepository,
            DocumentService documentService) {
        this.dashboardService = dashboardService;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        DashboardStatsResponse stats = dashboardService.getStats(orgId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<List<ChartDataPointResponse>> getStatusDistribution(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<ChartDataPointResponse> distribution = dashboardService.getStatusDistribution(orgId);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/department-workload")
    public ResponseEntity<List<ChartDataPointResponse>> getDepartmentWorkload(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<ChartDataPointResponse> workload = dashboardService.getDepartmentWorkload(orgId);
        return ResponseEntity.ok(workload);
    }

    @GetMapping("/weekly-activity")
    public ResponseEntity<List<ChartDataPointResponse>> getWeeklyActivity(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<ChartDataPointResponse> activity = dashboardService.getWeeklyActivity(orgId);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/recent-documents")
    public ResponseEntity<List<DocumentResponse>> getRecentDocuments(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<Document> docs = documentRepository.findTop5ByOrganizationIdOrderByCreatedAtDesc(orgId);
        List<DocumentResponse> response = docs.stream()
                .map(documentService::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<AuditEntryResponse>> getRecentActivity(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<AuditEntryResponse> activity = dashboardService.getRecentActivity(orgId);
        return ResponseEntity.ok(activity);
    }
}
