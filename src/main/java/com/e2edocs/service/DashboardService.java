package com.e2edocs.service;

import com.e2edocs.dto.AuditEntryResponse;
import com.e2edocs.dto.ChartDataPointResponse;
import com.e2edocs.dto.DashboardStatsResponse;
import com.e2edocs.dto.DocumentResponse;
import com.e2edocs.entity.Document;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import com.e2edocs.entity.enums.RuleStatus;
import com.e2edocs.entity.enums.WorkflowStatus;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.RuleRepository;
import com.e2edocs.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DocumentRepository documentRepository;
    private final RuleRepository ruleRepository;
    private final WorkflowRepository workflowRepository;
    private final AuditService auditService;

    public DashboardService(
            DocumentRepository documentRepository,
            RuleRepository ruleRepository,
            WorkflowRepository workflowRepository,
            AuditService auditService) {
        this.documentRepository = documentRepository;
        this.ruleRepository = ruleRepository;
        this.workflowRepository = workflowRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(String organizationId) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalDocuments(documentRepository.countByOrganizationId(organizationId));
        stats.setProcessing(documentRepository.countByOrganizationIdAndStatus(organizationId, DocumentStatus.PROCESSING));
        stats.setApproved(documentRepository.countByOrganizationIdAndStatus(organizationId, DocumentStatus.APPROVED));
        stats.setReview(documentRepository.countByOrganizationIdAndStatus(organizationId, DocumentStatus.REVIEW));
        stats.setRejected(documentRepository.countByOrganizationIdAndStatus(organizationId, DocumentStatus.REJECTED));
        stats.setDraft(documentRepository.countByOrganizationIdAndStatus(organizationId, DocumentStatus.DRAFT));
        stats.setCriticalItems(documentRepository.countByOrganizationIdAndPriority(organizationId, Priority.CRITICAL));
        stats.setActiveRules(ruleRepository.countByOrganizationIdAndStatus(organizationId, RuleStatus.ACTIVE));
        stats.setActiveWorkflows(workflowRepository.countByOrganizationIdAndStatus(organizationId, WorkflowStatus.ACTIVE));

        return stats;
    }

    @Transactional(readOnly = true)
    public List<ChartDataPointResponse> getStatusDistribution(String organizationId) {
        Map<DocumentStatus, Long> counts = new HashMap<>();
        for (DocumentStatus status : DocumentStatus.values()) {
            counts.put(status, 0L);
        }

        List<Object[]> grouped = documentRepository.countByStatusGrouped(organizationId);
        for (Object[] row : grouped) {
            if (row != null && row.length == 2 && row[0] instanceof DocumentStatus status) {
                counts.put(status, ((Number) row[1]).longValue());
            }
        }

        List<ChartDataPointResponse> result = new ArrayList<>();
        result.add(new ChartDataPointResponse("Approved", counts.get(DocumentStatus.APPROVED), "var(--color-success-500)"));
        result.add(new ChartDataPointResponse("Review Required", counts.get(DocumentStatus.REVIEW), "var(--color-warning-500)"));
        result.add(new ChartDataPointResponse("Processing", counts.get(DocumentStatus.PROCESSING), "var(--color-primary-500)"));
        result.add(new ChartDataPointResponse("Draft", counts.get(DocumentStatus.DRAFT), "var(--color-neutral-400)"));
        result.add(new ChartDataPointResponse("Rejected", counts.get(DocumentStatus.REJECTED), "var(--color-danger-500)"));

        return result;
    }

    @Transactional(readOnly = true)
    public List<ChartDataPointResponse> getDepartmentWorkload(String organizationId) {
        List<Object[]> grouped = documentRepository.countByDepartmentGrouped(organizationId);
        List<ChartDataPointResponse> result = new ArrayList<>();

        for (Object[] row : grouped) {
            if (row != null && row.length == 2 && row[0] != null) {
                String dept = row[0].toString();
                long val = ((Number) row[1]).longValue();
                result.add(new ChartDataPointResponse(dept, val));
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<ChartDataPointResponse> getWeeklyActivity(String organizationId) {
        // Group recent documents by day of week
        List<Document> docs = documentRepository.findAll();
        Map<DayOfWeek, Long> countByDay = new LinkedHashMap<>();
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
            countByDay.put(day, 0L);
        }

        for (Document d : docs) {
            if (d.getOrganizationId().equals(organizationId) && d.getCreatedAt() != null) {
                DayOfWeek dow = LocalDate.ofInstant(d.getCreatedAt(), ZoneId.systemDefault()).getDayOfWeek();
                countByDay.put(dow, countByDay.getOrDefault(dow, 0L) + 1);
            }
        }

        List<ChartDataPointResponse> list = new ArrayList<>();
        for (Map.Entry<DayOfWeek, Long> entry : countByDay.entrySet()) {
            String label = entry.getKey().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            list.add(new ChartDataPointResponse(label, entry.getValue()));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> getRecentActivity(String organizationId) {
        return auditService.getRecentActivity(organizationId);
    }
}
