package com.e2edocs.service;

import com.e2edocs.dto.AuditEntryResponse;
import com.e2edocs.dto.AuditHistoryItem;
import com.e2edocs.dto.PageResponse;
import com.e2edocs.entity.AuditLog;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String organizationId, String userId, String userName, AuditAction action,
                    String resource, String resourceId, AuditStatus status, String details, String ipAddress) {
        String id = "au-" + UUID.randomUUID().toString().substring(0, 8);
        AuditLog log = new AuditLog(id, organizationId, userId, userName, action, resource, resourceId, status, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEntryResponse> getAuditLogs(String organizationId, String search, String actionStr, int page, int pageSize) {
        int pageNumber = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("timestamp").descending());

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate userMatch = cb.like(cb.lower(root.get("userName")), searchPattern);
                Predicate detailsMatch = cb.like(cb.lower(root.get("details")), searchPattern);
                Predicate resourceMatch = cb.like(cb.lower(root.get("resource")), searchPattern);
                Predicate resourceIdMatch = cb.like(cb.lower(root.get("resourceId")), searchPattern);
                predicates.add(cb.or(userMatch, detailsMatch, resourceMatch, resourceIdMatch));
            }

            if (actionStr != null && !actionStr.isBlank()) {
                try {
                    AuditAction action = AuditAction.fromValue(actionStr);
                    predicates.add(cb.equal(root.get("action"), action));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> resultPage = auditLogRepository.findAll(spec, pageable);
        List<AuditEntryResponse> dtos = resultPage.getContent().stream()
                .map(this::mapToAuditEntryResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, resultPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> getRecentActivity(String organizationId) {
        return auditLogRepository.findTop5ByOrganizationIdOrderByTimestampDesc(organizationId).stream()
                .map(this::mapToAuditEntryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditHistoryItem> getAuditHistoryForResource(String resourceId) {
        return auditLogRepository.findByResourceIdOrderByTimestampDesc(resourceId).stream()
                .map(log -> new AuditHistoryItem(
                        log.getId(),
                        log.getTimestamp(),
                        log.getUserId(),
                        log.getUserName(),
                        log.getAction() != null ? log.getAction().getValue() : null,
                        log.getResource(),
                        log.getResourceId(),
                        log.getStatus() != null ? log.getStatus().getValue() : null,
                        log.getDetails()
                ))
                .collect(Collectors.toList());
    }

    private AuditEntryResponse mapToAuditEntryResponse(AuditLog log) {
        return new AuditEntryResponse(
                log.getId(),
                log.getTimestamp(),
                log.getUserId(),
                log.getUserName(),
                log.getAction(),
                log.getResource(),
                log.getResourceId(),
                log.getStatus(),
                log.getDetails(),
                log.getIpAddress()
        );
    }
}
